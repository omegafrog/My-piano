package com.omegafrog.My.piano.app.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenInfo;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUser;
import com.omegafrog.My.piano.app.web.response.success.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class CommonUserLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenUtils tokenUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        PrintWriter writer = response.getWriter();
        Map<String, Object> data = new HashMap<>();
        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        log.debug("login success. {}:{}", user.getId(), user.getUsername());
        TokenInfo tokenInfo = tokenUtils.generateToken(String.valueOf(user.getId()), user.getRole());
        Optional<RefreshToken> founded = refreshTokenRepository.findByRoleAndUserId(user.getId(), user.getRole());

        if (founded.isPresent())
            founded.get().updateRefreshToken(tokenInfo.getRefreshToken().getPayload());
        else
            refreshTokenRepository.save(tokenInfo.getRefreshToken());

        data.put("access token", tokenInfo.getGrantType() + " " + tokenInfo.getAccessToken());
        tokenUtils.setRefreshToken(response, tokenInfo);
        ApiResponse<Map<String, Object>> loginSuccess = new ApiResponse<>("login success", data);

        String s = objectMapper.writeValueAsString(loginSuccess.getBody());

        writer.write(s);
        writer.flush();
    }
}
