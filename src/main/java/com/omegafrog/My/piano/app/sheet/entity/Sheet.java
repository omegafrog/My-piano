package com.omegafrog.My.piano.app.sheet.entity;

import com.omegafrog.My.piano.app.enums.Difficulty;
import com.omegafrog.My.piano.app.enums.Genre;
import com.omegafrog.My.piano.app.enums.Instrument;
import com.omegafrog.My.piano.app.order.entity.Item;
import com.omegafrog.My.piano.app.sheet.dto.UpdateSheetDto;
import com.omegafrog.My.piano.app.user.entity.User;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
public class Sheet extends Item {

    private int pageNum;
    private Difficulty difficulty;
    private Instrument instrument;
    private Genre genre;
    private boolean isSolo;
    private boolean lyrics;
    private String filePath;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "USER_ID")
    private User user;

    @Builder
    public Sheet(int pageNum, Difficulty difficulty, Instrument instrument, Genre genre, boolean isSolo, boolean lyrics, String filePath, int price, User user) {
        super(LocalDateTime.now());
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


}


