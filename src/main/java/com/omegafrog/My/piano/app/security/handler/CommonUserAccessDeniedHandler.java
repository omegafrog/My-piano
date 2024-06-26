package com.omegafrog.My.piano.app.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.utils.response.APIForbiddenResponse;
import com.omegafrog.My.piano.app.utils.response.APIUnauthorizedResponse;
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
        accessDeniedException.printStackTrace();
        response.setCharacterEncoding("utf-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body;
        if(accessDeniedException.getCause() instanceof AuthenticationCredentialsNotFoundException){
            body = objectMapper.writeValueAsString(new APIUnauthorizedResponse(accessDeniedException.getMessage()));
        }else{
            body = objectMapper.writeValueAsString(new APIForbiddenResponse(accessDeniedException.getMessage()));
        }
        response.getWriter().write(body);
    }
}
