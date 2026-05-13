package com.omegafrog.My.piano.app.security.filter;

import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.TokenInfo;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUserRepository;
import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CommonUserJwtTokenFilter extends OncePerRequestFilter {

    private final SecurityUserRepository securityUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenUtils tokenUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws AuthenticationException, ServletException, IOException {

        List<AntPathRequestMatcher> ignoredPatterns = new ArrayList<>(
                Arrays.asList(
                        AntPathRequestMatcher.antMatcher("/api/v1/**/login/**"),
                        AntPathRequestMatcher.antMatcher("/api/v1/**/register/**"),
                        AntPathRequestMatcher.antMatcher("/api/v1/user/profile/register"),
                        AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/v1/sheet-post"),
                        AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/v1/sheet-post/autocomplete"),
                        AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/v1/sheet-post/{regex:\\d+}"),
                        AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/v1/sheet-post/{regex:\\d+}/comments"),
                        AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/v1/lesson/{regex:\\d+}"),
                        AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/v1/lesson/{regex:\\d+}/comments"),
                        AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/v1/lessons"),
                        AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/v1/posts"),
                        AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/v1/posts/{regex:\\d+}/comments"),
                        AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/v1/community/video-post"),
                        AntPathRequestMatcher.antMatcher("/h2-console/**"),
                        AntPathRequestMatcher.antMatcher("/api/v1/oauth2/google/callback"),
                        AntPathRequestMatcher.antMatcher("/api/v1/revalidate"),
                        AntPathRequestMatcher.antMatcher("/api/v1/popular"),
                        AntPathRequestMatcher.antMatcher("/healthcheck"),
                        AntPathRequestMatcher.antMatcher("/api/v1/cash/webhook"),
                        AntPathRequestMatcher.antMatcher("/swagger-ui/**"),
                        AntPathRequestMatcher.antMatcher("/api-docs/**")));
        for (AntPathRequestMatcher pathMatcher : ignoredPatterns) {
            if (pathMatcher.matches(request)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        try {
            // token 추출
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || authHeader.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }
            String accessToken = tokenUtils.getAccessTokenString(authHeader);
            // token으로부터 유저 추출
            Claims claims = tokenUtils.extractClaims(accessToken);
            Long userId = Long.valueOf((String) claims.get("id"));
            Role role = Role.valueOf(String.valueOf(claims.get("role")));

            // refresh token repository에 해당 유저의 refresh token이 없다면 로그아웃한 것.
            Optional<RefreshToken> refreshToken = refreshTokenRepository.findByRoleAndUserId(userId, role);
            if (refreshToken.isEmpty()) {
                throw new BadCredentialsException("Already logged out user.");
            }
            // token validation 진행
            UserDetails user = getUserFromAccessToken(userId);
            Authentication usernameToken = getAuthenticationToken(user);
            SecurityContextHolder.getContext().setAuthentication(usernameToken);
            TokenInfo tokenInfo = tokenUtils.refreshAccessToken(String.valueOf(userId), role, refreshToken.get());
            response.setHeader(HttpHeaders.AUTHORIZATION, tokenInfo.getGrantType() + " " + tokenInfo.getAccessToken());
            response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.AUTHORIZATION);
        } catch (ExpiredJwtException e) {
            // 만료되었을 때
            e.printStackTrace();
            throw new BadCredentialsException("Access token is expired.", e);
        }
        filterChain.doFilter(request, response);
    }

    private UserDetails getUserFromAccessToken(Long userId) {
        return securityUserRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Cannot find User entity"));
    }

    private static Authentication getAuthenticationToken(UserDetails user) {
        return new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
    }
}
