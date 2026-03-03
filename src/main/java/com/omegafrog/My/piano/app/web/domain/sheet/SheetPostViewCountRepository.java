package com.omegafrog.My.piano.app.web.domain.sheet;

import java.util.List;
import java.util.Map;

public interface SheetPostViewCountRepository{

    default int incrementViewCount(SheetPost sheetPost) {
        return incrementViewCount(sheetPost.getId(), sheetPost.getViewCount());
    }

    int incrementViewCount(Long sheetPostId, int initialViewCount);
    SheetPostViewCount findById(Long id);
    boolean exist(Long id);

    SheetPostViewCount save(SheetPostViewCount sheetPostViewCount);
    
    Map<Long, Integer> getViewCountsByIds(List<Long> ids);

}
