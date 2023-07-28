package com.omegafrog.My.piano.app.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.web.util.response.APISuccessResponse;
import com.omegafrog.My.piano.app.web.util.response.ResponseUtil;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import java.io.IOException;


@RequiredArgsConstructor
public class CommonUserLogoutHandler implements LogoutHandler {

    private final ObjectMapper objectMapper;
    private final LogoutBlacklistRepository logoutBlacklistRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 임시로 accessToken의 만료시간을 0으로 해서 로그아웃시켰지만, 클라이언트에서 Token을 변조할 수
        // 있으므로 로그아웃한 access token이 들어오면 블랙리스트에 추가시켜 놓고 만료시간까지 로그인하지 못하게
        // 해야 한다.
        String accessToken = TokenUtils.getAccessTokenStringFromHeaders(request);
        if(logoutBlacklistRepository.isPresent(accessToken))
            return;

        logoutBlacklistRepository.save(accessToken);
        try {
            ResponseUtil.writeResponse(new APISuccessResponse("logout success"), response, objectMapper);
        } catch (IOException e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }
}
