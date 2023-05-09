package com.omegafrog.My.piano.app.security.provider;

import com.omegafrog.My.piano.app.security.service.CommonUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
public class CommonUserAuthenticationProvider implements AuthenticationProvider {


    private final CommonUserService commonUserService;
    private final PasswordEncoder passwordEncoder;
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UserDetails foundedUserDetails = commonUserService.loadUserByUsername(authentication.getPrincipal().toString());
        if(passwordEncoder.matches((CharSequence) authentication.getCredentials(), foundedUserDetails.getPassword())){
            return new UsernamePasswordAuthenticationToken(foundedUserDetails.getUsername(),
                    foundedUserDetails.getPassword(), foundedUserDetails.getAuthorities());
        }else{
            throw new BadCredentialsException("Password is not match.");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isInstance(UsernamePasswordAuthenticationToken.class);
    }
}
