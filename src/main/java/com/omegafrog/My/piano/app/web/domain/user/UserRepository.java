package com.omegafrog.My.piano.app.web.domain.user;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    void deleteById(Long id);

    long count();

    void deleteAll();

    List<User> findAll(Pageable pageable);
}
