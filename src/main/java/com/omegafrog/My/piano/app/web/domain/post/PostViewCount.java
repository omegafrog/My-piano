package com.omegafrog.My.piano.app.web.domain.post;

import com.omegafrog.My.piano.app.web.domain.article.ViewCount;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("post")
@NoArgsConstructor
@Getter
public class PostViewCount extends ViewCount {
    public static final String KEY_NAME="post";
    public PostViewCount(Long postId, int viewCount) {
        this.id = postId;
        this.viewCount = viewCount;
    }
}
