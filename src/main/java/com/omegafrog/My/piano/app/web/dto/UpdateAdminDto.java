package com.omegafrog.My.piano.app.web.dto;

import com.omegafrog.My.piano.app.web.enums.Position;
import lombok.Builder;
import lombok.Data;

@Data
public class UpdateAdminDto {
    private String name;
    private Position position;
    private String email;

    @Builder
    public UpdateAdminDto(String name, Position position, String email) {
        this.name = name;
        this.position = position;
        this.email = email;
    }

}
