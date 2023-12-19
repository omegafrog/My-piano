package com.omegafrog.My.piano.app.security.jwt;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository  {

    RefreshToken save(RefreshToken token);

    List<RefreshToken> findAll();

    Optional<RefreshToken> findByUserId(@NonNull Long userId);
    void deleteByUserId(@NonNull Long userId);
    void deleteById(Long id);
    void deleteAll();

}
