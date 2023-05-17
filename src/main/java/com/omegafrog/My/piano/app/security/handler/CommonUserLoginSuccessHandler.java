package com.omegafrog.My.piano.app.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.response.APISuccessResponse;
import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenInfo;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.user.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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

@Slf4j
@RequiredArgsConstructor
public class CommonUserLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    private final RefreshTokenRepository refreshTokenRepository;

    private final String secret;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.debug("login success");
        PrintWriter writer = response.getWriter();
        Map<String, Object> data = new HashMap<>();
        User user = (User)authentication.getDetails();
        TokenInfo tokenInfo = TokenUtils.generateToken(String.valueOf(user.getId()), secret);
        RefreshToken savedRefreshToken = refreshTokenRepository.save(tokenInfo.getRefreshToken());
        data.put("access token", tokenInfo.getAccessToken());
        response.addCookie(
                new Cookie("refreshToken", savedRefreshToken.getRefreshToken())
        );
        APISuccessResponse loginSuccess = new APISuccessResponse("login success", objectMapper, data);
        String s = objectMapper.writeValueAsString(loginSuccess).replaceAll("\\\\","");
        s = s.replaceAll("\"\\{","{");
        s = s.replaceAll("}\"","}");
        writer.write(s);
        writer.flush();
    }
}
