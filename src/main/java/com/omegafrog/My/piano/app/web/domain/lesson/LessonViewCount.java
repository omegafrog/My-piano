package com.omegafrog.My.piano.app.web.domain.lesson;

import com.omegafrog.My.piano.app.web.domain.article.ViewCount;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("lesson")
@NoArgsConstructor
@Getter
public class LessonViewCount extends ViewCount {
    public static String KEY_NAME="lesson";

    @Builder
    public LessonViewCount(Long id, int viewCount) {
        this.id= id;
        this.viewCount = viewCount;
    }
}
