package com.omegafrog.My.piano.app.web.infra.sheetPost;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.article.LikeCount;
import com.omegafrog.My.piano.app.web.domain.article.LikeCountRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostLikeCount;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.MutableEntry;

@Repository
@Profile("!test")
@Qualifier("SheetPostLikeCountRepository")
@RequiredArgsConstructor
public class SheetPostLikeCountRepositoryImpl implements LikeCountRepository {
    private static final String CACHE_NAME = "sheetPostLikeCounts";
    private final CacheManager cacheManager;
    private final JpaSheetPostRepositoryImpl sheetPostRepository;

    private Cache<Long, Integer> cache() {
        return cacheManager.getCache(CACHE_NAME, Long.class, Integer.class);
    }

    @Override
    public LikeCount save(LikeCount sheetPostLikeCount) {
        cache().put(sheetPostLikeCount.getId(), sheetPostLikeCount.getLikeCount());
        return sheetPostLikeCount;
    }

    @Override
    public SheetPostLikeCount findById(Long id) {
        Integer cached = cache().get(id);
        if (cached != null) {
            return new SheetPostLikeCount(id, cached);
        }

        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post."));
        int current = sheetPost.getLikeCount();
        cache().put(id, current);
        return new SheetPostLikeCount(id, current);
    }


    @Override
    public int incrementLikeCount(Article sheetPost) {
        Integer updated = cache().invoke(sheetPost.getId(), (EntryProcessor<Long, Integer, Integer>)
                (MutableEntry<Long, Integer> entry, Object... arguments) -> {
                    int initial = (Integer) arguments[0];
                    int current = entry.exists() ? entry.getValue() : initial;
                    int next = current + 1;
                    entry.setValue(next);
                    return next;
                }, sheetPost.getLikeCount());
        return updated == null ? sheetPost.getLikeCount() + 1 : updated;
    }

    @Override
    public boolean exist(Long id) {
        return cache().containsKey(id);
    }

    @Override
    public int decrementLikeCount(Article sheetPost) {
        Integer updated = cache().invoke(sheetPost.getId(), (EntryProcessor<Long, Integer, Integer>)
                (MutableEntry<Long, Integer> entry, Object... arguments) -> {
                    int initial = (Integer) arguments[0];
                    int current = entry.exists() ? entry.getValue() : initial;
                    int next = Math.max(0, current - 1);
                    entry.setValue(next);
                    return next;
                }, sheetPost.getLikeCount());
        return updated == null ? Math.max(0, sheetPost.getLikeCount() - 1) : updated;
    }
}
