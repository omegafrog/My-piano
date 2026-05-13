package com.omegafrog.My.piano.app.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;

import io.jsonwebtoken.Claims;

class TokenUtilsTest {

    @Test
    @DisplayName("기존 refresh token을 유지하면서 access token을 새로 발급해야 한다.")
    void refreshAccessTokenKeepsRefreshTokenAndIssuesNewAccessToken() {
        TokenUtils tokenUtils = new TokenUtils();
        ReflectionTestUtils.setField(tokenUtils, "secret",
                "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890bcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890");
        ReflectionTestUtils.setField(tokenUtils, "accessTokenExpirationPeriod", "60000");
        RefreshToken refreshToken = RefreshToken.builder()
                .id("USER-1")
                .payload("refresh-token")
                .userId(1L)
                .role(Role.USER)
                .expiration(600000L)
                .createdAt(LocalDateTime.now())
                .build();

        TokenInfo tokenInfo = tokenUtils.refreshAccessToken("1", Role.USER, refreshToken);
        Claims claims = tokenUtils.extractClaims(tokenInfo.getAccessToken());

        assertThat(tokenInfo.getGrantType()).isEqualTo("Bearer");
        assertThat(tokenInfo.getRefreshToken()).isSameAs(refreshToken);
        assertThat(claims.get("id")).isEqualTo("1");
        assertThat(claims.get("role")).isEqualTo(Role.USER.value);
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }
}
