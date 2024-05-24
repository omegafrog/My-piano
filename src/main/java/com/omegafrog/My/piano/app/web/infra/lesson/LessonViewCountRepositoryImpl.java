package com.omegafrog.My.piano.app.web.infra.lesson;

import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonViewCount;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LessonViewCountRepositoryImpl implements com.omegafrog.My.piano.app.web.domain.lesson.LessonViewCountRepository {

    private final RedisLessonViewCountRepository jpaRepository;
    private final RedisTemplate<String, LessonViewCount> redisTemplate;
    @Override
    public int incrementViewCount(Lesson lesson) {
        if(!exist(lesson.getId())) {
            LessonViewCount saved = save(LessonViewCount.builder()
                    .id(lesson.getId())
                    .viewCount(lesson.getViewCount() + 1).build());
            return saved.getViewCount();
        }
        return redisTemplate.opsForHash().increment("lesson:"+lesson.getId(), "viewCount", 1L).intValue();

    }
    @Override
    public boolean exist(Long id){
        return redisTemplate.opsForHash().hasKey("lesson:"+id, "viewCount");
    }

    @Override
    public LessonViewCount save(LessonViewCount lessonViewCount) {
        return jpaRepository.save(lessonViewCount);
    }
    @Override
    public LessonViewCount findById(Long id){
        return jpaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find lesson view count entity : " + id));
    }
}
