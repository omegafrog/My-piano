# Architecture

## Purpose

- Document the repository architecture assumptions that orchestration agents must respect.

## Current Stack

- Language: Java
- Framework: Spring Boot
- Runtime: Java 17
- Build Tool: Gradle
- Package Manager: Gradle

## Application Shape

- The codebase is a layered Spring Boot monolith.
- HTTP entrypoints live under `app/web/controller`, business logic under `app/web/service`, entities and repository interfaces under `app/web/domain`, and persistence adapters under `app/web/infra`.
- Security, batch/scheduling, external integrations, and cross-cutting configuration live in sibling packages under `app/`.

## Major Domains

- Sheet posts, community posts, lessons, users, orders/payments, tickets, and file uploads are the main product areas.
- Search uses Elasticsearch, hot-path counters and caches use Redis/Ehcache, and file handling switches between local storage and S3 depending on profile.

## Async and Integration Boundaries

- The repository uses scheduled outbox processors plus Kafka/event publishing for asynchronous workflows.
- When extending async behavior, prefer existing outbox services/processors and `EventPublisher` conventions over adding new direct integrations in controllers.
- Topic names and consumer bindings are code-level contracts; treat changes there as cross-module changes.

## Profiles and Environment

- `dev` is the default profile in tracked config and uses local storage.
- Production configuration is externalized by deployment scripts rather than fully described in tracked YAML.
- Test configuration depends on local MySQL/Redis and excludes several Elasticsearch/Redis auto-configurations.

## Testing and Verification Rules

- `./gradlew build` is the repository-level completion gate after code changes.
- `./gradlew test` is the baseline unit/integration command, but many tests are full-context `@SpringBootTest` + `MockMvc` flows rather than narrow slices.
- Repository and persistence behavior is also covered with `@DataJpaTest`-based tests.

## Practical Boundaries

- Keep controllers thin and move behavior into services/application services.
- Preserve the domain-repository-interface vs infra-implementation split.
- Reuse package-level `AGENTS.md` documents for local constraints before inventing new navigation or ownership rules.
