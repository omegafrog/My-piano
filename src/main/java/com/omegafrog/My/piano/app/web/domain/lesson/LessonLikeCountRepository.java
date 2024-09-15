package com.omegafrog.My.piano.app.web.domain.lesson;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.article.LikeCount;
import com.omegafrog.My.piano.app.web.domain.article.LikeCountRepository;
import org.springframework.stereotype.Repository;

@Repository
public class LessonLikeCountRepository implements LikeCountRepository {
    @Override
    public int incrementLikeCount(Article article) {
        return 0;
    }

    @Override
    public int decrementLikeCount(Article article) {
        return 0;
    }

    @Override
    public LikeCount save(LikeCount likeCount) {
        return null;
    }

    @Override
    public LikeCount findById(Long articleId) {
        return null;
    }

    @Override
    public boolean exist(Long articleId) {
        return false;
    }
}