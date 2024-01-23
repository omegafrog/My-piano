package com.omegafrog.My.piano.app.web.domain.notification;

import jakarta.persistence.Id;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("noti")
@Getter
public class Subscription {

    @Id
    private String id;
    private String token;
    @Indexed
    private Long userId;

    public Subscription(String token, Long userId) {
        this.token = token;
        this.userId = userId;
    }
}
