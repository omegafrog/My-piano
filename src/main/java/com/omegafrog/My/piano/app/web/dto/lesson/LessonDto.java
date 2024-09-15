package com.omegafrog.My.piano.app.web.dto.lesson;

import com.omegafrog.My.piano.app.web.domain.lesson.LessonInformation;
import com.omegafrog.My.piano.app.web.domain.lesson.VideoInformation;
import com.omegafrog.My.piano.app.web.dto.order.SellableItemDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.ArtistInfo;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetDto;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@Setter
@Getter
@SuperBuilder
public class LessonDto extends SellableItemDto {
    private String subTitle;
    private VideoInformation videoInformation;
    private ArtistInfo artist;
    private SheetDto sheet;
    private Long sheetPostId;
    private LessonInformation lessonInformation;
}
