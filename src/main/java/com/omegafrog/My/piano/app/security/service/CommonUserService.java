package com.omegafrog.My.piano.app.security.service;

import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

@RequiredArgsConstructor
public class CommonUserService implements UserDetailsService {

    private final SecurityUserRepository securityUserRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<SecurityUser> founded = securityUserRepository.findByUsername(username);
        if(founded.isPresent()){
            return founded.get();
        }else{
            throw new UsernameNotFoundException("No such user.");
        }
    }
}
