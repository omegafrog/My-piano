package com.omegafrog.My.piano.app.web.domain.sheet;

import com.omegafrog.My.piano.app.web.domain.order.SellableItem;
import com.omegafrog.My.piano.app.web.dto.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.sheet.SheetInfoDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@NoArgsConstructor
@Getter
public class SheetPost extends SellableItem {

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "SHEET_ID")
    private Sheet sheet;

    @Builder
    public SheetPost(String title, String content, User artist, Sheet sheet, int price) {
        super(artist, title, content, price);
        this.title = title;
        this.content = content;
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
                .artist(author.getUserProfile())
                .createdAt(createdAt)
                .build();
    }
}