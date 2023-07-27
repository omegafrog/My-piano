package com.omegafrog.My.piano.app.web.dto.sheet;

import com.omegafrog.My.piano.app.web.dto.order.ItemDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Genre;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Setter
@Getter
@NoArgsConstructor
public class SheetDto {
    private Long id;
    private String title;
    private int pageNum;
    private Difficulty difficulty;
    private Instrument instrument;
    private Genre genre;
    private boolean isSolo;
    private boolean lyrics;
    private String filePath;
    private LocalDateTime createdAt;
    private UserProfile user;

    @Builder
    public SheetDto(Long id, String title, int pageNum, Difficulty difficulty, Instrument instrument, Genre genre, boolean isSolo, boolean lyrics, String filePath, LocalDateTime createdAt, UserProfile user) {
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
