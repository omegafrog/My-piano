package com.omegafrog.My.piano.app.web.service.cache;

import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostListDto;

import java.io.Serializable;
import java.util.List;

public record SheetPostListCachePayload(
        List<SheetPostListDto> items,
        long totalElements,
        String rawQuery
) implements Serializable {
}
