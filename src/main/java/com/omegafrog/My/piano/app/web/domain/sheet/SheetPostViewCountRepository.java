package com.omegafrog.My.piano.app.web.domain.sheet;


public interface SheetPostViewCountRepository{

    int incrementViewCount(SheetPost sheetPost);
    SheetPostViewCount findById(Long id);
    boolean exist(Long id);

    SheetPostViewCount save(SheetPostViewCount sheetPostViewCount);

}
