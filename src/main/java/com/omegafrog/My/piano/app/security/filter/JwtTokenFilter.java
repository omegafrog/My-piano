package com.omegafrog.My.piano.app.security.filter;

import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.security.jwt.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {
    private final TokenUtils tokenUtils;
    private final SecurityUserRepository securityUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessTokenString = null;
        try {
            accessTokenString = tokenUtils.getAccessTokenString(request.getHeader(HttpHeaders.AUTHORIZATION));
        } catch (AuthenticationCredentialsNotFoundException e) {
            filterChain.doFilter(request, response);
            return;
        }

        RefreshToken refreshToken = null;
        try {
            Claims claims = tokenUtils.extractClaims(accessTokenString);
            Long securityUserId = Long.valueOf((String) claims.get("id"));
            Role securityUserRole = Role.valueOf((String) claims.get("role"));

            SecurityUser user = securityUserRepository.findById(securityUserId)
                    .orElseThrow(EntityNotFoundException::new);
            refreshToken = refreshTokenRepository.findByRoleAndUserId(securityUserId, securityUserRole)
                    .orElseThrow(EntityNotFoundException::new);

            TokenInfo tokenInfo = tokenUtils.wrap(accessTokenString, refreshToken);

            JwtAuthenticationToken jwtAuthenticationToken =
                    new JwtAuthenticationToken(user.getAuthorities(), tokenInfo, securityUserId);
            jwtAuthenticationToken.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);
            filterChain.doFilter(request, response);

        } catch (EntityNotFoundException e) {
            log.error("{} token:{}", e.getMessage(), accessTokenString);
            if (refreshToken == null)
                throw new AuthenticationServiceException("Already logged out user.", e);
            throw new AuthenticationServiceException("Cannot find logged in user.", e);
        } catch (ExpiredJwtException e) {
            log.error("{} token:{}", e.getMessage(), accessTokenString);
            // access token이 만료된 경우
            Claims claims = e.getClaims();
            Long securityUserId = Long.valueOf((String) claims.get("id"));
            Role securityUserRole = Role.valueOf((String) claims.get("role"));

            securityUserRepository.findById(securityUserId)
                    // access token의 payload가 옳지 않은 값인 경우
                    .orElseThrow(() -> new AuthenticationServiceException("Wrong token."));
            refreshTokenRepository.findByRoleAndUserId(securityUserId, securityUserRole)
                    // 이미 로그아웃된 토큰인 경우
                    .orElseThrow(() -> new AuthenticationServiceException("Already logged out user."));

            // 올바른 토큰이지만 만료된 경우 토큰 재발급 컨트롤러로 이동
            filterChain.doFilter(request, response);

        } catch (SignatureException e) {
            log.error("{} token:{}", e.getMessage(), accessTokenString);
            throw new InsufficientAuthenticationException("Signature is invalid", e);
        } catch (JwtException e) {
            log.error("{} token:{}", e.getMessage(), accessTokenString);
            throw new AuthenticationServiceException("Wrong token error. token:" + accessTokenString, e);
        }

    }
}
