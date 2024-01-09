package com.omegafrog.My.piano.app.security.infrastructure.redis;

import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;


public interface RedisRefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
    List<RefreshToken> findAll();

    Optional<RefreshToken> findByUserId(Long userId);

    void deleteByUserId(Long userId);

}
