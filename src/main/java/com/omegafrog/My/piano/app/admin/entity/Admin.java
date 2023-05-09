package com.omegafrog.My.piano.app.admin.entity;


import com.omegafrog.My.piano.app.admin.dto.AdminDto;
import com.omegafrog.My.piano.app.admin.dto.UpdateAdminDto;
import com.omegafrog.My.piano.app.enums.Position;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@NoArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    private String name;
    private Position position;
    private String email;

    @Builder
    public Admin(String name, Position position, String email) {
        this.name = name;
        this.position = position;
        this.email = email;
    }

    public Admin update(UpdateAdminDto dto) {
        this.name = dto.getName();
        this.position = dto.getPosition();
        this.email = dto.getEmail();
        return this;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Admin admin = (Admin) o;

        return id.equals(admin.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public AdminDto toDto(){
        return AdminDto.builder()
                .name(this.name)
                .email(this.email)
                .position(this.position)
                .build();
    }

}
