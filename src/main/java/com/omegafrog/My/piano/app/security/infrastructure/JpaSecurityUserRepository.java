package com.omegafrog.My.piano.app.security.infrastructure;

import com.omegafrog.My.piano.app.web.domain.user.SecurityUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaSecurityUserRepository extends JpaRepository<SecurityUser, Long> {
    Optional<SecurityUser> findByUsername(String username);

}
