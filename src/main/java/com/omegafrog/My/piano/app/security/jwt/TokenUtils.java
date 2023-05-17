package com.omegafrog.My.piano.app.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;

import java.util.Arrays;
import java.util.Date;

public class TokenUtils {
    //토큰 생성
    public static TokenInfo generateToken(String userId, String secret){
        int accessTokenExpirationPeriod = 60*60*60*10;
        int refreshTokenExpirationPeriod = 60*60*60*60;
        String accessToken = getToken(userId, accessTokenExpirationPeriod, secret);
        String refreshToken = getToken(null, refreshTokenExpirationPeriod, secret);
        return TokenInfo.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(RefreshToken.builder()
                        .userId(Long.valueOf(userId))
                        .refreshToken(refreshToken)
                        .build())
                .build();
    }

    public static String expireToken(String accessToken, String secret){
        String userId = extractClaims(accessToken,secret).getId();
        return getToken(userId, 0, secret);

    }

    private static String getToken(String payload, int expirationPeriod, String secret) {
        Claims claims = Jwts.claims();
        claims.put("id", payload);
        JwtBuilder jwtBuilder = Jwts.builder()
                .addClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationPeriod))
                .signWith(SignatureAlgorithm.HS512, secret);
        return jwtBuilder.compact();
    }

    public static String getAccessTokenStringFromHeaders(HttpServletRequest request)throws AuthenticationException {
        String tokenString = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (tokenString != null){
            String[] tokenSplit = tokenString.split(" ");
            if(verifyAccessTokenString(tokenSplit))
                return tokenSplit[1];
        }
        throw new AuthenticationCredentialsNotFoundException("Invalid Access token");
    }

    public static String getRefreshTokenStringFromCookies(HttpServletRequest request) throws AuthenticationException {
        Cookie[] cookies = request.getCookies();
        return Arrays.stream(cookies).filter(cookie ->
                cookie.getName().equals("refreshToken")
        ).findFirst().orElseThrow(
                ()-> new AuthenticationCredentialsNotFoundException("Invalid refresh token")
        ).getValue();
    }

    private static boolean verifyAccessTokenString(String[] accessToken) throws AuthenticationException{
        if(accessToken.length==2){
            String[] splitted = accessToken[1].split("\\.");
            return  accessToken[0].equals("Bearer") && splitted.length==3 ;
        }else {
            throw new AuthenticationCredentialsNotFoundException("Invalid access token");
        }
    }

    public static Claims extractClaims(String token, String secret){
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }


    //토큰 유효성 검증

    public static boolean isNonExpired(String token, String secret ){
        Claims body = extractClaims(token, secret);
        return body.getExpiration().before(new Date());
    }


}
