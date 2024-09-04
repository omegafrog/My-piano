package com.omegafrog.My.piano.app.web.dto.sheetPost;

import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import lombok.*;
import org.hibernate.validator.constraints.Range;

import java.util.Arrays;

@Data
@Setter
@Getter
@NoArgsConstructor
public class RegisterSheetDto {
    private String title;
    @Range(min = 0, max = 4)
    private int difficulty;
    @Range(min = 0, max = 12)
    private int instrument;

    private Genres genres;
    private Boolean isSolo;
    private boolean lyrics;
    private String filePath;

    @Builder
    public RegisterSheetDto(String title, int difficulty, int instrument, Genres genres, boolean isSolo,
                            boolean lyrics, String filePath) {
        this.title = title;
        this.difficulty = difficulty;
        this.instrument = instrument;
        this.isSolo = isSolo;
        this.lyrics = lyrics;
        this.filePath = filePath;
    }

    public Sheet createEntity(User user, int pageNum) {
        return Sheet.builder()
                .title(title)
                .difficulty(Arrays.stream(Difficulty.values()).filter(iter -> iter.ordinal() == difficulty).findFirst().get())
                .instrument(Arrays.stream(Instrument.values()).filter(iter -> iter.ordinal() == instrument).findFirst().get())
                .genres(genres)
                .isSolo(isSolo)
                .lyrics(lyrics)
                .sheetUrl(filePath)
                .user(user)
                .pageNum(pageNum)
                .build();
    }

}
