package com.omegafrog.My.piano.app.security.infrastructure;

import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    long deleteByUserId(Long userId);
    Optional<RefreshToken> findByUserId(Long userId);

}
