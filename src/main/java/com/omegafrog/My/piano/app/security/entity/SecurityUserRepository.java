package com.omegafrog.My.piano.app.security.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SecurityUserRepository extends JpaRepository<SecurityUser, Long> {
    SecurityUser save(SecurityUser securityUser);
    Optional<SecurityUser> findByUsername(String username);

    Optional<SecurityUser> findById(Long id);
    void deleteById(Long id);
}
