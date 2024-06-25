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
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {
    private final TokenUtils tokenUtils;
    private final SecurityUserRepository securityUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessTokenString = null;
        try{
            accessTokenString = tokenUtils.getAccessTokenString(request.getHeader(HttpHeaders.AUTHORIZATION));
        }catch (AuthenticationCredentialsNotFoundException e){
            filterChain.doFilter(request, response);
            return;
        }

        RefreshToken refreshToken= null;
        try{
            Claims claims = tokenUtils.extractClaims(accessTokenString);
            Long securityUserId = Long.valueOf((String) claims.get("id"));
            Role securityUserRole = Role.valueOf((String) claims.get("role"));

            SecurityUser user = securityUserRepository.findById(securityUserId)
                    .orElseThrow(EntityNotFoundException::new);
            refreshToken = refreshTokenRepository.findByRoleAndUserId(securityUserId,securityUserRole )
                    .orElseThrow(EntityNotFoundException::new);

            TokenInfo tokenInfo = tokenUtils.wrap(accessTokenString, refreshToken);

            JwtAuthenticationToken jwtAuthenticationToken =
                    new JwtAuthenticationToken(user.getAuthorities(),tokenInfo,securityUserId);
            jwtAuthenticationToken.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);

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
