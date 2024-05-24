package com.omegafrog.My.piano.app.web.domain.article;

import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonViewCount;

public interface ViewCountRepository<T extends ViewCount> {

    int incrementViewCount(Article article);
    T findById(Long id);
    boolean exist(Long id);

    T save(T viewCount);
}
