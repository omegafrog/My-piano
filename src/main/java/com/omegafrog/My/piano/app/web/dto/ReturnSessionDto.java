package com.omegafrog.My.piano.app.web.dto;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;

import java.time.LocalDateTime;

public record ReturnSessionDto(

        Long id, String name, String username, LoginMethod loginMethod,
        LocalDateTime loggedInAt, LocalDateTime createdAt, Role role) {
}
