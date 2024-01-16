package com.omegafrog.My.piano.app.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenInfo;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final String secret;
    private final TokenUtils tokenUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.debug("login success");
        PrintWriter writer = response.getWriter();
        Map<String, Object> data = new HashMap<>();
        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        TokenInfo tokenInfo = tokenUtils.generateToken(String.valueOf(user.getId()));
        Optional<RefreshToken> founded = refreshTokenRepository.findByUserId(user.getId());

        if(founded.isPresent())
            founded.get().updateRefreshToken(tokenInfo.getRefreshToken().getRefreshToken());
        else
            founded = Optional.of(refreshTokenRepository.save(tokenInfo.getRefreshToken()));

        data.put("access token", tokenInfo.getGrantType() + " " + tokenInfo.getAccessToken());
        tokenUtils.setRefreshToken(response, tokenInfo);
        APISuccessResponse loginSuccess = new APISuccessResponse("login success", data);
        String s = objectMapper.writeValueAsString(loginSuccess);
//        s = s.replaceAll("\"\\{", "{");
//        s = s.replaceAll("}\"", "}");
//        s = s.replaceAll("\\\\\"", "\"");
        writer.write(s);
        writer.flush();
    }
}
