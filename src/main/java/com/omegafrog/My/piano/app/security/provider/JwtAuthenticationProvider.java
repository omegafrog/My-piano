package com.omegafrog.My.piano.app.security.provider;

import com.omegafrog.My.piano.app.security.jwt.JwtAuthenticationToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenInfo;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUser;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUserRepository;
import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final TokenUtils tokenUtils;
    private final SecurityUserRepository securityUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String accessToken = (String) authentication.getCredentials();
        if (accessToken == null || accessToken.isBlank()) {
            throw new BadCredentialsException("Token string is null");
        }

        try {
            Claims claims = tokenUtils.extractClaims(accessToken);
            Long securityUserId = Long.valueOf((String) claims.get("id"));
            Role securityUserRole = Role.valueOf((String) claims.get("role"));

            SecurityUser user = findUser(securityUserId);
            RefreshToken refreshToken = findRefreshToken(securityUserId, securityUserRole);
            TokenInfo tokenInfo = tokenUtils.wrap(accessToken, refreshToken);
            return JwtAuthenticationToken.authenticated(user.getAuthorities(), tokenInfo, securityUserId);
        } catch (ExpiredJwtException e) {
            validateExpiredTokenClaims(e);
            throw new CredentialsExpiredException("Access token is expired.", e);
        } catch (SignatureException e) {
            throw new AuthenticationServiceException("Signature is invalid", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthenticationServiceException("Wrong token error.", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private SecurityUser findUser(Long securityUserId) {
        return securityUserRepository.findById(securityUserId)
                .orElseThrow(() -> new UsernameNotFoundException("Cannot find logged in user."));
    }

    private RefreshToken findRefreshToken(Long securityUserId, Role securityUserRole) {
        return refreshTokenRepository.findByRoleAndUserId(securityUserId, securityUserRole)
                .orElseThrow(() -> new AuthenticationServiceException("Already logged out user."));
    }

    private void validateExpiredTokenClaims(ExpiredJwtException e) {
        Claims claims = e.getClaims();
        Long securityUserId = Long.valueOf((String) claims.get("id"));
        Role securityUserRole = Role.valueOf((String) claims.get("role"));
        findUser(securityUserId);
        findRefreshToken(securityUserId, securityUserRole);
    }
}
