package com.omegafrog.My.piano.app.cache;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@ConditionalOnBean(CacheManager.class)
@RequiredArgsConstructor
@Slf4j
public class EhcacheMetricsBinder {
    private final CacheManager cacheManager;
    private final MeterRegistry meterRegistry;

    private final Map<String, Integer> cacheCapacities = Map.of(
            "sheetpostList", 2000,
            "sheetpostDetail", 10000,
            "sheetPostViewCounts", 200000,
            "sheetPostLikeCounts", 200000,
            "postViewCounts", 200000,
            "postLikeCounts", 200000,
            "lessonViewCounts", 200000,
            "lessonLikeCounts", 200000
    );

    private final Map<String, AtomicLong> entryCountByCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void bindMeters() {
        for (Map.Entry<String, Integer> entry : cacheCapacities.entrySet()) {
            String cacheName = entry.getKey();
            int capacity = entry.getValue();
            AtomicLong countHolder = entryCountByCache.computeIfAbsent(cacheName, key -> new AtomicLong(0L));

            Gauge.builder("cache.entries", countHolder, AtomicLong::get)
                    .tag("cache", cacheName)
                    .register(meterRegistry);
            Gauge.builder("cache.capacity", () -> capacity)
                    .tag("cache", cacheName)
                    .register(meterRegistry);
            Gauge.builder("cache.occupancy.ratio", countHolder,
                            c -> capacity == 0 ? 0.0 : c.get() / (double) capacity)
                    .tag("cache", cacheName)
                    .register(meterRegistry);
        }

        refreshEntryCounts();
    }

    @Scheduled(fixedDelayString = "${cache.metrics.refresh-ms:30000}")
    public void refreshEntryCounts() {
        for (String cacheName : cacheCapacities.keySet()) {
            Cache<Object, Object> cache = cacheManager.getCache(cacheName, Object.class, Object.class);
            if (cache == null) {
                continue;
            }
            long count = 0L;
            for (Cache.Entry<Object, Object> ignored : cache) {
                count++;
            }
            entryCountByCache.computeIfAbsent(cacheName, key -> new AtomicLong(0L)).set(count);
        }
    }
}
