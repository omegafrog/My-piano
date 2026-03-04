package com.omegafrog.My.piano.app.batch;

import com.omegafrog.My.piano.app.web.domain.article.LikeCount;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonLikeCount;
import com.omegafrog.My.piano.app.web.domain.post.PostLikeCount;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostLikeCount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.database.AbstractPagingItemReader;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.lang.reflect.Constructor;
import java.util.*;

@Slf4j
public class LikeCountPagingItemReader<T extends LikeCount> extends AbstractPagingItemReader<LikeCount> {
    private static final Map<Class<?>, String> CACHE_NAMES = Map.of(
            SheetPostLikeCount.class, "sheetPostLikeCounts",
            LessonLikeCount.class, "lessonLikeCounts",
            PostLikeCount.class, "postLikeCounts"
    );

    private final Class<T> targetType;
    private final CacheManager cacheManager;
    private final List<LikeCount> allEntries = new ArrayList<>();

    public LikeCountPagingItemReader(Class<T> targetType, CacheManager cacheManager) {
        this.targetType = targetType;
        this.cacheManager = cacheManager;
    }

    @Override
    protected void doOpen() throws Exception {
        super.doOpen();
        long start = System.currentTimeMillis();
        allEntries.clear();

        String cacheName = Optional.ofNullable(CACHE_NAMES.get(targetType))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported type: " + targetType));
        Cache<Long, Integer> cache = cacheManager.getCache(cacheName, Long.class, Integer.class);
        Constructor<T> constructor = targetType.getConstructor(Long.class, int.class);

        if (cache != null) {
            for (Cache.Entry<Long, Integer> entry : cache) {
                if (entry == null || entry.getKey() == null || entry.getValue() == null) {
                    continue;
                }
                int value = entry.getValue();
                if (value > 0) {
                    allEntries.add(constructor.newInstance(entry.getKey(), value));
                }
            }
        }

        allEntries.sort(Comparator.comparingLong(item -> item.getId()));
        long end = System.currentTimeMillis();
        log.info("LikeCountPagingItemReader opened - Type: {}, Total entries: {}, Time: {}ms",
                targetType.getSimpleName(), allEntries.size(), end - start);
    }

    @Override
    protected void doClose() throws Exception {
        super.doClose();
        log.info("LikeCountPagingItemReader closed - Type: {}, Total entries: {}",
                targetType.getSimpleName(), allEntries.size());
        allEntries.clear();
    }

    @Override
    protected void doReadPage() {
        if (results == null) {
            results = new ArrayList<>();
        } else {
            results.clear();
        }

        int start = getPage() * getPageSize();
        int end = Math.min(allEntries.size(), (getPage() + 1) * getPageSize());
        if (start >= allEntries.size()) {
            return;
        }
        results.addAll(allEntries.subList(start, end));
    }
}
