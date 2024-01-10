package com.omegafrog.My.piano.app.web.domain.admin;


import com.omegafrog.My.piano.app.security.entity.authorities.Authority;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Entity
@NoArgsConstructor
@Getter
public class Admin implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;
    private String username;
    private String password;
    private String name;
    private String email;
    private Role role;

    @Builder
    public Admin(String username, String password, String name, String email, Role role) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public Admin update(UpdateAdminDto dto) {
        this.name = dto.getName();
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

    public AdminDto toDto() {
        return AdminDto.builder()
                .name(this.name)
                .email(this.email)
                .role(this.role)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<Authority> authorities = new ArrayList<>();
        authorities.add(new Authority(role.value));
        return authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
