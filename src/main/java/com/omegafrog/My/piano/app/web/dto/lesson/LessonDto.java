package com.omegafrog.My.piano.app.web.dto.lesson;

import com.omegafrog.My.piano.app.web.domain.lesson.LessonInformation;
import com.omegafrog.My.piano.app.web.domain.lesson.VideoInformation;
import com.omegafrog.My.piano.app.web.dto.order.ItemDto;
import com.omegafrog.My.piano.app.web.dto.sheet.SheetDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import lombok.*;

@Data
@NoArgsConstructor
@Setter
@Getter
public class LessonDto extends ItemDto {
    Long id;

    private String title;
    private String subTitle;
    private VideoInformation videoInformation;

    private int viewCount;

    private UserProfile lessonProvider;

    private SheetDto sheet;

    private LessonInformation lessonInformation;

    @Builder
    public LessonDto(Long id, String title, String subTitle, VideoInformation videoInformation, int viewCount, UserProfile lessonProvider, SheetDto sheet, LessonInformation lessonInformation) {
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
