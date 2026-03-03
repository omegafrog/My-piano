package com.omegafrog.My.piano.app.web.infra.lesson;

import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonViewCount;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.MutableEntry;

@Repository
@Profile("!test")
@RequiredArgsConstructor
public class LessonViewCountRepositoryImpl implements com.omegafrog.My.piano.app.web.domain.lesson.LessonViewCountRepository {

    private static final String CACHE_NAME = "lessonViewCounts";
    private final CacheManager cacheManager;
    private final LessonRepository lessonRepository;

    private Cache<Long, Integer> cache() {
        return cacheManager.getCache(CACHE_NAME, Long.class, Integer.class);
    }

    @Override
    public int incrementViewCount(Lesson lesson) {
        Integer updated = cache().invoke(lesson.getId(), (EntryProcessor<Long, Integer, Integer>)
                (MutableEntry<Long, Integer> entry, Object... arguments) -> {
                    int initial = (Integer) arguments[0];
                    int current = entry.exists() ? entry.getValue() : initial;
                    int next = current + 1;
                    entry.setValue(next);
                    return next;
                }, lesson.getViewCount());
        return updated == null ? lesson.getViewCount() + 1 : updated;

    }
    @Override
    public boolean exist(Long id){
        return cache().containsKey(id);
    }

    @Override
    public LessonViewCount save(LessonViewCount lessonViewCount) {
        cache().put(lessonViewCount.getId(), lessonViewCount.getViewCount());
        return lessonViewCount;
    }
    @Override
    public LessonViewCount findById(Long id){
        Integer cached = cache().get(id);
        if (cached != null) {
            return LessonViewCount.builder().id(id).viewCount(cached).build();
        }

        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find lesson entity : " + id));
        int current = lesson.getViewCount();
        cache().put(id, current);
        return LessonViewCount.builder().id(id).viewCount(current).build();
    }
}
