package com.omegafrog.My.piano.app.web.domain.lesson;

import com.omegafrog.My.piano.app.web.domain.article.LikeCount;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("lesson")
@NoArgsConstructor
@Getter
public class LessonLikeCount extends LikeCount {
    public static String KEY_NAME = "lesson";

    @Builder
    public LessonLikeCount(Long id, int likeCount) {
        super(id, likeCount);
    }
}
