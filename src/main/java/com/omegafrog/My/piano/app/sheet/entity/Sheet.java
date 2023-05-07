package com.omegafrog.My.piano.app.sheet.entity;

import com.omegafrog.My.piano.app.enums.Difficulty;
import com.omegafrog.My.piano.app.enums.Genre;
import com.omegafrog.My.piano.app.enums.Instrument;
import com.omegafrog.My.piano.app.sheet.dto.UpdateSheetDto;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.User;

import javax.persistence.*;

@Entity
@NoArgsConstructor
public class Sheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int pageNum;
    private Difficulty difficulty;
    private Instrument instrument;
    private Genre genre;
    private boolean isSolo;
    private boolean lyrics;
    private String filePath;
    private int price;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "USER_ID")
    private User user;

    @Builder
    public Sheet(int pageNum, Difficulty difficulty, Instrument instrument, Genre genre, boolean isSolo, boolean lyrics, String filePath, int price, User user) {
        this.pageNum = pageNum;
        this.difficulty = difficulty;
        this.instrument = instrument;
        this.genre = genre;
        this.isSolo = isSolo;
        this.lyrics = lyrics;
        this.filePath = filePath;
        this.price = price;
        this.user = user;
    }

    public Sheet update(UpdateSheetDto dto){
        this.pageNum = dto.getPageNum();
        this.difficulty = dto.getDifficulty();
        this.instrument = dto.getInstrument();
        this.genre = dto.getGenre();
        this.isSolo = dto.isSolo();
        this.lyrics = dto.isLyrics();
        this.filePath = dto.getFilePath();
        this.price = dto.getPrice();
        return this;
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


