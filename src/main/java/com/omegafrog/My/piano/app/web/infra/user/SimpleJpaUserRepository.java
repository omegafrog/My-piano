package com.omegafrog.My.piano.app.web.infra.user;

import com.omegafrog.My.piano.app.web.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SimpleJpaUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

}
