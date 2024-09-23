package com.omegafrog.My.piano.app.web.dto.admin;

import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;

public record ControlUserDto(Role role,
                             Boolean locked,
                             Boolean remove) {
}
