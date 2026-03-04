package com.omegafrog.My.piano.app.web.infra.sheetPost;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCount;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.MutableEntry;

@Repository
@Profile("!test")
@RequiredArgsConstructor
public class SheetPostViewCountRepositoryImpl implements SheetPostViewCountRepository {

    private static final String CACHE_NAME = "sheetPostViewCounts";
    private final CacheManager cacheManager;
    private final JpaSheetPostRepositoryImpl sheetPostRepository;

    private Cache<Long, Integer> cache() {
        return cacheManager.getCache(CACHE_NAME, Long.class, Integer.class);
    }

    @Override
    public int incrementViewCount(Long sheetPostId, int initialViewCount) {
        Integer updated = cache().invoke(sheetPostId, (EntryProcessor<Long, Integer, Integer>)
                (MutableEntry<Long, Integer> entry, Object... arguments) -> {
                    int initial = (Integer) arguments[0];
                    int current = entry.exists() ? entry.getValue() : initial;
                    int next = current + 1;
                    entry.setValue(next);
                    return next;
                }, initialViewCount);
        return updated == null ? initialViewCount + 1 : updated;
    }

    @Override
    public SheetPostViewCount findById(Long id) {
        Integer value = cache().get(id);
        if (value != null) {
            return SheetPostViewCount.builder().id(id).viewCount(value).build();
        }

        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + id));
        int current = sheetPost.getViewCount();
        cache().put(id, current);
        return SheetPostViewCount.builder().id(id).viewCount(current).build();
    }

    @Override
    public boolean exist(Long id) {
        return cache().containsKey(id);
    }

    @Override
    public SheetPostViewCount save(SheetPostViewCount viewCount) {
        cache().put(viewCount.getId(), viewCount.getViewCount());
        return viewCount;
    }

    @Override
    public Map<Long, Integer> getViewCountsByIds(List<Long> ids) {
        Map<Long, Integer> viewCounts = new HashMap<>();
        List<Long> misses = new ArrayList<>();

        for (Long id : ids) {
            Integer cached = cache().get(id);
            if (cached == null) {
                misses.add(id);
            } else {
                viewCounts.put(id, cached);
            }
        }

        if (!misses.isEmpty()) {
            List<SheetPost> sheetPosts = sheetPostRepository.findAllById(misses);
            for (SheetPost sheetPost : sheetPosts) {
                int count = sheetPost.getViewCount();
                viewCounts.put(sheetPost.getId(), count);
                cache().put(sheetPost.getId(), count);
            }
            for (Long miss : misses) {
                viewCounts.putIfAbsent(miss, 0);
            }
        }

        return viewCounts;
    }
}
