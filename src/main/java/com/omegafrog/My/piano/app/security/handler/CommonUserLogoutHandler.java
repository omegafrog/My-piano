package com.omegafrog.My.piano.app.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
public class CommonUserLogoutHandler implements LogoutHandler {

    private final ObjectMapper objectMapper;
    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TokenUtils tokenUtils;

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // refreshTokenRepository에서 로그아웃한 유저의 refresh token을 삭제하면 됨.
        String accessToken = tokenUtils.getAccessTokenString(request.getHeader(HttpHeaders.AUTHORIZATION));
        Claims claims = tokenUtils.extractClaims(accessToken);
        Long userId = Long.valueOf((String) claims.get("id"));
        refreshTokenRepository.deleteByUserIdAndRole(userId, Role.valueOf((String) claims.get("role")));
    }
}
