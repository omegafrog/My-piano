package com.omegafrog.My.piano.app.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private TokenInfo tokenInfo;
    private Long securityUserId;

    public JwtAuthenticationToken(Collection<? extends GrantedAuthority> authorities, TokenInfo tokenInfo, Long securityUserId) {
        super(authorities);
        this.securityUserId = securityUserId;
        this.tokenInfo = tokenInfo;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return securityUserId;
    }
}
