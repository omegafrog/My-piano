package com.omegafrog.My.piano.app.web.dto.sheetPost;

import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Instrument;

import java.time.LocalDateTime;

public record SheetPostListDto(Long id, String title, String artistName,
                               String artistProfile, String sheetTitle,
                               Difficulty difficulty,
                               Genres genres,
                               Instrument instrument, LocalDateTime createdAt,
                               Integer price) {
}
