package com.omegafrog.My.piano.app.web.infra.lesson;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.article.LikeCount;
import com.omegafrog.My.piano.app.web.domain.article.LikeCountRepository;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonLikeCount;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
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
@RequiredArgsConstructor
@Qualifier("LessonLikeCountRepositoryImpl")
public class LessonLikeCountRepositoryImpl implements LikeCountRepository {

    private static final String CACHE_NAME = "lessonLikeCounts";
    private final CacheManager cacheManager;
    private final LessonRepository lessonRepository;

    private Cache<Long, Integer> cache() {
        return cacheManager.getCache(CACHE_NAME, Long.class, Integer.class);
    }

    @Override
    public int incrementLikeCount(Article article) {
        Integer updated = cache().invoke(article.getId(), (EntryProcessor<Long, Integer, Integer>)
                (MutableEntry<Long, Integer> entry, Object... arguments) -> {
                    int initial = (Integer) arguments[0];
                    int current = entry.exists() ? entry.getValue() : initial;
                    int next = current + 1;
                    entry.setValue(next);
                    return next;
                }, article.getLikeCount());
        return updated == null ? article.getLikeCount() + 1 : updated;
    }

    @Override
    public int decrementLikeCount(Article article) {
        Integer updated = cache().invoke(article.getId(), (EntryProcessor<Long, Integer, Integer>)
                (MutableEntry<Long, Integer> entry, Object... arguments) -> {
                    int initial = (Integer) arguments[0];
                    int current = entry.exists() ? entry.getValue() : initial;
                    int next = Math.max(0, current - 1);
                    entry.setValue(next);
                    return next;
                }, article.getLikeCount());
        return updated == null ? Math.max(0, article.getLikeCount() - 1) : updated;
    }

    @Override
    public LikeCount save(LikeCount likeCount) {
        cache().put(likeCount.getId(), likeCount.getLikeCount());
        return likeCount;
    }

    @Override
    public LikeCount findById(Long articleId) {
        Integer cached = cache().get(articleId);
        if (cached != null) {
            return new LessonLikeCount(articleId, cached);
        }

        Lesson lesson = lessonRepository.findById(articleId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson."));
        int current = lesson.getLikeCount();
        cache().put(articleId, current);
        return new LessonLikeCount(articleId, current);
    }

    @Override
    public boolean exist(Long articleId) {
        return cache().containsKey(articleId);
    }
}
