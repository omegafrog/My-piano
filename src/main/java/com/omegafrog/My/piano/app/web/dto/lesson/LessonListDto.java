package com.omegafrog.My.piano.app.web.dto.lesson;

import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.dto.sheet.SheetDto;
import com.omegafrog.My.piano.app.web.dto.user.UserInfo;

import java.time.LocalDateTime;

public record LessonListDto(
        Long id,
        String title,
        UserInfo creator,
        SheetDto sheet,
        LocalDateTime createdAt,
        int price,
        boolean disabled
) {
    public LessonListDto(Lesson lesson){
        this(lesson.getId(), lesson.getTitle(), lesson.getAuthor().getUserInfo(), lesson.getSheet().toSheetDto(),
                lesson.getCreatedAt(), lesson.getPrice(), lesson.isDisabled());
    }

}
