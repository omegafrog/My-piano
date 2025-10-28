package com.omegafrog.My.piano.app.web.dto.sheetPost;

import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import lombok.*;

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
    private String sheetUrl;
    private String thumbnailUrl;
    private int price;
    private String originalFileName;

    @Builder
    public UpdateSheetDto(String title,
            int pageNum,
            Difficulty difficulty,
            Instrument instrument,
            Genres genres,
            boolean isSolo,
            boolean lyrics,
            String sheetUrl,
            String thumbnailUrl,
            int price,
            String originalFileName) {
        this.title = title;
        this.pageNum = pageNum;
        this.difficulty = difficulty;
        this.instrument = instrument;
        this.genres = genres;
        this.solo = isSolo;
        this.lyrics = lyrics;
        this.sheetUrl = sheetUrl;
        this.price = price;
        this.originalFileName = originalFileName;
        this.thumbnailUrl = thumbnailUrl;
    }

    public boolean isSolo() {
        return solo;
    }

    public boolean isLyrics() {
        return lyrics;
    }
}
