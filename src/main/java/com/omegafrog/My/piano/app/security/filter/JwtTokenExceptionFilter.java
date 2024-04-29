package com.omegafrog.My.piano.app.security.filter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.utils.response.APIForbiddenResponse;
import com.omegafrog.My.piano.app.utils.response.APIInternalServerResponse;
import com.omegafrog.My.piano.app.utils.response.APIUnauthorizedResponse;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtTokenExceptionFilter extends OncePerRequestFilter {
    @Autowired
    private ObjectMapper objectMapper;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (BadCredentialsException ex) {
            ex.printStackTrace();
            response.setCharacterEncoding("utf-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    objectMapper.writeValueAsString(new APIUnauthorizedResponse(ex.getMessage()))
            );
        }catch (AuthenticationCredentialsNotFoundException ex){
            ex.printStackTrace();
            response.setCharacterEncoding("utf-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    objectMapper.writeValueAsString(new APIForbiddenResponse(ex.getMessage()))
            );
        }catch(RuntimeException e){
            response.setCharacterEncoding("utf-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    objectMapper.writeValueAsString(new APIInternalServerResponse(e.getMessage()))
            );
        }
    }



}
