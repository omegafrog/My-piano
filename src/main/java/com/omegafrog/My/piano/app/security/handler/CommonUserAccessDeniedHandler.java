package com.omegafrog.My.piano.app.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.web.response.APIForbiddenResponse;
import com.omegafrog.My.piano.app.web.response.APIUnauthorizedResponse;
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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            body = objectMapper.writeValueAsString(new APIUnauthorizedResponse(accessDeniedException.getMessage()).getBody());
        }else{
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            body = objectMapper.writeValueAsString(new APIForbiddenResponse(accessDeniedException.getMessage()).getBody());
        }
        response.getWriter().write(body);
    }
}
