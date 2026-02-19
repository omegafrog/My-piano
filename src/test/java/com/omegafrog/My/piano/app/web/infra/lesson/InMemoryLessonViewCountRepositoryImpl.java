package com.omegafrog.My.piano.app.web.infra.lesson;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.omegafrog.My.piano.app.TestResettable;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonViewCount;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonViewCountRepository;

@Repository
@Profile("test")
public class InMemoryLessonViewCountRepositoryImpl implements LessonViewCountRepository, TestResettable {

    private final Map<Long, Integer> counts = new ConcurrentHashMap<>();

    @Override
    public int incrementViewCount(Lesson lesson) {
        return counts.merge(lesson.getId(), lesson.getViewCount() + 1, (prev, init) -> prev + 1);
    }

    @Override
    public LessonViewCount findById(Long id) {
        Integer count = counts.get(id);
        if (count == null) {
            throw new IllegalArgumentException("Cannot find lesson view count entity : " + id);
        }
        return LessonViewCount.builder().id(id).viewCount(count).build();
    }

    @Override
    public boolean exist(Long id) {
        return counts.containsKey(id);
    }

    @Override
    public LessonViewCount save(LessonViewCount lessonViewCount) {
        counts.put(lessonViewCount.getId(), lessonViewCount.getViewCount());
        return lessonViewCount;
    }

    @Override
    public void reset() {
        counts.clear();
    }
}
