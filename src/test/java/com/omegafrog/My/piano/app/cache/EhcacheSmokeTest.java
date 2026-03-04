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

class EhcacheSmokeTest {

    @Test
    @DisplayName("ehcache.xml configured caches are available and can store data")
    void ehcacheShouldBeConfiguredAndOperational() throws Exception {
        URL configUrl = Thread.currentThread().getContextClassLoader().getResource("ehcache.xml");
        assertNotNull(configUrl, "ehcache.xml must exist on classpath");

        URI configUri = configUrl.toURI();
        CachingProvider cachingProvider = Caching.getCachingProvider();

        CacheManager cacheManager = cachingProvider.getCacheManager(configUri, Thread.currentThread().getContextClassLoader());
        try {
            List<String> requiredCaches = List.of(
                    "sheetpostList",
                    "sheetpostDetail",
                    "sheetPostViewCounts",
                    "sheetPostLikeCounts",
                    "postViewCounts",
                    "postLikeCounts",
                    "lessonViewCounts",
                    "lessonLikeCounts",
                    "sheetpost",
                    "lesson"
            );

            for (String cacheName : requiredCaches) {
                Cache<?, ?> cache = cacheManager.getCache(cacheName);
                assertNotNull(cache, "Cache must be configured: " + cacheName);
            }

            Cache<String, Object> listCache = cacheManager.getCache("sheetpostList", String.class, Object.class);
            assertNotNull(listCache, "sheetpostList cache must be available");

            String key = "cache-smoke-key";
            String value = "cache-smoke-value";
            listCache.put(key, value);

            Object cachedValue = listCache.get(key);
            assertEquals(value, cachedValue, "Cache should return stored value");
        } finally {
            cacheManager.close();
            cachingProvider.close();
        }
    }
}
