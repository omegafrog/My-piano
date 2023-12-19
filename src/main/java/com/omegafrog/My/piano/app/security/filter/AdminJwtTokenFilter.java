package com.omegafrog.My.piano.app.security.filter;

import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.security.service.AdminUserService;
import com.omegafrog.My.piano.app.web.domain.admin.Admin;
import com.omegafrog.My.piano.app.web.domain.admin.AdminRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Admin authenitcation에 사용되는 jwt token을 decode하는 filter
 * Admin과 common user의 refresh token repository를 분리해서 사용하므로 repository를 bean으로 꼭 따로 등록해야 한다
 */
@RequiredArgsConstructor
public class AdminJwtTokenFilter extends OncePerRequestFilter {

    @Autowired
    private TokenUtils tokenUtils;

    // admin redis server template으로 구현된 repository 필요함
    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        List<AntPathRequestMatcher> ignoredPatterns = new ArrayList<>(
                Arrays.asList(
                        AntPathRequestMatcher.antMatcher("/**/login/**"),
                        AntPathRequestMatcher.antMatcher("/**/register/**")
                ));
        for (AntPathRequestMatcher pathMatcher : ignoredPatterns) {
            if (pathMatcher.matches(request)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // request가 admin으로 시작하지 않다면 필터에 해당하지 않은 request이므로 패스
        if (!AntPathRequestMatcher.antMatcher("/admin/**").matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // token 추출
            String accessToken = tokenUtils.getAccessTokenStringFromHeaders(request);
            //token으로부터 유저 추출
            Claims claims = tokenUtils.extractClaims(accessToken);
            Long userId = Long.valueOf((String) claims.get("id"));

            // refresh token repository에 해당 유저의 refresh token이 없다면 로그아웃한 것.
            if (refreshTokenRepository.findByUserId(userId).isEmpty()) {
                throw new BadCredentialsException("Already logged out user.");
            }
            // token validation 진행
            Admin user = getAdminFromAccessToken(userId);
            Authentication usernameToken = getAuthenticationToken(user);
            SecurityContextHolder.getContext().setAuthentication(usernameToken);
        } catch (ExpiredJwtException e) {
            // 만료되었을 때
            e.printStackTrace();
            throw new BadCredentialsException("Access token is expired.");
        }
        filterChain.doFilter(request, response);
    }

    private Admin getAdminFromAccessToken(Long userId) {
        return adminRepository.findById(userId).orElseThrow(
                () -> new AuthenticationCredentialsNotFoundException("Invalid access token")
        );
    }

    private static Authentication getAuthenticationToken(Admin admin) {
        return new UsernamePasswordAuthenticationToken(
                admin, null, admin.getAuthorities()
        );
    }
}
