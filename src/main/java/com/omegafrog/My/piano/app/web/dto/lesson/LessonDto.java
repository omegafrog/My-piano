package com.omegafrog.My.piano.app.web.dto.lesson;

import com.omegafrog.My.piano.app.web.domain.lesson.LessonInformation;
import com.omegafrog.My.piano.app.web.domain.lesson.VideoInformation;
import com.omegafrog.My.piano.app.web.dto.order.SellableItemDto;
import com.omegafrog.My.piano.app.web.dto.sheet.SheetDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import lombok.*;

import java.util.Objects;

@Data
@NoArgsConstructor
@Setter
@Getter
public class LessonDto extends SellableItemDto {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        LessonDto lessonDto = (LessonDto) o;

        if (viewCount != lessonDto.viewCount) return false;
        if (!Objects.equals(id, lessonDto.id)) return false;
        if (!Objects.equals(title, lessonDto.title)) return false;
        if (!Objects.equals(subTitle, lessonDto.subTitle)) return false;
        if (!Objects.equals(videoInformation, lessonDto.videoInformation))
            return false;
        if (!Objects.equals(lessonProvider, lessonDto.lessonProvider))
            return false;
        if (!Objects.equals(sheet, lessonDto.sheet)) return false;
        return Objects.equals(lessonInformation, lessonDto.lessonInformation);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (subTitle != null ? subTitle.hashCode() : 0);
        result = 31 * result + (videoInformation != null ? videoInformation.hashCode() : 0);
        result = 31 * result + viewCount;
        result = 31 * result + (lessonProvider != null ? lessonProvider.hashCode() : 0);
        result = 31 * result + (sheet != null ? sheet.hashCode() : 0);
        result = 31 * result + (lessonInformation != null ? lessonInformation.hashCode() : 0);
        return result;
    }
}
