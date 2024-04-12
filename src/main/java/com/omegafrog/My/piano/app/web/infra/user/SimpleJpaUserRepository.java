package com.omegafrog.My.piano.app.web.infra.user;

import com.omegafrog.My.piano.app.web.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SimpleJpaUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Page<User> findAll(Pageable pageable);

}
