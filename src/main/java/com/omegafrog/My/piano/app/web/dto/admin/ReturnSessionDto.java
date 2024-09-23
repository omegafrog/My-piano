package com.omegafrog.My.piano.app.web.dto.admin;

import com.omegafrog.My.piano.app.web.domain.user.SecurityUser;
import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;

import java.time.LocalDateTime;

public record ReturnSessionDto(

        Long id, String name, String username, LoginMethod loginMethod,
        LocalDateTime loggedInAt, LocalDateTime createdAt, Role role) {
    public ReturnSessionDto(SecurityUser securityUser, LocalDateTime loggedInAt) {
        this(
                securityUser.getId(),
                securityUser.getUser().getName(),
                securityUser.getUsername(),
                securityUser.getUser().getLoginMethod(),
                loggedInAt,
                securityUser.getCreatedAt(),
                securityUser.getRole()
        );
    }
}
