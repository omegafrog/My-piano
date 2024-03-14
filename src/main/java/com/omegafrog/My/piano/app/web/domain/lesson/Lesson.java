package com.omegafrog.My.piano.app.web.domain.lesson;

import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.order.SellableItem;
import com.omegafrog.My.piano.app.web.dto.UpdateLessonDto;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
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
    @JoinColumn(name = "SHEET_ID")
    private Sheet sheet;


    @Builder
    public Lesson(String title, String subTitle, Integer price, VideoInformation videoInformation,
                  User lessonProvider, Sheet sheet, LessonInformation lessonInformation) {
        super(lessonProvider, title, subTitle, price);
        this.videoInformation = videoInformation;
        this.sheet = sheet;
        this.lessonInformation = lessonInformation;
    }

    public Lesson update(UpdateLessonDto dto, Sheet sheet){
        title = dto.getTitle();
        content = dto.getSubTitle();
        this.updatePrice(dto.getPrice());
        this.videoInformation = dto.getVideoInformation();
        this.sheet = sheet;
        this.lessonInformation = dto.getLessonInformation();
        return this;
    }

    public LessonDto toDto(){
        return LessonDto.builder()
                .id(id)
                .title(title)
                .sheet(this.sheet.toSheetDto())
                .subTitle(content)
                .lessonInformation(this.lessonInformation)
                .videoInformation(this.videoInformation)
                .artist(author.getUserProfile())
                .viewCount(viewCount)
                .likeCount(likeCount)
                .createdAt(createdAt)
                .comments(comments.stream().map(Comment::toDto).toList())
                .price(price)
                .build();
    }

}
