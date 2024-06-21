package com.omegafrog.My.piano.app.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.web.response.APIForbiddenSuccessResponse;
import com.omegafrog.My.piano.app.web.response.APIUnauthorizedSuccessResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class CommonUserAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setCharacterEncoding("utf-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body;
        if(accessDeniedException.getCause() instanceof AuthenticationCredentialsNotFoundException){
            body = objectMapper.writeValueAsString(new APIUnauthorizedSuccessResponse(accessDeniedException.getMessage()));
        }else{
            body = objectMapper.writeValueAsString(new APIForbiddenSuccessResponse(accessDeniedException.getMessage()));
        }
        response.getWriter().write(body);
    }
}
