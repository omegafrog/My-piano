package com.omegafrog.My.piano.app.web.dto;

import com.omegafrog.My.piano.app.web.domain.lesson.LessonInformation;
import com.omegafrog.My.piano.app.web.domain.lesson.VideoInformation;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
public class UpdateLessonDto {
    private String title;
    private String subTitle;
    private int price;

    private Long sheetId;
    private LessonInformation lessonInformation;
    private VideoInformation videoInformation;


@Builder
    public UpdateLessonDto(String title, String subTitle, int price, Long sheetId,
                           LessonInformation lessonInformation, VideoInformation videoInformation) {
        this.title = title;
        this.subTitle = subTitle;
        this.price = price;
        this.sheetId = sheetId;
        this.lessonInformation = lessonInformation;
        this.videoInformation = videoInformation;
    }
}
