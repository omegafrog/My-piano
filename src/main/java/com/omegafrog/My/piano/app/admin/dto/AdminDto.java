package com.omegafrog.My.piano.app.admin.dto;

import com.omegafrog.My.piano.app.enums.Position;
import lombok.Builder;
import lombok.Data;

@Data
public class AdminDto {
    private Long id;

    private String name;
    private Position position;
    private String email;

    @Builder
    public AdminDto(Long id, String name, Position position, String email) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.email = email;
    }
}
