package com.omegafrog.My.piano.app.web.domain.article;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@NoArgsConstructor
@Getter
public abstract class ViewCount {
    @Id
    protected Long id;
    protected int viewCount = 0;

    public ViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
}

