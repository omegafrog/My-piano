package com.omegafrog.My.piano.app.security.provider;

import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.service.CommonUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;


@RequiredArgsConstructor
@Slf4j
public class CommonUserAuthenticationProvider implements AuthenticationProvider {


    private final CommonUserService commonUserService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SecurityUser foundedUserDetails = (SecurityUser) commonUserService.loadUserByUsername(authentication.getPrincipal().toString());

        if (foundedUserDetails.isEnabled()) {
            if (passwordEncoder.matches((CharSequence) authentication.getCredentials(), foundedUserDetails.getPassword())) {
                return new UsernamePasswordAuthenticationToken(foundedUserDetails,
                        foundedUserDetails.getPassword(), foundedUserDetails.getAuthorities());
            } else {
                throw new BadCredentialsException("Password is not match.");
            }
        } else {
            if (!foundedUserDetails.isEnabled()) {
                throw new LockedException("Account is locked");
            } else if (!foundedUserDetails.isCredentialsNonExpired()) {
                throw new CredentialsExpiredException("Credential is expired");
            } else {
                throw new AuthenticationServiceException("Internal server error");
            }
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
