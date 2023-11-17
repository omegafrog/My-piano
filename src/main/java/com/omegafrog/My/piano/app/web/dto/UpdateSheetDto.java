package com.omegafrog.My.piano.app.web.dto;

import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Genre;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@Setter
@Getter
public class UpdateSheetDto {
    private String title;
    private int pageNum;
    private Difficulty difficulty;
    private Instrument instrument;
    private Genres genres;
    private Boolean solo;
    private Boolean lyrics;
    private String filePath;
    private int price;

    @Builder
    public UpdateSheetDto(String title, int pageNum, Difficulty difficulty, Instrument instrument, Genres genres, boolean isSolo, boolean lyrics, String filePath, int price) {
        this.title = title;
        this.pageNum = pageNum;
        this.difficulty = difficulty;
        this.instrument = instrument;
        this.genres = genres;
        this.solo = isSolo;
        this.lyrics = lyrics;
        this.filePath = filePath;
        this.price = price;
    }

    public boolean isSolo() {
        return solo;
    }

    public boolean isLyrics() {
        return lyrics;
    }
}
