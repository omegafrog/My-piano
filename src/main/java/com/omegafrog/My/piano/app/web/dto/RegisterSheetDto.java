package com.omegafrog.My.piano.app.web.dto;

import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.validator.constraints.Range;

import java.lang.reflect.Array;
import java.util.Arrays;

@Data
@Setter
@Getter
@NoArgsConstructor
public class RegisterSheetDto {
    private String title;
    @Positive
    private int pageNum;
    @Range(min = 0, max = 4)
    private int difficulty;
    @Range(min=0,max=12)
    private int instrument;

    private Genres genres;
    private Boolean isSolo;
    private boolean lyrics;
    private String filePath;

    @Builder
    public RegisterSheetDto(String title, int pageNum, int difficulty, int instrument, Genres genres, boolean isSolo,
                            boolean lyrics, String filePath) {
        this.title = title;
        this.pageNum = pageNum;
        this.difficulty = difficulty;
        this.instrument = instrument;
        this.isSolo = isSolo;
        this.lyrics = lyrics;
        this.filePath = filePath;
    }

    public Sheet.SheetBuilder getEntityBuilderWithoutAuthor() {
        return Sheet.builder()
                .title(title)
                .pageNum(pageNum)
                .difficulty(Arrays.stream(Difficulty.values()).filter(iter -> iter.ordinal() == difficulty).findFirst().get())
                .instrument(Arrays.stream(Instrument.values()).filter(iter -> iter.ordinal() == instrument).findFirst().get())
                .genres(genres)
                .isSolo(isSolo)
                .lyrics(lyrics)
                .filePath(filePath);
    }

}
