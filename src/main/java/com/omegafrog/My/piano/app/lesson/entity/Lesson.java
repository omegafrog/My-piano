package com.omegafrog.My.piano.app.lesson.entity;

import com.omegafrog.My.piano.app.lesson.dto.UpdateLessonDto;
import com.omegafrog.My.piano.app.order.entity.Item;
import com.omegafrog.My.piano.app.sheet.entity.Sheet;
import com.omegafrog.My.piano.app.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@NoArgsConstructor
public class Lesson extends Item {

    private String title;
    private String subTitle;

    private VideoInformation videoInformation;


    private int viewCount;
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "USER_ID")
    private User lessonProvider;
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "SHEET_ID")
    private Sheet sheet;

    private LessonInformation lessonInformation;

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
}
