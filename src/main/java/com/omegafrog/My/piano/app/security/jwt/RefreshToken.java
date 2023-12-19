package com.omegafrog.My.piano.app.security.jwt;

import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RedisHash("refresh")
@Getter
@NoArgsConstructor
public class RefreshToken {

    private String id;
    private String refreshToken;
    @Indexed
    private Long userId;
    private Role role;

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private Long expiration;

    @Builder
    public RefreshToken(String id, String refreshToken, Long userId, Long expiration, Role role) {
        this.id = id;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.expiration = expiration;
        this.role = role;
    }

    public void updateRefreshToken(String refreshToken){
        this.refreshToken = refreshToken;
    }
}
