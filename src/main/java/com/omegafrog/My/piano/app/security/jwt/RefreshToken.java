package com.omegafrog.My.piano.app.security.jwt;

import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@RedisHash("refresh")
@Getter
@NoArgsConstructor
public class RefreshToken {

    @Setter
    private String id;
    private String payload;
    private Long userId;
    private Role role;

    private Long expiration;
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public RefreshToken(String id, String payload, Long userId, Long expiration, Role role, LocalDateTime createdAt) {
        this.id = id;
        this.payload = payload;
        this.userId = userId;
        this.expiration = expiration;
        this.role = role;
        this.createdAt = createdAt;
    }

    public void updateRefreshToken(String refreshToken) {
        this.payload = refreshToken;
    }
}
