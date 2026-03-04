package com.omegafrog.My.piano.app.web.service.cache;

import com.omegafrog.My.piano.app.cache.SingleFlightCoordinator;
import com.omegafrog.My.piano.app.cache.SwrCacheValue;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class SheetPostCacheCoordinator {

    public static final String SHEET_POST_LIST_CACHE = "sheetpostList";
    public static final String SHEET_POST_DETAIL_CACHE = "sheetpostDetail";
    public static final long CACHE_WAIT_TIMEOUT_MS = 2_000L;

    public static final long LIST_HARD_TTL_MS = 120_000L;
    public static final long LIST_SOFT_TTL_MIN_MS = 70_000L;
    public static final long LIST_SOFT_TTL_MAX_MS = 100_000L;

    public static final long DETAIL_HARD_TTL_MS = 300_000L;
    public static final long DETAIL_SOFT_TTL_MIN_MS = 210_000L;
    public static final long DETAIL_SOFT_TTL_MAX_MS = 270_000L;

    public static final int WARMUP_LIST_PAGE_SIZE = 20;
    public static final int WARMUP_MAX_LIST_PAGE = 2;
    public static final int WARMUP_MAX_DETAIL_IDS = 100;

    private final CacheManager cacheManager;
    private final SingleFlightCoordinator singleFlightCoordinator;

    @Qualifier("ThreadPoolTaskExecutor")
    private final ObjectProvider<Executor> threadPoolTaskExecutorProvider;

    public Cache getRequiredCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalStateException("Cache not configured: " + cacheName);
        }
        return cache;
    }

    public void evictSheetPostListCache() {
        Cache cache = cacheManager.getCache(SHEET_POST_LIST_CACHE);
        if (cache != null) {
            cache.clear();
        }
    }

    public void evictSheetPostDetailCache(Long id) {
        Cache cache = cacheManager.getCache(SHEET_POST_DETAIL_CACHE);
        if (cache != null) {
            cache.evict(id);
        }
    }

    public <T extends Serializable> T getWithSWR(
            Cache cache,
            Object key,
            Supplier<T> loader,
            long softTtlMinMs,
            long softTtlMaxMs,
            long hardTtlMs
    ) {
        long now = System.currentTimeMillis();
        Cache.ValueWrapper wrapper = cache.get(key);
        SwrCacheValue<T> cached = null;
        if (wrapper != null && wrapper.get() instanceof SwrCacheValue<?> swrCacheValue) {
            @SuppressWarnings("unchecked")
            SwrCacheValue<T> casted = (SwrCacheValue<T>) swrCacheValue;
            cached = casted;
        }

        if (cached != null && cached.isFresh(now)) {
            return cached.payload();
        }

        if (cached != null && cached.isStaleWindow(now)) {
            Executor executor = threadPoolTaskExecutorProvider.getIfAvailable();
            if (executor != null) {
                singleFlightCoordinator.refreshAsyncIfAbsent(cache.getName(), key, () -> {
                    T loaded = loader.get();
                    cache.put(key, buildSwrValue(loaded, softTtlMinMs, softTtlMaxMs, hardTtlMs));
                    return loaded;
                }, executor);
            }
            return cached.payload();
        }

        try {
            return singleFlightCoordinator.loadBlocking(cache.getName(), key, () -> {
                T loaded = loader.get();
                cache.put(key, buildSwrValue(loaded, softTtlMinMs, softTtlMaxMs, hardTtlMs));
                return loaded;
            }, CACHE_WAIT_TIMEOUT_MS);
        } catch (TimeoutException e) {
            if (cached != null) {
                return cached.payload();
            }
            throw new IllegalStateException("Cache load timed out for key: " + key, e);
        }
    }

    public <T extends Serializable> SwrCacheValue<T> buildSwrValue(
            T payload,
            long softTtlMinMs,
            long softTtlMaxMs,
            long hardTtlMs
    ) {
        long now = System.currentTimeMillis();
        long softTtl = ThreadLocalRandom.current().nextLong(softTtlMinMs, softTtlMaxMs + 1);
        return new SwrCacheValue<>(payload, now, now + softTtl, now + hardTtlMs);
    }
}
