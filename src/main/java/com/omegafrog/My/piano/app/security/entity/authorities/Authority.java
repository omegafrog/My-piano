package com.omegafrog.My.piano.app.security.entity.authorities;

import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;

public class Authority implements GrantedAuthority {
    private String authorityName;
    @Override
    public String getAuthority() {
        return authorityName;
    }

    @Builder
    public Authority(String authorityName) {
        this.authorityName = authorityName;
    }
}
