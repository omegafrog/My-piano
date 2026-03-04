package com.omegafrog.My.piano.app.web.service.cache;

import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;

import java.io.Serializable;

public record SheetPostDetailCachePayload(
        SheetPostDto baseDto,
        int initialViewCount
) implements Serializable {
}
