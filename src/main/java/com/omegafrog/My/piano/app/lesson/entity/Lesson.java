package com.omegafrog.My.piano.app.lesson.entity;

import com.omegafrog.My.piano.app.lesson.dto.UpdateLessonDto;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@NoArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String subTitle;
    private int price;
    private String videoUrl;
    private LocalDateTime createdAt;
    private int viewCount;
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "ARTIST_ID")
    private LessonProvider lessonProvider;
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "SHEET_ID")
    private Sheet sheet;

    private LessonInformation lessonInformation;

    @Builder
    public Lesson(String title, String subTitle, int price, String videoUrl,
                  LessonProvider lessonProvider, Sheet sheet, LessonInformation lessonInformation) {
        this.title = title;
        this.subTitle = subTitle;
        this.price = price;
        this.videoUrl = videoUrl;
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
        this.videoUrl = dto.getVideoUrl();
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
