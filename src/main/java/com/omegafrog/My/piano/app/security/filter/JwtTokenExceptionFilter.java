package com.omegafrog.My.piano.app.security.filter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.utils.response.APIForbiddenResponse;
import com.omegafrog.My.piano.app.utils.response.APIInternalServerResponse;
import com.omegafrog.My.piano.app.utils.response.APIUnauthorizedResponse;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtTokenExceptionFilter extends OncePerRequestFilter {
    @Autowired
    private ObjectMapper objectMapper;
    private String responseBody=null;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try {
            filterChain.doFilter(request, response);
        }catch (AuthenticationException ex){
            if (ex instanceof CredentialsExpiredException)
                responseBody = ex.getMessage();
        }
        catch(RuntimeException e){
            responseBody = e.getMessage();
        }finally {
            if(responseBody !=null){
                log.error(responseBody);
                response.getWriter().write(
                        objectMapper.writeValueAsString(new APIUnauthorizedResponse(responseBody))
                );
            }
        }
    }



}
