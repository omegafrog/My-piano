package com.omegafrog.My.piano.app.web.dto.sheetPost;

import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.dto.user.UserInfo;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Setter
@Getter
@NoArgsConstructor
public class SheetDto {
    private Long id;
    @NotNull
    private String title;
    @PositiveOrZero
    private int pageNum;
    @NotNull
    private Difficulty difficulty;
    @NotNull
    private Instrument instrument;
    @NotNull
    private Genres genres;
    @NotNull
    private Boolean isSolo;
    @NotNull
    private Boolean lyrics;
    @NotNull
    private String sheetUrl;
    @NotNull
    private LocalDateTime createdAt;
    @NotNull
    private UserInfo user;
    private String originalFileName;


    @Builder
    public SheetDto(Long id, String title, int pageNum, Difficulty difficulty, Genres genres, Instrument instrument,
                    Boolean isSolo, Boolean lyrics, String sheetUrl, LocalDateTime createdAt, UserInfo user, String originalFileName) {
        this.id = id;
        this.title = title;
        this.pageNum = pageNum;
        this.difficulty = difficulty;
        this.instrument = instrument;
        this.isSolo = isSolo;
        this.genres = genres;
        this.lyrics = lyrics;
        this.sheetUrl = sheetUrl;
        this.createdAt = createdAt;
        this.user = user;
        this.originalFileName = originalFileName;
    }
}
