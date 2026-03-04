package com.omegafrog.My.piano.app.security.provider;

import com.omegafrog.My.piano.app.security.jwt.JwtAuthenticationToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUser;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUserRepository;
import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationProviderTest {

    @Mock
    private TokenUtils tokenUtils;

    @Mock
    private SecurityUserRepository securityUserRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private JwtAuthenticationProvider jwtAuthenticationProvider;

    @Test
    @DisplayName("유효한 access token이면 authenticated JWT Authentication을 반환한다")
    void authenticateWithValidToken() {
        Claims claims = claims("1", Role.USER);
        SecurityUser securityUser = SecurityUser.builder()
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

        given(tokenUtils.extractClaims("access-token")).willReturn(claims);
        given(securityUserRepository.findById(1L)).willReturn(Optional.of(securityUser));
        given(refreshTokenRepository.findByRoleAndUserId(1L, Role.USER)).willReturn(Optional.of(refreshToken));

        Authentication authentication = jwtAuthenticationProvider
                .authenticate(JwtAuthenticationToken.unauthenticated("access-token"));

        assertThat(authentication).isInstanceOf(JwtAuthenticationToken.class);
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isEqualTo(1L);
    }

    @Test
    @DisplayName("refresh token이 없으면 로그아웃된 사용자로 간주하고 인증을 실패한다")
    void authenticateWithLoggedOutToken() {
        Claims claims = claims("1", Role.USER);
        SecurityUser securityUser = SecurityUser.builder()
                .username("user1")
                .password("encoded")
                .role(Role.USER)
                .build();

        given(tokenUtils.extractClaims("access-token")).willReturn(claims);
        given(securityUserRepository.findById(1L)).willReturn(Optional.of(securityUser));
        given(refreshTokenRepository.findByRoleAndUserId(1L, Role.USER)).willReturn(Optional.empty());

        assertThatThrownBy(() -> jwtAuthenticationProvider
                .authenticate(JwtAuthenticationToken.unauthenticated("access-token")))
                .isInstanceOf(AuthenticationServiceException.class)
                .hasMessageContaining("Already logged out user");
    }

    @Test
    @DisplayName("만료된 token이지만 refresh token이 살아있으면 만료 예외를 던진다")
    void authenticateWithExpiredButRefreshableToken() {
        Claims claims = claims("1", Role.USER);
        ExpiredJwtException expiredJwtException = new ExpiredJwtException(Jwts.header(), claims, "expired");
        SecurityUser securityUser = SecurityUser.builder()
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

        given(tokenUtils.extractClaims("expired-token")).willThrow(expiredJwtException);
        given(securityUserRepository.findById(1L)).willReturn(Optional.of(securityUser));
        given(refreshTokenRepository.findByRoleAndUserId(1L, Role.USER)).willReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> jwtAuthenticationProvider
                .authenticate(JwtAuthenticationToken.unauthenticated("expired-token")))
                .isInstanceOf(CredentialsExpiredException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("token 문자열이 비어있으면 인증에 실패한다")
    void authenticateWithEmptyToken() {
        assertThatThrownBy(() -> jwtAuthenticationProvider
                .authenticate(JwtAuthenticationToken.unauthenticated("")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Token string is null");
    }

    private Claims claims(String id, Role role) {
        Claims claims = Jwts.claims();
        claims.put("id", id);
        claims.put("role", role.value);
        return claims;
    }
}
