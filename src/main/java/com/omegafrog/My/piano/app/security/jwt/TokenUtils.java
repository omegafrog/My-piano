package com.omegafrog.My.piano.app.security.jwt;

import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import io.jsonwebtoken.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;

import java.util.Arrays;
import java.util.Date;

public class TokenUtils {

    @Value("${security.jwt.accessToken.period}")
    private String accessTokenExpirationPeriod;

    @Value("${security.jwt.refreshToken.period}")
    private String refreshTokenExpirationPeriod;

    @Value("${security.jwt.secret}")
    private String secret;

    //토큰 생성
    public TokenInfo generateToken(String securityUserId) {
        String accessToken = getToken(securityUserId, Integer.parseInt(accessTokenExpirationPeriod));
        String refreshToken = getToken(null, Integer.parseInt(refreshTokenExpirationPeriod));
        return TokenInfo.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(RefreshToken.builder()
                        .userId(Long.valueOf(securityUserId))
                        .refreshToken(refreshToken)
                        .build())
                .build();
    }

    private  String getToken(String payload, int expirationPeriod) {
        Claims claims = Jwts.claims();
        claims.put("id", payload);
        Long curTime = System.currentTimeMillis();
        JwtBuilder jwtBuilder = Jwts.builder()
                .addClaims(claims)
                .setIssuedAt(new Date(curTime))
                .setExpiration(new Date(System.currentTimeMillis() + expirationPeriod))
                .signWith(SignatureAlgorithm.HS512, secret);
        return jwtBuilder.compact();
    }



    public  String getAccessTokenStringFromHeaders(HttpServletRequest request) throws AuthenticationException {
        String tokenString = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (tokenString != null) {
            String[] tokenSplit = tokenString.split(" ");
            if (verifyAccessTokenString(tokenSplit))
                return tokenSplit[1];
        }
        throw new AuthenticationCredentialsNotFoundException("Invalid Access token");
    }

    public  String getRefreshTokenStringFromCookies(HttpServletRequest request) throws AuthenticationException {
        Cookie[] cookies = request.getCookies();
        return Arrays.stream(cookies).filter(cookie ->
                cookie.getName().equals("refreshToken")
        ).findFirst().orElseThrow(
                () -> new AuthenticationCredentialsNotFoundException("Invalid refresh token")
        ).getValue();
    }

    private  boolean verifyAccessTokenString(String[] accessToken) throws AuthenticationException {
        if (accessToken.length == 2) {
            String[] splitted = accessToken[1].split("\\.");
            return accessToken[0].equals("Bearer") && splitted.length == 3;
        } else {
            throw new AuthenticationCredentialsNotFoundException("Invalid access token");
        }
    }

    public  Claims extractClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }


    //토큰 유효성 검증
    public  boolean isNonExpired(String token) {
        try{
            Claims body = extractClaims(token);
            return body.getExpiration().after(new Date());
        }catch (JwtException e){
            return false;
        }
    }

    public  void setRefreshToken(HttpServletResponse response, TokenInfo tokenInfo) {
        Cookie refreshToken = new Cookie("refreshToken", tokenInfo.getRefreshToken().getRefreshToken());
        refreshToken.setPath("/");
        refreshToken.setHttpOnly(true);
        response.addCookie(refreshToken);
    }

}
