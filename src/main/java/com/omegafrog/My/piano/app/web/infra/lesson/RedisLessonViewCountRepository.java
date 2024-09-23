package com.omegafrog.My.piano.app.web.infra.lesson;

import com.omegafrog.My.piano.app.web.domain.lesson.LessonViewCount;
import org.springframework.data.repository.CrudRepository;

public interface RedisLessonViewCountRepository extends CrudRepository<LessonViewCount, Long> {
}
