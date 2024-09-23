package com.omegafrog.My.piano.app.web.domain.sheet;

public interface SheetPostLikeCountRepository {
    SheetPostLikeCount save(SheetPostLikeCount sheetPostLikeCount);

    SheetPostLikeCount findById(Long id);

    int incrementLikeCount(SheetPost sheetPost);

    boolean exist(Long id);

    void decrementLikeCount(SheetPost sheetPost);
}
