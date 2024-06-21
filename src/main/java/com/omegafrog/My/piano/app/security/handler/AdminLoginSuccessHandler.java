package com.omegafrog.My.piano.app.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenInfo;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.web.response.success.ApiSuccessResponse;
import jakarta.servlet.ServletException;
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

@RequiredArgsConstructor
@Slf4j
public class AdminLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final ObjectMapper objectMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenUtils tokenUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.debug("login success");
        PrintWriter writer = response.getWriter();
        Map<String, Object> data = new HashMap<>();
        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        TokenInfo tokenInfo = tokenUtils.generateToken(String.valueOf(user.getId()), user.getRole());
        Optional<RefreshToken> founded = refreshTokenRepository.findByRoleAndUserId(user.getId(), Role.ADMIN);

        if (founded.isPresent())
            founded.get().updateRefreshToken(tokenInfo.getRefreshToken().getPayload());
        else
            founded = Optional.of(refreshTokenRepository.save(tokenInfo.getRefreshToken()));

        data.put("access token", tokenInfo.getGrantType() + " " + tokenInfo.getAccessToken());
        tokenUtils.setRefreshToken(response, tokenInfo);
        ApiSuccessResponse loginSuccess = new ApiSuccessResponse("login success", data);
        String s = objectMapper.writeValueAsString(loginSuccess);

        writer.write(s);
        writer.flush();
    }

}
