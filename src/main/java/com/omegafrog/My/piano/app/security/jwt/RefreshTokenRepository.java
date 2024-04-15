package com.omegafrog.My.piano.app.security.jwt;

import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository  {

    RefreshToken save(RefreshToken token);

    List<RefreshToken> findAll();

    Optional<RefreshToken> findByRoleAndUserId(@NonNull Long userId, Role role);
    void deleteByUserIdAndRole(@NonNull Long userId, Role role);
    void deleteById(String id);
    void deleteAll();

    List<RefreshToken> findAllByRole(Role[] role, Pageable pageable);

    Long countByRole(Role role);
}
