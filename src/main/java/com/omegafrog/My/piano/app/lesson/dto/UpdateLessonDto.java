package com.omegafrog.My.piano.app.lesson.dto;

import com.omegafrog.My.piano.app.lesson.entity.LessonInformation;
import lombok.Getter;

@Getter
public class UpdateLessonDto {
    private String title;
    private String subTitle;
    private int price;
    private String videoUrl;
    private Sheet sheet;
    private LessonInformation lessonInformation;



    public UpdateLessonDto(String title, String subTitle, int price, String videoUrl, Sheet sheet,
                           LessonInformation lessonInformation) {
        this.title = title;
        this.subTitle = subTitle;
        this.price = price;
        this.videoUrl = videoUrl;
        this.sheet = sheet;
        this.lessonInformation = lessonInformation;
    }
}
