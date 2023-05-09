package com.omegafrog.My.piano.app.sheet.dto;

import com.omegafrog.My.piano.app.enums.Difficulty;
import com.omegafrog.My.piano.app.enums.Genre;
import com.omegafrog.My.piano.app.enums.Instrument;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
public class UpdateSheetDto {
    private int pageNum;
    private Difficulty difficulty;
    private Instrument instrument;
    private Genre genre;
    private boolean solo;
    private boolean lyrics;
    private String filePath;
    private int price;

    @Builder
    public UpdateSheetDto(int pageNum, Difficulty difficulty, Instrument instrument, Genre genre, boolean isSolo, boolean lyrics, String filePath, int price) {
        this.pageNum = pageNum;
        this.difficulty = difficulty;
        this.instrument = instrument;
        this.genre = genre;
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
