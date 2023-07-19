package com.omegafrog.My.piano.app.web.domain.lesson;

import com.omegafrog.My.piano.app.web.dto.UpdateLessonDto;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.order.Item;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
public class Lesson extends Item {

    @NotNull
    private String title;
    @NotNull
    private String subTitle;
    @NotNull
    private VideoInformation videoInformation;

    @NotNull
    private LessonInformation lessonInformation;

    private int viewCount;

    @OneToOne(cascade = { CascadeType.MERGE})
    @JoinColumn(name = "USER_ID")
    private User lessonProvider;

    @OneToOne(cascade = { CascadeType.MERGE})
    @JoinColumn(name = "SHEET_ID")
    private Sheet sheet;


    @Builder
    public Lesson(String title, String subTitle, int price, VideoInformation videoInformation,
                  User lessonProvider, Sheet sheet, LessonInformation lessonInformation) {
        super(price,LocalDateTime.now());
        this.title = title;
        this.subTitle = subTitle;
        this.videoInformation = videoInformation;
        this.lessonProvider = lessonProvider;
        this.sheet = sheet;
        this.lessonInformation = lessonInformation;
        viewCount=0;
    }

    public Lesson update(UpdateLessonDto dto){
        this.title = dto.getTitle();
        this.subTitle = dto.getSubTitle();
        this.updatePrice(dto.getPrice());
        this.videoInformation = dto.getVideoInformation();
        this.sheet = dto.getSheet();
        this.lessonInformation = dto.getLessonInformation();
        return this;
    }

    public LessonDto toDto(){
        return LessonDto.builder()
                .id(super.getId())
                .title(this.title)
                .sheet(this.sheet)
                .subTitle(this.subTitle)
                .lessonInformation(this.lessonInformation)
                .videoInformation(this.videoInformation)
                .lessonProvider(lessonProvider)
                .viewCount(viewCount)
                .build();
    }
}
