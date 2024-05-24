package com.omegafrog.My.piano.app.web.domain.lesson;

public interface LessonViewCountRepository {
    int incrementViewCount(Lesson lesson);
    LessonViewCount findById(Long id);
    boolean exist(Long id);

    LessonViewCount save(LessonViewCount lessonViewCount);
}
