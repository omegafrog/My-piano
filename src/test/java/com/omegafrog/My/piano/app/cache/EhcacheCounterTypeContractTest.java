package com.omegafrog.My.piano.app.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EhcacheCounterTypeContractTest {

    @Test
    @DisplayName("counter caches use the Long and Integer repository contract")
    void counterCachesShouldSupportTypedAccess() throws Exception {
        URL configUrl = Thread.currentThread().getContextClassLoader().getResource("ehcache.xml");
        assertNotNull(configUrl, "ehcache.xml must exist on classpath");

        URI configUri = configUrl.toURI();
        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager(
                configUri,
                Thread.currentThread().getContextClassLoader()
        );

        try {
            List<String> counterCaches = List.of(
                    "sheetPostViewCounts",
                    "sheetPostLikeCounts",
                    "postViewCounts",
                    "postLikeCounts",
                    "lessonViewCounts",
                    "lessonLikeCounts"
            );

            for (int index = 0; index < counterCaches.size(); index++) {
                String cacheName = counterCaches.get(index);
                Cache<Long, Integer> cache = cacheManager.getCache(cacheName, Long.class, Integer.class);
                assertNotNull(cache, "Typed counter cache must be configured: " + cacheName);

                long key = index + 1L;
                int value = (index + 1) * 10;
                cache.put(key, value);
                assertEquals(value, cache.get(key), "Counter cache must round-trip Integer values: " + cacheName);
            }
        } finally {
            cacheManager.close();
            cachingProvider.close();
        }
    }
}
