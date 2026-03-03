package com.omegafrog.My.piano.app.cache;

import java.net.URI;
import java.net.URL;
import java.util.concurrent.Executor;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@ActiveProfiles("test")
class JCacheCacheManagerOverrideIntegrationTest {

    @Autowired
    @Qualifier("testCacheManager")
    private CacheManager cacheManager;

    @Autowired
    @Qualifier("ThreadPoolTaskExecutor")
    private Executor threadPoolTaskExecutor;

    @Test
    @DisplayName("test profile can override CacheManager to JCache-backed caches using ehcache.xml")
    void canUseJCacheBackedCaches() {
        Assertions.assertThat(cacheManager).isNotNull();
        if (cacheManager instanceof TransactionAwareCacheManagerProxy proxy) {
            Object targetCacheManager = new DirectFieldAccessor(proxy)
                    .getPropertyValue("targetCacheManager");
            Assertions.assertThat(targetCacheManager)
                    .isInstanceOf(org.springframework.cache.jcache.JCacheCacheManager.class);
        } else {
            Assertions.assertThat(cacheManager)
                    .isInstanceOf(org.springframework.cache.jcache.JCacheCacheManager.class);
        }

        Cache listCache = cacheManager.getCache("sheetpostList");
        Cache detailCache = cacheManager.getCache("sheetpostDetail");
        Assertions.assertThat(listCache).isNotNull();
        Assertions.assertThat(detailCache).isNotNull();

        Object nativeListCache = listCache.getNativeCache();
        Assertions.assertThat(nativeListCache).isNotNull();
    }

    @Test
    @DisplayName("test profile provides ThreadPoolTaskExecutor for SWR refresh")
    void executorIsAvailableInTestProfile() {
        Assertions.assertThat(threadPoolTaskExecutor).isNotNull();
        Assertions.assertThat(threadPoolTaskExecutor)
                .isInstanceOf(ThreadPoolTaskExecutor.class);
    }

    @TestConfiguration
    static class JCacheOverrideConfig {

        @Bean(destroyMethod = "close")
        public CachingProvider jCacheProvider() {
            return Caching.getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider");
        }

        @Bean(destroyMethod = "close")
        public javax.cache.CacheManager jCacheManager(CachingProvider cachingProvider) throws Exception {
            URL configUrl = Thread.currentThread().getContextClassLoader().getResource("ehcache.xml");
            if (configUrl == null) {
                throw new IllegalStateException("ehcache.xml must exist on classpath");
            }
            URI configUri = configUrl.toURI();
            return cachingProvider.getCacheManager(configUri, Thread.currentThread().getContextClassLoader());
        }

        @Bean(name = "testCacheManager")
        @Primary
        public org.springframework.cache.CacheManager testCacheManager(javax.cache.CacheManager jCacheManager) {
            return new JCacheCacheManager(jCacheManager);
        }
    }
}
