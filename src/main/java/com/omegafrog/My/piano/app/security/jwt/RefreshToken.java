package com.omegafrog.My.piano.app.security.jwt;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.concurrent.TimeUnit;

@RedisHash("refresh")
@Getter
@NoArgsConstructor
public class RefreshToken {

    @Id
    private Long id;
    private String refreshToken;
    @Indexed
    private Long userId;

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private Long expiration;

    @Builder
    public RefreshToken(String refreshToken, Long userId, Long expiration) {
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.expiration = expiration;
    }

    public void updateRefreshToken(String refreshToken){
        this.refreshToken = refreshToken;
    }
}
