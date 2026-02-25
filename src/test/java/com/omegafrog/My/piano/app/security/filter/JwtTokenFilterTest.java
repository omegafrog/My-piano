package com.omegafrog.My.piano.app.security.filter;

import com.omegafrog.My.piano.app.security.exception.JwtExpiredButRefreshableException;
import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUser;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUserRepository;
import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtTokenFilterTest {

    @Mock
    private TokenUtils tokenUtils;

    @Mock
    private SecurityUserRepository securityUserRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuthenticationManager jwtAuthenticationManager;

    @InjectMocks
    private JwtTokenFilter jwtTokenFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증 없이 다음 필터로 진행한다")
    void passThroughWhenAuthorizationHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        given(tokenUtils.getAccessTokenString(null))
                .willThrow(new AuthenticationCredentialsNotFoundException("Token string is null"));

        jwtTokenFilter.doFilter(request, response, filterChain);

        verify(jwtAuthenticationManager, never()).authenticate(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("유효한 token이면 AuthenticationManager 결과를 SecurityContext에 설정한다")
    void setAuthenticationWhenTokenIsValid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(1L, null);

        given(tokenUtils.getAccessTokenString("Bearer access-token")).willReturn("access-token");
        given(jwtAuthenticationManager.authenticate(any())).willReturn(authentication);

        jwtTokenFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("만료됐지만 재발급 가능한 token이면 인증을 세팅하지 않고 다음 필터로 진행한다")
    void continueWithoutAuthenticationWhenTokenIsExpiredButRefreshable() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        Claims claims = Jwts.claims();
        claims.put("id", "1");
        claims.put("role", Role.USER.value);
        ExpiredJwtException expiredJwtException = new ExpiredJwtException(Jwts.header(), claims, "expired");
        JwtExpiredButRefreshableException refreshableException =
                new JwtExpiredButRefreshableException("Access token is expired.", expiredJwtException);

        SecurityUser user = SecurityUser.builder()
                .username("user1")
                .password("encoded")
                .role(Role.USER)
                .build();
        RefreshToken refreshToken = RefreshToken.builder()
                .id("USER-1")
                .payload("refresh")
                .userId(1L)
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .expiration(1000L)
                .build();

        given(tokenUtils.getAccessTokenString("Bearer expired-token")).willReturn("expired-token");
        given(jwtAuthenticationManager.authenticate(any())).willThrow(refreshableException);
        given(securityUserRepository.findById(1L)).willReturn(Optional.of(user));
        given(refreshTokenRepository.findByRoleAndUserId(1L, Role.USER)).willReturn(Optional.of(refreshToken));

        jwtTokenFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("만료 token의 refresh 정보가 없으면 인증 실패 예외를 던진다")
    void throwWhenTokenIsExpiredAndLoggedOut() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        Claims claims = Jwts.claims();
        claims.put("id", "1");
        claims.put("role", Role.USER.value);
        ExpiredJwtException expiredJwtException = new ExpiredJwtException(Jwts.header(), claims, "expired");
        JwtExpiredButRefreshableException refreshableException =
                new JwtExpiredButRefreshableException("Access token is expired.", expiredJwtException);

        SecurityUser user = SecurityUser.builder()
                .username("user1")
                .password("encoded")
                .role(Role.USER)
                .build();

        given(tokenUtils.getAccessTokenString("Bearer expired-token")).willReturn("expired-token");
        given(jwtAuthenticationManager.authenticate(any())).willThrow(refreshableException);
        given(securityUserRepository.findById(1L)).willReturn(Optional.of(user));
        given(refreshTokenRepository.findByRoleAndUserId(1L, Role.USER)).willReturn(Optional.empty());

        assertThatThrownBy(() -> jwtTokenFilter.doFilter(request, response, filterChain))
                .isInstanceOf(AuthenticationServiceException.class)
                .hasMessageContaining("Already logged out user");
    }
}
