package com.omegafrog.My.piano.app.dto;

import com.omegafrog.My.piano.app.lesson.entity.LessonInformation;
import com.omegafrog.My.piano.app.lesson.entity.VideoInformation;
import com.omegafrog.My.piano.app.sheet.entity.Sheet;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UpdateLessonDto {
    private String title;
    private String subTitle;
    private int price;

    private Sheet sheet;
    private LessonInformation lessonInformation;
    private VideoInformation videoInformation;


@Builder
    public UpdateLessonDto(String title, String subTitle, int price, Sheet sheet,
                           LessonInformation lessonInformation, VideoInformation videoInformation) {
        this.title = title;
        this.subTitle = subTitle;
        this.price = price;
        this.sheet = sheet;
        this.lessonInformation = lessonInformation;
        this.videoInformation = videoInformation;
    }
}
