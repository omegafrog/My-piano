package com.omegafrog.My.piano.app.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.response.APISuccessResponse;
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

@Slf4j
@RequiredArgsConstructor
public class CommonUserLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.debug("login success");
        PrintWriter writer = response.getWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("loggedInUser", authentication);
        APISuccessResponse loginSuccess = new APISuccessResponse("login success", objectMapper, data);
        String s = objectMapper.writeValueAsString(loginSuccess);
        writer.write(s);
        writer.flush();
    }
}
