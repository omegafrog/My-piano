package com.omegafrog.My.piano.app.web.dto.admin;

import com.omegafrog.My.piano.app.security.entity.authorities.Role;

public record ControlUserDto(Role role,
                             Boolean locked,
                             Boolean remove) {
}
