package com.omegafrog.My.piano.app.web.dto.lesson;

import com.omegafrog.My.piano.app.web.domain.lesson.LessonInformation;
import com.omegafrog.My.piano.app.web.domain.lesson.VideoInformation;
import com.omegafrog.My.piano.app.web.dto.order.SellableItemDto;
import com.omegafrog.My.piano.app.web.dto.sheet.SheetDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@Setter
@Getter
@SuperBuilder
public class LessonDto extends SellableItemDto {
    private String subTitle;
    private VideoInformation videoInformation;
    private UserProfile lessonProvider;
    private SheetDto sheet;
    private LessonInformation lessonInformation;

}
