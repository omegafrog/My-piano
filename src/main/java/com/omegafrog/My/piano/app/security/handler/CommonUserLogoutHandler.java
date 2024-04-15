package com.omegafrog.My.piano.app.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.ResponseUtil;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;


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
        String accessToken = tokenUtils.getAccessTokenStringFromHeaders(request);
        Claims claims = tokenUtils.extractClaims(accessToken);
        Long userId = Long.valueOf((String) claims.get("id"));
        refreshTokenRepository.deleteByUserIdAndRole(userId, Role.valueOf((String)claims.get("role")));
        try {
            ResponseUtil.writeResponse(new APISuccessResponse("logout success"), response, objectMapper);
        } catch (IOException e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }
}
