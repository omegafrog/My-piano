package com.omegafrog.My.piano.app.security.infrastructure.redis;

import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;


public interface RedisRefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    List<RefreshToken> findAll();

    Optional<RefreshToken> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    List<RefreshToken> findByRole(Role role, Pageable pageable);
}
