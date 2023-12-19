package com.omegafrog.My.piano.app.web.dto.lesson;

import com.omegafrog.My.piano.app.web.domain.lesson.LessonInformation;
import com.omegafrog.My.piano.app.web.domain.lesson.VideoInformation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Data
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonRegisterDto {
    @NotNull
    private String title;
    @NotNull
    private String subTitle;
    @NotNull
    private VideoInformation videoInformation;

    @NotNull
    private LessonInformation lessonInformation;

    @PositiveOrZero
    private int viewCount;

    private Long sheetId;

    private int price;

    @Builder
    public LessonRegisterDto(String title, String subTitle, VideoInformation videoInformation,LessonInformation lessonInformation, Long sheetId, int price) {
        this.title = title;
        this.subTitle = subTitle;
        this.videoInformation = videoInformation;
        this.viewCount =0;
        this.sheetId = sheetId;
        this.lessonInformation = lessonInformation;
        this.price = price;
    }
}
