package com.omegafrog.My.piano.app.web.util;

import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.util.response.APIInternalServerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class AuthenticationUtil {
    public static User getLoggedInUser()throws AccessDeniedException{
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.error("authentication is null");
            throw new AccessDeniedException("authentication is null");
        }
        return ((SecurityUser) authentication.getPrincipal()).getUser();
    }

}
