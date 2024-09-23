package com.omegafrog.My.piano.app.web.dto.user;

import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SecurityUserDto {
    private Long id;

    private String username;
    private String password;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime credentialChangedAt;
    private boolean locked;

    @Builder
    public SecurityUserDto(Long id, String username, String password, Role role, LocalDateTime createdAt, LocalDateTime credentialChangedAt, boolean locked) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
        this.credentialChangedAt = credentialChangedAt;
        this.locked = locked;
    }
}
