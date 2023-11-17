package com.omegafrog.My.piano.app.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    private final SecurityUserRepository securityUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.jwt.secret}")
    private String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws AuthenticationException, ServletException, IOException {

        List<AntPathRequestMatcher> ignoredPatterns = new ArrayList<>(
                Arrays.asList(
                        AntPathRequestMatcher.antMatcher("/user/login/**"),
                        AntPathRequestMatcher.antMatcher("/user/register"),
                        AntPathRequestMatcher.antMatcher("/user/profile/register"),
                        AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/sheet"),
                        AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/sheet/{regex:\\d+}"),
                        AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/lesson/{regex:\\d+}"),
                        AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/lessons"),
                        AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/community/video-post"),
                        AntPathRequestMatcher.antMatcher("/h2-console/**"),
                        AntPathRequestMatcher.antMatcher("/oauth2/**"),
                        AntPathRequestMatcher.antMatcher("/revalidate")
                ));
        for (AntPathRequestMatcher pathMatcher : ignoredPatterns) {
            if (pathMatcher.matches(request)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        try {
            // token 추출
            String accessToken = TokenUtils.getAccessTokenStringFromHeaders(request);
            //token으로부터 유저 추출
            Claims claims = TokenUtils.extractClaims(accessToken, secret);
            Long userId = Long.valueOf((String) claims.get("id"));

            // refresh token repository에 해당 유저의 refresh token이 없다면 로그아웃한 것.
            if (refreshTokenRepository.findByUserId(userId).isEmpty()) {
                throw new BadCredentialsException("Already logged out user.");
            }
            // token validation 진행
            SecurityUser user = getSecurityUserFromAccessToken(userId);
            Authentication usernameToken = getAuthenticationToken(user);
            SecurityContextHolder.getContext().setAuthentication(usernameToken);
        } catch (ExpiredJwtException e) {
            // 만료되었을 때
            e.printStackTrace();
            throw new BadCredentialsException("Access token is expired.");
        }
        filterChain.doFilter(request, response);
    }

    private SecurityUser getSecurityUserFromAccessToken(Long userId) {
        SecurityUser user;
        user = securityUserRepository.findById(userId).orElseThrow(
                () -> new AuthenticationCredentialsNotFoundException("Invalid access token")
        );
        return user;
    }

    private static Authentication getAuthenticationToken(SecurityUser user) {
        return new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
        );
    }
}
