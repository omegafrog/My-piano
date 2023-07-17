package com.omegafrog.My.piano.app.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.web.response.APIBadRequestResponse;
import com.omegafrog.My.piano.app.web.response.APIInternalServerResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.io.PrintWriter;

@RequiredArgsConstructor
public class AuthenticationExceptionEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String message = authException.getMessage();
        String s;
        // 내부 서버 문제
        if (authException instanceof AuthenticationServiceException) {
            APIInternalServerResponse apiInternalServerResponse = new APIInternalServerResponse(message);
             s=objectMapper.writeValueAsString(apiInternalServerResponse);
        }else{
            APIBadRequestResponse apiBadRequestResponse = new APIBadRequestResponse(message);
            s = objectMapper.writeValueAsString(apiBadRequestResponse);
        }
        PrintWriter writer = response.getWriter();
        writer.write(s);
        writer.flush();
    }
}
