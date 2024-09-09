package com.omegafrog.My.piano.app.web.domain.sheet;

import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.UpdateSheetDto;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
public class Sheet implements Serializable {
    private final static long serialVersionUID = 3998165768159256704L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private int pageNum;
    private Difficulty difficulty;
    private Instrument instrument;
    @Embedded
    private Genres genres;
    private boolean isSolo;
    private boolean lyrics;
    private String sheetUrl;
    private String originalFileName;

    @OneToOne(mappedBy = "sheet")
    @Setter
    private SheetPost sheetPost;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "CREATOR_ID")
    private User user;

    @Builder
    public Sheet(String title,
                 int pageNum,
                 Difficulty difficulty,
                 Instrument instrument,
                 Genres genres,
                 boolean isSolo,
                 boolean lyrics,
                 String sheetUrl,
                 User user,
                 String originalFileName,
                 SheetPost sheetPost) {
        this.title = title;
        this.pageNum = pageNum;
        this.difficulty = difficulty;
        this.instrument = instrument;
        this.genres = genres;
        this.isSolo = isSolo;
        this.lyrics = lyrics;
        this.sheetUrl = sheetUrl;
        this.user = user;
        this.originalFileName = originalFileName;
        this.sheetPost = sheetPost;
    }

    public Sheet update(UpdateSheetDto dto) {
        this.title = dto.getTitle();
        this.pageNum = dto.getPageNum();
        this.difficulty = dto.getDifficulty();
        this.instrument = dto.getInstrument();
        this.genres = dto.getGenres();
        this.isSolo = dto.isSolo();
        this.lyrics = dto.isLyrics();
        this.sheetUrl = dto.getSheetUrl();
        this.originalFileName = dto.getOriginalFileName();
        return this;
    }

    public SheetDto toSheetDto() {
        return SheetDto.builder()
                .id(id)
                .createdAt(createdAt)
                .difficulty(difficulty)
                .genres(genres)
                .sheetUrl(sheetUrl)
                .originalFileName(originalFileName)
                .instrument(instrument)
                .lyrics(lyrics)
                .pageNum(pageNum)
                .isSolo(isSolo)
                .title(title)
                .user(user.getUserInfo())
                .originalFileName(originalFileName)
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


