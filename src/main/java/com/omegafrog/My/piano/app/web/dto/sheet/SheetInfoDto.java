package com.omegafrog.My.piano.app.web.dto.sheet;

import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.dto.order.SellableItemDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Setter
@Getter
@NoArgsConstructor
public class SheetInfoDto extends SellableItemDto {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private UserProfile artist;
    private Genres genres;
    private int pageNum;
    private Difficulty difficulty;
    private Instrument instrument;
    private boolean isSolo;
    private boolean lyrics;
    private String sheetUrl;

    @Builder
    public SheetInfoDto(Long id, String title, String content,LocalDateTime createdAt, UserProfile artist,
                        Genres genres, int pageNum, Difficulty difficulty, Instrument instrument, boolean isSolo,
                        boolean lyrics, String sheetUrl) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.artist = artist;
        this.genres = genres;
        this.pageNum = pageNum;
        this.difficulty = difficulty;
        this.instrument = instrument;
        this.isSolo = isSolo;
        this.lyrics = lyrics;
        this.sheetUrl = sheetUrl;
        this.content = content;
    }
}
