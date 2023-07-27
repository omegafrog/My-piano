package com.omegafrog.My.piano.app.web.domain.sheet;

import com.omegafrog.My.piano.app.web.domain.order.Item;
import com.omegafrog.My.piano.app.web.dto.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.sheet.SheetInfoDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
public class SheetPost extends Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String title;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt=LocalDateTime.now();
    @PositiveOrZero
    private int view;

    @NotNull
    private String content;

    
    @OneToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "USER_ID")
    private User artist;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "SHEET_ID")
    private Sheet sheet;

    @Builder
    public SheetPost(String title, String content, User artist, Sheet sheet, int price) {
        super(price, LocalDateTime.now());
        this.title = title;
        this.view = 0;
        this.content = content;
        this.artist = artist;
        this.sheet = sheet;
    }

    public SheetPost update(UpdateSheetPostDto dto) {
        super.updatePrice(dto.getPrice());
        super.updateDiscountRate(dto.getDiscountRate());
        this.title = dto.getTitle();
        this.sheet = dto.getSheet();
        this.content = dto.getContent();

        return this;
    }

    public SheetInfoDto toInfoDto(){
        return SheetInfoDto.builder()
                .id(id)
                .sheetUrl(sheet.getFilePath())
                .artist(artist.getUserProfile())
                .createdAt(createdAt)
                .build();
    }
}