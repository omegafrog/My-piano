package com.omegafrog.My.piano.app.sheet.entity;

import com.omegafrog.My.piano.app.enums.Difficulty;
import com.omegafrog.My.piano.app.enums.Genre;
import com.omegafrog.My.piano.app.enums.Instrument;
import com.omegafrog.My.piano.app.sheet.dto.UpdateSheetDto;
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


