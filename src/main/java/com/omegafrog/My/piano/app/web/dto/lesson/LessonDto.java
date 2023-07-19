package com.omegafrog.My.piano.app.web.dto.lesson;

import com.omegafrog.My.piano.app.web.domain.lesson.LessonInformation;
import com.omegafrog.My.piano.app.web.domain.lesson.VideoInformation;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@Setter
@Getter
public class LessonDto {
    Long id;

    private String title;
    private String subTitle;
    private VideoInformation videoInformation;

    private int viewCount;

    private User lessonProvider;

    private Sheet sheet;

    private LessonInformation lessonInformation;

    @Builder
    public LessonDto(Long id, String title, String subTitle, VideoInformation videoInformation, int viewCount, User lessonProvider, Sheet sheet, LessonInformation lessonInformation) {
        this.id = id;
        this.title = title;
        this.subTitle = subTitle;
        this.videoInformation = videoInformation;
        this.viewCount = viewCount;
        this.lessonProvider = lessonProvider;
        this.sheet = sheet;
        this.lessonInformation = lessonInformation;
    }
}
