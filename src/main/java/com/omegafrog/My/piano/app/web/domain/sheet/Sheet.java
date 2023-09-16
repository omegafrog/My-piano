package com.omegafrog.My.piano.app.web.domain.sheet;

import com.omegafrog.My.piano.app.web.dto.sheet.SheetDto;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Genre;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import com.omegafrog.My.piano.app.web.dto.UpdateSheetDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;

@Entity
@NoArgsConstructor
@Getter
public class Sheet  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private int pageNum;
    private Difficulty difficulty;
    private Instrument instrument;
    private Genre genre1,genre2;
    private boolean isSolo;
    private boolean lyrics;
    private String filePath;

    private LocalDateTime createdAt=LocalDateTime.now();

    @OneToOne(cascade = { CascadeType.MERGE})
    @JoinColumn(name = "CREATOR_ID")
    private User user;

    @Builder
    public Sheet(String title, int pageNum, Difficulty difficulty, Instrument instrument,Genre genre1,Genre genre2 , boolean isSolo, boolean lyrics, String filePath, User user) {
        this.title = title;
        this.pageNum = pageNum;
        this.difficulty = difficulty;
        this.instrument = instrument;
        this.genre1 = genre1;
        this.genre2 = genre2;
        this.isSolo = isSolo;
        this.lyrics = lyrics;
        this.filePath = filePath;
        this.user = user;
    }

    public Sheet update(UpdateSheetDto dto){
        this.title = dto.getTitle();
        this.pageNum = dto.getPageNum();
        this.difficulty = dto.getDifficulty();
        this.instrument = dto.getInstrument();
        this.genre1 = dto.getGenre1();
        this.genre2 = dto.getGenre2();
        this.isSolo = dto.isSolo();
        this.lyrics = dto.isLyrics();
        this.filePath = dto.getFilePath();
        return this;
    }

    public SheetDto toSheetDto(){
        return SheetDto.builder()
                .id(id)
                .createdAt(createdAt)
                .difficulty(difficulty)
                .genres(Arrays.asList(genre1, genre2))
                .filePath(filePath)
                .instrument(instrument)
                .lyrics(lyrics)
                .pageNum(pageNum)
                .isSolo(isSolo)
                .title(title)
                .user(user.getUserProfile())
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sheet sheet = (Sheet) o;

        return id.equals(sheet.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}


