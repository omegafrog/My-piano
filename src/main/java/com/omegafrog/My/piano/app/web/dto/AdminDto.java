package com.omegafrog.My.piano.app.web.dto;

import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.web.enums.Position;
import lombok.Builder;
import lombok.Data;

@Data
public class AdminDto {
    private String id;

    private String name;
    private String email;
    private Role role;

    @Builder
    public AdminDto(String id, String name, String email, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
