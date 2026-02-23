package com.omegafrog.My.piano.app.security.filter;

import com.omegafrog.My.piano.app.security.exception.JwtExpiredButRefreshableException;
import com.omegafrog.My.piano.app.security.jwt.JwtAuthenticationToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUserRepository;
import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
    private final TokenUtils tokenUtils;
    private final SecurityUserRepository securityUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager jwtAuthenticationManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String accessTokenString;
        try {
            accessTokenString = tokenUtils.getAccessTokenString(request.getHeader(HttpHeaders.AUTHORIZATION));
        } catch (AuthenticationCredentialsNotFoundException e) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication authentication = jwtAuthenticationManager
                    .authenticate(JwtAuthenticationToken.unauthenticated(accessTokenString));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtExpiredButRefreshableException e) {
            validateExpiredTokenClaims((ExpiredJwtException) e.getCause());
        }

        filterChain.doFilter(request, response);
    }

    private void validateExpiredTokenClaims(ExpiredJwtException expiredJwtException) {
        Claims claims = expiredJwtException.getClaims();
        Long securityUserId = Long.valueOf((String) claims.get("id"));
        Role securityUserRole = Role.valueOf((String) claims.get("role"));

        securityUserRepository.findById(securityUserId)
                .orElseThrow(() -> new AuthenticationServiceException("Wrong token."));
        refreshTokenRepository.findByRoleAndUserId(securityUserId, securityUserRole)
                .orElseThrow(() -> new AuthenticationServiceException("Already logged out user."));
    }
}
