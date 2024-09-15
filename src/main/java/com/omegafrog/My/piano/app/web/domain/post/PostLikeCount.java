package com.omegafrog.My.piano.app.web.domain.post;

import com.omegafrog.My.piano.app.web.domain.article.LikeCount;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("post")
@NoArgsConstructor
@Getter
public class PostLikeCount extends LikeCount {
    public static String KEY_NAME = "post";

    @Builder
    public PostLikeCount(Long id, int likeCount) {
        super(id, likeCount);
    }
}
