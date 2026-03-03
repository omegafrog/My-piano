package com.omegafrog.My.piano.app.cache;

import java.net.URI;
import java.net.URL;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class JCacheTestConfig {

    @Bean(destroyMethod = "close")
    public CachingProvider jCacheProvider() {
        return Caching.getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider");
    }

    @Bean(destroyMethod = "close")
    public CacheManager jCacheManager(CachingProvider cachingProvider) throws Exception {
        URL configUrl = Thread.currentThread().getContextClassLoader().getResource("ehcache.xml");
        if (configUrl == null) {
            throw new IllegalStateException("ehcache.xml must exist on classpath");
        }
        URI configUri = configUrl.toURI();
        return cachingProvider.getCacheManager(configUri, Thread.currentThread().getContextClassLoader());
    }

    @Bean(name = "testCacheManager")
    @Primary
    public org.springframework.cache.CacheManager testCacheManager(CacheManager jCacheManager) {
        return new JCacheCacheManager(jCacheManager);
    }
}
