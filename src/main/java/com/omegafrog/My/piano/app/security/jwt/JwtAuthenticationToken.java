package com.omegafrog.My.piano.app.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collections;
import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final Long securityUserId;
    private final String accessToken;
    private final TokenInfo tokenInfo;

    private JwtAuthenticationToken(Collection<? extends GrantedAuthority> authorities,
                                   Long securityUserId,
                                   String accessToken,
                                   TokenInfo tokenInfo,
                                   boolean authenticated) {
        super(authorities);
        this.securityUserId = securityUserId;
        this.accessToken = accessToken;
        this.tokenInfo = tokenInfo;
        setAuthenticated(authenticated);
    }

    public static JwtAuthenticationToken unauthenticated(String accessToken) {
        return new JwtAuthenticationToken(Collections.emptyList(), null, accessToken, null, false);
    }

    public static JwtAuthenticationToken authenticated(Collection<? extends GrantedAuthority> authorities,
                                                       TokenInfo tokenInfo,
                                                       Long securityUserId) {
        return new JwtAuthenticationToken(authorities, securityUserId, null, tokenInfo, true);
    }

    @Override
    public Object getCredentials() {
        return accessToken;
    }

    @Override
    public Object getPrincipal() {
        return securityUserId;
    }

    public TokenInfo getTokenInfo() {
        return tokenInfo;
    }
}
