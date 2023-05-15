package com.omegafrog.My.piano.app.security.jwt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByUserId(@NonNull Long userId);

    void deleteById(Long id);

}
