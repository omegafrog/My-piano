package com.omegafrog.My.piano.app.web.domain.lesson;

import com.omegafrog.My.piano.app.web.domain.order.SellableItem;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.dto.lesson.UpdateLessonDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@NoArgsConstructor
@Getter
public class Lesson extends SellableItem {
    @NotNull
    private VideoInformation videoInformation;

    @NotNull
    private LessonInformation lessonInformation;

    @OneToOne(cascade = { CascadeType.MERGE})
    @JoinColumn(name = "SHEET_POST_ID")
    private SheetPost sheetPost;

    @Builder
    public Lesson(String title, String subTitle, Integer price, VideoInformation videoInformation,
                  User lessonProvider, SheetPost sheetPost, LessonInformation lessonInformation) {
        super(lessonProvider, title, subTitle, price);
        this.videoInformation = videoInformation;
        this.sheetPost = sheetPost;
        this.lessonInformation = lessonInformation;
    }

    public Lesson update(UpdateLessonDto dto, SheetPost sheetPost ){
        title = dto.getTitle();
        content = dto.getSubTitle();
        this.updatePrice(dto.getPrice());
        this.videoInformation = dto.getVideoInformation();
        this.sheetPost = sheetPost;
        this.lessonInformation = dto.getLessonInformation();
        return this;
    }
    public LessonDto toDto(){ return LessonDto.builder()
            .id(id)
            .title(title)
            .sheet(sheetPost.getSheet().toSheetDto())
            .subTitle(content)
            .lessonInformation(lessonInformation)
            .videoInformation(videoInformation)
            .sheetPostId(sheetPost.getId())
            .artist(author.getUserInfo())
            .viewCount(viewCount)
            .likeCount(likeCount)
            .createdAt(createdAt)
            .price(price)
            .build();
    }
}
