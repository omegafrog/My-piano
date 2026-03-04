# Ehcache SheetPost caching (SPEC)

This document describes the planned caching behavior for SheetPost APIs.
It is a specification (planned behavior) and may differ from the current implementation until code lands.

## Purpose
- Reduce ES/DB load for read-heavy SheetPost endpoints.
- Prevent cache stampede (thundering herd) on cache misses/expirations.
- Keep correctness under eviction by using DB fallback.

## Caches and TTLs

### sheetpostList
- Stores: list response payload for a normalized search options + page.
- Scope: cache only pages 0..2.
- Hard TTL: 120s.
- Soft TTL (SWR): randomized per entry in [70s..100s].

### sheetpostDetail
- Stores: base detail payload (exclude dynamic fields).
- Hard TTL: 300s.
- Soft TTL (SWR): randomized per entry in [210s..270s].

### Count caches (view/like)
- Stores: absolute view/like count by id.
- Durability choice:
  - Write-through (safe under eviction/restart): update DB per increment/decrement.
  - Write-behind (batch sync): lower DB writes, but eviction/restart can lose unflushed increments.

## Single-flight (cache miss coalescing)

For the same cache key, only one loader runs.
Concurrent requests for the same key wait on the same in-flight `CompletableFuture`.

- Wait timeout: 2s.
- Timeout behavior:
  - If a stale value is available: return stale.
  - If no stale value exists: return 503/504 (implementation-defined).
- A timeout does not start a second loader.

## Stale-While-Revalidate (SWR)

We track two expiry timestamps per cached entry:
- softExpireAt: after this point the value is stale but still served.
- hardExpireAt: after this point the value is not served; requests block on reload.

### SWR behavior

| State | Condition | Response | Refresh behavior |
|------|-----------|----------|------------------|
| Fresh | now < softExpireAt | return cached payload | none |
| Stale | softExpireAt <= now < hardExpireAt | return stale payload immediately | trigger background refresh (single-flight) |
| Hard expired | now >= hardExpireAt | block on reload (single-flight) | loader runs; callers wait (bounded) |

Notes:
- SWR is implemented at the application layer (Ehcache does not provide SWR as a built-in).
- Background refresh uses Spring `ThreadPoolTaskExecutor` when available.

## Cache key schema

### sheetpostList key
Normalized key fields:
- searchSentence: null/blank -> empty string
- instrument/difficulty/genre lists: null -> empty; sort; stable join
- page: 0-based; cache only if page in {0,1,2}
- size: cache only if size <= 50

### sheetpostDetail key
- id (Long)

## Write policy and invalidation (no events)

Write policy is write-around (evict on write), not write-through, for list/detail caches.

- On SheetPost create/update/delete:
  - clear sheetpostList (search key cardinality is high; partial update is not attempted)
- On SheetPost update/delete:
  - evict sheetpostDetail for that id

No event-driven invalidation is used.

## Capacity-full policy

Ehcache caches are configured with bounded resource pools (e.g., heap entries).
When the pool is full, Ehcache evicts existing entries to make room.

- list/detail eviction is acceptable: cache miss falls back to DB/ES and single-flight prevents stampede.
- count caches must not lose increments:
  - prefer write-through if eviction/restart loss is unacceptable.

## Metrics

- cache hit/miss (Actuator/Micrometer):
  - `cache.gets{cache=...,result=hit|miss}`
- occupancy gauges:
  - `cache.entries{cache=...}`
  - `cache.occupancy.ratio{cache=...}`
- optional single-flight metrics:
  - `singleflight.inflight{cache=...}`
  - `singleflight.wait.ms`

## Caveats
- Do not cache user-personalized fields (e.g., per-user like flags) in shared caches.
- Always overlay dynamic count fields on responses rather than storing them inside cached DTO payloads.
