package com.omegafrog.My.piano.app.dto;

import com.omegafrog.My.piano.app.security.entity.authorities.Authority;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class SecurityUserDto {
    private Long id;

    private String username;
    private String password;
    private List<Authority> authorities;
    private LocalDateTime createdAt;
    private LocalDateTime credentialChangedAt;
    private boolean locked;

    @Builder
    public SecurityUserDto(Long id, String username, String password, List<Authority> authorities, LocalDateTime createdAt, LocalDateTime credentialChangedAt, boolean locked) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.createdAt = createdAt;
        this.credentialChangedAt = credentialChangedAt;
        this.locked = locked;
    }
}
