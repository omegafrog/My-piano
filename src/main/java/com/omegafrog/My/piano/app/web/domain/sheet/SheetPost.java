package com.omegafrog.My.piano.app.web.domain.sheet;

import com.omegafrog.My.piano.app.web.domain.order.Item;
import com.omegafrog.My.piano.app.web.dto.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
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
    @Getter
    private Long id;
    private String title;
    private LocalDateTime createdAt;
    private int view;
    private String content;

    @OneToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "USER_ID")
    private User artist;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
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

}