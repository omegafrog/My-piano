package com.omegafrog.My.piano.app.web.domain.lesson;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("lesson")
@NoArgsConstructor
@Getter
public class LessonViewCount {

    @Id
    private Long lessonId;
    private int viewCount;

    @Builder
    public LessonViewCount(Long lessonId, int viewCount) {
        this.lessonId = lessonId;
        this.viewCount = viewCount;
    }
}
