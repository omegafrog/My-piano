package com.omegafrog.My.piano.app.web.domain.article;

public interface LikeCountRepository {
    int incrementLikeCount(Article article);

    int decrementLikeCount(Article article);

    LikeCount save(LikeCount likeCount);

    LikeCount findById(Long articleId);


    boolean exist(Long articleId);
}
