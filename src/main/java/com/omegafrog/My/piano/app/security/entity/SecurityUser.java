package com.omegafrog.My.piano.app.security.entity;

import com.omegafrog.My.piano.app.dto.SecurityUserDto;
import com.omegafrog.My.piano.app.security.entity.authorities.Authority;
import com.omegafrog.My.piano.app.user.entity.User;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@NoArgsConstructor
public class SecurityUser implements UserDetails {

    @Transient
    private final long CREDENTIAL_EXPIRED_MONTH = 30L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private List<Authority> authorities=new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime credentialChangedAt;
    private boolean locked;

    @OneToOne(cascade = {CascadeType.MERGE,CascadeType.PERSIST})
    @JoinColumn(name = "USER_ID")
    private User user;

    @Builder
    public SecurityUser(String username, String password, User user) {
        this.username = username;
        this.password = password;
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SecurityUser that = (SecurityUser) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialChangedAt.plusMonths(CREDENTIAL_EXPIRED_MONTH)
                .isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isEnabled() {
        return isAccountNonExpired() || isCredentialsNonExpired();
    }

    public SecurityUserDto toDto(){
        return SecurityUserDto.builder()
                .createdAt(createdAt)
                .credentialChangedAt(credentialChangedAt)
                .authorities(authorities)
                .locked(locked)
                .id(id)
                .password(password)
                .username(username)
                .build();
    }
}
