package com.omegafrog.My.piano.app.web.domain.user.authorities;

import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@NoArgsConstructor
public class Authority implements GrantedAuthority {
    private String authorityName;

    @Override
    public String getAuthority() {
        return "ROLE_" + authorityName;
    }

    @Builder
    public Authority(String authority) {
        this.authorityName = authority;
    }
}
