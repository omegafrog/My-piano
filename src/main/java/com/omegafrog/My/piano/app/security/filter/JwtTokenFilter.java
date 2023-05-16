package com.omegafrog.My.piano.app.security.filter;

import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final SecurityUserRepository securityUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final String secret;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws AuthenticationException, ServletException, IOException {

        if(request.getRequestURI().contains("login")) {
            filterChain.doFilter(request, response);
            return;
        }


        // token 추출
        String accessToken = getAccessTokenStringFromHeaders(request);
        String refreshToken = getRefreshTokenStringFromCookies(request);

        //token으로부터 유저 추출
        Claims claims = TokenProvider.extractClaims(accessToken, secret);
        Long userId = Long.valueOf(claims.getId());

        // token이 만료되지 않음.
        if (TokenProvider.isNonExpired(accessToken, secret)) {
            SecurityUser user = securityUserRepository.findById(userId).orElseThrow(
                    ()->new AuthenticationCredentialsNotFoundException("Invalid access token.")
            );
            // securityContextHolder에 request와 lifecycle이 같은 객체 저장.
            Authentication usernameToken = getAuthenticationToken(user);
            SecurityContextHolder.getContext().setAuthentication(usernameToken);

        }
        // 토큰이 만료되어 재발급함
        else{
            RefreshToken founded = refreshTokenRepository.findByUserId(userId)
                    .orElseThrow(
                            ()->new AuthenticationCredentialsNotFoundException("Invalid refresh token")
                    );
            // 토큰이 동일하면 access token 재발급
            if(founded.getRefreshToken().equals(refreshToken)){
                response.setHeader(HttpHeaders.AUTHORIZATION,
                        TokenProvider.generateToken(userId.toString(),secret).getAccessToken());
            }
        }
    }

    private static Authentication getAuthenticationToken(SecurityUser user) {
        UsernamePasswordAuthenticationToken usernameToken = new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, user.getAuthorities()
        );
        usernameToken.setDetails(user.getUser());
        return usernameToken;
    }

    private static String getAccessTokenStringFromHeaders(HttpServletRequest request)throws AuthenticationException{
        String tokenString = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (tokenString != null){
            String[] tokenSplit = tokenString.split(" ");
            if(verifyAccessTokenString(tokenSplit))
                return tokenSplit[1];
        }
        throw new AuthenticationCredentialsNotFoundException("Invalid Access token");
    }

    private static String getRefreshTokenStringFromCookies(HttpServletRequest request) throws AuthenticationException {
        Cookie[] cookies = request.getCookies();
        return Arrays.stream(cookies).filter(cookie ->
                cookie.getName().equals("RefreshToken")
        ).findFirst().orElseThrow(
                ()-> new AuthenticationCredentialsNotFoundException("Invalid refresh token")
        ).getValue();
    }

    private static boolean verifyAccessTokenString(String[] accessToken) {
        String[] splitted = accessToken[1].split("\\.");
        return accessToken.length == 2 && accessToken[0].equals("Bearer") && splitted.length==3 ;
    }
}
