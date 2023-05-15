package com.omegafrog.My.piano.app.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;

public class TokenProvider {
    //토큰 생성
    public static TokenInfo generateToken(String userId, String secret){
        int accessTokenExpirationPeriod = 3600;
        int refreshTokenExpirationPeriod = 3600 * 60;
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

    public static Claims extractClaims(String token, String secret){
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }


    //토큰 유효성 검증

    public static boolean isNonExpired(String token, String secret ){
        Claims body = extractClaims(token, secret);
        return body.getExpiration().before(new Date());
    }


}
