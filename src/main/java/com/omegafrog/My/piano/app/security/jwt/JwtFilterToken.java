package com.omegafrog.My.piano.app.security.jwt;

import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.jwt.TokenInfo;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtFilterToken extends AbstractAuthenticationToken {
    private TokenInfo tokenInfo;
    private SecurityUser securityUser;
    public JwtFilterToken(Collection<? extends GrantedAuthority> authorities, TokenInfo tokenInfo, SecurityUser securityUser){
        super(authorities);
        this.securityUser = securityUser;
        this.tokenInfo = tokenInfo;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return tokenInfo;
    }
}
