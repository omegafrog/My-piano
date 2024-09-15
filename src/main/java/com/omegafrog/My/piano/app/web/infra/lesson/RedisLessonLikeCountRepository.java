package com.omegafrog.My.piano.app.web.infra.lesson;

import com.omegafrog.My.piano.app.web.domain.lesson.LessonLikeCount;
import org.springframework.data.repository.CrudRepository;

public interface RedisLessonLikeCountRepository extends CrudRepository<LessonLikeCount, Long> {
}
