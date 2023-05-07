package com.omegafrog.My.piano.app.sheet.entity;

import com.omegafrog.My.piano.app.sheet.dto.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
public class SheetPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;
    private String title;
    LocalDateTime created_at;
    private int view;
    private String content;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "USER_ID")
    private User artist;

    @OneToOne(cascade = {CascadeType.MERGE,CascadeType.PERSIST})
    @JoinColumn(name = "SHEET_ID")
    private Sheet sheet;

    @Builder
    public SheetPost(String title, String content, User artist, Sheet sheet) {
        this.title = title;
        this.created_at = LocalDateTime.now();
        this.view=0;
        this.content = content;
        this.artist = artist;
        this.sheet = sheet;
    }

    public SheetPost update(UpdateSheetPostDto dto){
        this.title = dto.getTitle();
        this.sheet = dto.getSheet();
        this.content = dto.getContent();
        return this;
    }

}