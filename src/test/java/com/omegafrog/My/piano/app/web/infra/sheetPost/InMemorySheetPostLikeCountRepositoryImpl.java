package com.omegafrog.My.piano.app.web.infra.sheetPost;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.omegafrog.My.piano.app.TestResettable;
import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.article.LikeCount;
import com.omegafrog.My.piano.app.web.domain.article.LikeCountRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostLikeCount;

@Repository
@Profile("test")
@Qualifier("SheetPostLikeCountRepository")
public class InMemorySheetPostLikeCountRepositoryImpl implements LikeCountRepository, TestResettable {

    private final Map<Long, Integer> counts = new ConcurrentHashMap<>();

    @Override
    public LikeCount save(LikeCount likeCount) {
        counts.put(likeCount.getId(), likeCount.getLikeCount());
        return likeCount;
    }

    @Override
    public SheetPostLikeCount findById(Long id) {
        int count = counts.getOrDefault(id, 0);
        return SheetPostLikeCount.builder().id(id).likeCount(count).build();
    }

    @Override
    public int incrementLikeCount(Article article) {
        return counts.merge(article.getId(), article.getLikeCount() + 1, (prev, init) -> prev + 1);
    }

    @Override
    public boolean exist(Long id) {
        return counts.containsKey(id);
    }

    @Override
    public int decrementLikeCount(Article article) {
        Integer current = counts.get(article.getId());
        int next = (current == null ? article.getLikeCount() : current) - 1;
        counts.put(article.getId(), next);
        return next;
    }

    @Override
    public void reset() {
        counts.clear();
    }
}
