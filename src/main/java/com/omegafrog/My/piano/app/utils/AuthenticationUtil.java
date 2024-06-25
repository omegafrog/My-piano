package com.omegafrog.My.piano.app.utils;

import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.web.domain.admin.Admin;
import com.omegafrog.My.piano.app.web.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationUtil {
    private final SecurityUserRepository securityUserRepository;
    public User getLoggedInUser() throws AccessDeniedException{
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("authentication is null");
            throw new AccessDeniedException("authentication is null");
        }

        Long securityUserId = (Long) authentication.getPrincipal();
        return securityUserRepository.findById(securityUserId)
                .orElseThrow(() -> new AccessDeniedException("user not found"))
                .getUser();
    }
}
