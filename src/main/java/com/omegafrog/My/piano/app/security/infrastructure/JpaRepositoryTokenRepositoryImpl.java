package com.omegafrog.My.piano.app.security.infrastructure;

import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class JpaRepositoryTokenRepositoryImpl implements RefreshTokenRepository {

    @Autowired
    private JpaRefreshTokenRepository jpaRepository;

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        return jpaRepository.save(token);
    }

    @Override
    public List<RefreshToken> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Optional<RefreshToken> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        jpaRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
