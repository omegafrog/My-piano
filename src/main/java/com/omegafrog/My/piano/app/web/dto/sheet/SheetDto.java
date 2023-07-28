package com.omegafrog.My.piano.app.web.dto.sheet;

import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Genre;
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
    private Genre genre;
    @NotNull
    private Boolean isSolo;
    @NotNull
    private Boolean lyrics;
    @NotNull
    private String filePath;
    @NotNull
    private LocalDateTime createdAt;
    @NotNull
    private UserProfile user;

    @Builder
    public SheetDto(Long id, String title, int pageNum, Difficulty difficulty, Instrument instrument, Genre genre, Boolean isSolo, Boolean lyrics, String filePath, LocalDateTime createdAt, UserProfile user) {
        this.id = id;
        this.title = title;
        this.pageNum = pageNum;
        this.difficulty = difficulty;
        this.instrument = instrument;
        this.genre = genre;
        this.isSolo = isSolo;
        this.lyrics = lyrics;
        this.filePath = filePath;
        this.createdAt = createdAt;
        this.user = user;
    }
}
