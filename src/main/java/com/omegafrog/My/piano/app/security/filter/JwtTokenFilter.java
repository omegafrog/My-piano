package com.omegafrog.My.piano.app.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.response.APIBadRequestResponse;
import com.omegafrog.My.piano.app.response.ResponseUtil;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.handler.LogoutBlacklistRepository;
import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.utils.PathMatchUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    private final SecurityUserRepository securityUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LogoutBlacklistRepository logoutBlacklistRepository;

    private final String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws AuthenticationException, ServletException, IOException {

        if (PathMatchUtils.isMatched(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        // token 추출
        try {
            String accessToken = TokenUtils.getAccessTokenStringFromHeaders(request);
            String refreshToken = TokenUtils.getRefreshTokenStringFromCookies(request);
            //token으로부터 유저 추출
            Claims claims = TokenUtils.extractClaims(accessToken, secret);
            Long userId = Long.valueOf((String) claims.get("id"));

            //Logout된 유저의 access token이면 빠져나오기
            if (logoutBlacklistRepository.isPresent(accessToken)) {
                throw new SessionAuthenticationException("Already logged out user.");
            }
            SecurityUser user = securityUserRepository.findById(userId).orElseThrow(
                    () -> new AuthenticationCredentialsNotFoundException("Invalid access token")
            );
            // token이 만료되지 않음.
            if (TokenUtils.isNonExpired(accessToken, secret)) {
                // securityContextHolder에 request와 lifecycle이 같은 객체 저장.
                Authentication usernameToken = getAuthenticationToken(user);
                SecurityContextHolder.getContext().setAuthentication(usernameToken);
                filterChain.doFilter(request, response);
            }
            // 토큰이 만료되어 재발급함
            else {
                RefreshToken founded = refreshTokenRepository.findByUserId(userId).orElseThrow(
                        () -> new AuthenticationCredentialsNotFoundException("Invalid refresh token")
                );
                // 토큰이 동일하면 access token 재발급
                if (founded.getRefreshToken().equals(refreshToken)) {
                    response.setHeader(HttpHeaders.AUTHORIZATION,
                            TokenUtils.generateToken(userId.toString(), secret).getAccessToken());
                    Authentication usernameToken = getAuthenticationToken(user);
                    SecurityContextHolder.getContext().setAuthentication(usernameToken);
                    filterChain.doFilter(request, response);
                }
            }
        } catch (AuthenticationException e) {
            ResponseUtil.writeResponse(new APIBadRequestResponse(e.getMessage()), response, objectMapper);
        }
    }

    private static Authentication getAuthenticationToken(SecurityUser user) {
        UsernamePasswordAuthenticationToken usernameToken = new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, user.getAuthorities()
        );
        usernameToken.setDetails(user.getUser().getId());
        return usernameToken;
    }
}
