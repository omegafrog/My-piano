package com.omegafrog.My.piano.app.lesson.entity;

import com.omegafrog.My.piano.app.lesson.dto.UpdateLessonDto;
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
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    private String title;
    private String subTitle;
    private int price;

    private VideoInformation videoInformation;

    private LocalDateTime createdAt;
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
        this.title = title;
        this.subTitle = subTitle;
        this.price = price;
        this.videoInformation = videoInformation;
        this.lessonProvider = lessonProvider;
        this.sheet = sheet;
        this.lessonInformation = lessonInformation;
        createdAt = LocalDateTime.now();
        viewCount=0;
    }

    public Lesson update(UpdateLessonDto dto){
        this.title = dto.getTitle();
        this.subTitle = dto.getSubTitle();
        this.price = dto.getPrice();
        this.videoInformation = dto.getVideoInformation();
        this.sheet = dto.getSheet();
        this.lessonInformation = dto.getLessonInformation();
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lesson lesson = (Lesson) o;

        return Objects.equals(id, lesson.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
