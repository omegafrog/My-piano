package com.omegafrog.My.piano.app.web.domain.admin;


import com.omegafrog.My.piano.app.web.dto.AdminDto;
import com.omegafrog.My.piano.app.web.dto.UpdateAdminDto;
import com.omegafrog.My.piano.app.web.enums.Position;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


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
