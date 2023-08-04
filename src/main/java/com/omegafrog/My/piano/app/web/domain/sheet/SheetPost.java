package com.omegafrog.My.piano.app.web.domain.sheet;

import com.omegafrog.My.piano.app.web.domain.article.Comment;
import com.omegafrog.My.piano.app.web.domain.order.SellableItem;
import com.omegafrog.My.piano.app.web.dto.UpdateSheetDto;
import com.omegafrog.My.piano.app.web.dto.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.sheet.SheetInfoDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
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
        super.updatePrice(dto.getPrice()==null?price:dto.getPrice());
        super.updateDiscountRate(dto.getDiscountRate()==null?discountRate:dto.getDiscountRate());
        this.title = dto.getTitle()==null?title:dto.getTitle();
        if(dto.getSheetDto()!=null){
            UpdateSheetDto sheetDto = dto.getSheetDto();
            this.sheet = Sheet.builder()
                    .title(sheetDto.getTitle())
                    .genre(sheetDto.getGenre())
                    .filePath(sheetDto.getFilePath())
                    .pageNum(sheetDto.getPageNum())
                    .difficulty(sheetDto.getDifficulty())
                    .instrument(sheetDto.getInstrument())
                    .isSolo(sheetDto.getSolo())
                    .lyrics(sheetDto.getLyrics())
                    .user(author)
                    .build();
        }
        this.content = dto.getContent()==null?content:dto.getContent();
        return this;
    }

    public SheetPostDto toDto(){
        return SheetPostDto.builder()
                .id(id)
                .title(title)
                .content(content)
                .likeCount(likeCount)
                .viewCount(viewCount)
                .discountRate(getDiscountRate())
                .comments(comments.stream().map(Comment::toDto).toList())
                .author(author.getUserProfile())
                .createdAt(createdAt)
                .sheet(toInfoDto())
                .price(getPrice())
                .build();
    }

    public SheetInfoDto toInfoDto(){
        return SheetInfoDto.builder()
                .id(id)
                .title(sheet.getTitle())
                .sheetUrl(sheet.getFilePath())
                .artist(author.getUserProfile())
                .createdAt(createdAt)
                .build();
    }
}