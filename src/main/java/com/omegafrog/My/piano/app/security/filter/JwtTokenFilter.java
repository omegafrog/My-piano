package com.omegafrog.My.piano.app.security.filter;

import com.nimbusds.oauth2.sdk.GrantType;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.security.jwt.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.omegafrog.My.piano.app.security.entity.QSecurityUser.securityUser;

@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {
    private final TokenUtils tokenUtils;
    private final SecurityUserRepository securityUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(request.getHeader(HttpHeaders.AUTHORIZATION) == null) {
            filterChain.doFilter(request, response);
            return;
        }
        String accessTokenString = tokenUtils.getAccessTokenString(request.getHeader(HttpHeaders.AUTHORIZATION));
        RefreshToken refreshToken= null;

        SecurityUser user=null;
        try{
            Claims claims = tokenUtils.extractClaims(accessTokenString);
            Long userId = Long.valueOf((String) claims.get("id"));
            user = securityUserRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
            refreshToken = refreshTokenRepository.findByRoleAndUserId(userId, Role.valueOf((String) claims.get("role"))).orElseThrow(EntityNotFoundException::new);
            TokenInfo tokenInfo = new TokenInfo(GrantType.JWT_BEARER.getValue(), accessTokenString,refreshToken );
            JwtFilterToken jwtFilterToken = new JwtFilterToken(user.getAuthorities(),tokenInfo,user);
            jwtFilterToken.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(jwtFilterToken);
        }catch (EntityNotFoundException e){
            log.error("{} token:{}",e.getMessage(),accessTokenString);
            if(refreshToken == null)
                throw new AuthenticationServiceException("Already logged out user.", e);
            throw new AuthenticationServiceException("Cannot find logged in user.", e);
        }catch (ExpiredJwtException e){
            log.error("{} token:{}",e.getMessage(),accessTokenString);
            throw new CredentialsExpiredException("Access token is expired.", e);
        }catch (SignatureException e){
            log.error("{} token:{}",e.getMessage(),accessTokenString);
            throw new InsufficientAuthenticationException("Signature is invalid", e);
        }catch (JwtException e){
            log.error("{} token:{}",e.getMessage(),accessTokenString);
            throw new AuthenticationServiceException("Wrong token error. token:"+accessTokenString, e);
        }finally{
            filterChain.doFilter(request, response);
        }

    }
}
