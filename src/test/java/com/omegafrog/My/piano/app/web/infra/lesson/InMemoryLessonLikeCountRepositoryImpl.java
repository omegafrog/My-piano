package com.omegafrog.My.piano.app.web.infra.lesson;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.omegafrog.My.piano.app.TestResettable;
import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.article.LikeCount;
import com.omegafrog.My.piano.app.web.domain.article.LikeCountRepository;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonLikeCount;

@Repository
@Profile("test")
@Qualifier("LessonLikeCountRepositoryImpl")
public class InMemoryLessonLikeCountRepositoryImpl implements LikeCountRepository, TestResettable {

    private final Map<Long, Integer> counts = new ConcurrentHashMap<>();

    @Override
    public int incrementLikeCount(Article article) {
        return counts.merge(article.getId(), article.getLikeCount() + 1, (prev, init) -> prev + 1);
    }

    @Override
    public int decrementLikeCount(Article article) {
        Integer current = counts.get(article.getId());
        int next = (current == null ? article.getLikeCount() : current) - 1;
        counts.put(article.getId(), next);
        return next;
    }

    @Override
    public LikeCount save(LikeCount likeCount) {
        counts.put(likeCount.getId(), likeCount.getLikeCount());
        return likeCount;
    }

    @Override
    public LikeCount findById(Long articleId) {
        int count = counts.getOrDefault(articleId, 0);
        return LessonLikeCount.builder().id(articleId).likeCount(count).build();
    }

    @Override
    public boolean exist(Long articleId) {
        return counts.containsKey(articleId);
    }

    @Override
    public void reset() {
        counts.clear();
    }
}
