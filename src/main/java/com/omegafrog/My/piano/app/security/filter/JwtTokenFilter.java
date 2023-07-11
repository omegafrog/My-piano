package com.omegafrog.My.piano.app.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.handler.LogoutBlacklistRepository;
import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    private final SecurityUserRepository securityUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LogoutBlacklistRepository logoutBlacklistRepository;

    private final String secret;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws AuthenticationException, ServletException, IOException {

        if (request.getRequestURI().contains("login") || request.getRequestURI().contains("register")) {
            filterChain.doFilter(request,response);
            return;
        }
        SecurityUser user=null;
        // token 추출
        try{
            String accessToken = TokenUtils.getAccessTokenStringFromHeaders(request);
            String refreshToken = TokenUtils.getRefreshTokenStringFromCookies(request);
            //token으로부터 유저 추출
            Claims claims = TokenUtils.extractClaims(accessToken, secret);
            Long userId = Long.valueOf((String)claims.get("id"));

            //Logout된 유저의 access token이면 빠져나오기
            if(logoutBlacklistRepository.isPresent(accessToken)){
                throw new SessionAuthenticationException("Already logged out user.");
            }
            // 토큰이 만료되어 재발급함
            if (!TokenUtils.isNonExpired(accessToken, secret)){
                RefreshToken founded = refreshTokenRepository.findByUserId(userId).orElseThrow(
                        ()->new AuthenticationCredentialsNotFoundException("Invalid refresh token")
                );
                // 토큰이 동일하면 access token 재발급
                if(founded.getRefreshToken().equals(refreshToken)){
                    response.setHeader(HttpHeaders.AUTHORIZATION,
                            TokenUtils.generateToken(userId.toString(),secret).getAccessToken());
                }
            }
            user = securityUserRepository.findById(userId).orElseThrow(
                            () -> new AuthenticationCredentialsNotFoundException("Invalid access token")
            );
            Authentication usernameToken = getAuthenticationToken(user);
            SecurityContextHolder.getContext().setAuthentication(usernameToken);
        }catch (AuthenticationException e){
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static Authentication getAuthenticationToken(SecurityUser user) {
        UsernamePasswordAuthenticationToken usernameToken = new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, user.getAuthorities()
        );
        usernameToken.setDetails(user.getUser());
        return usernameToken;
    }




}
