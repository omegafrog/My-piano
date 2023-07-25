package com.omegafrog.My.piano.app.web.domain.user;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    void deleteById(Long id);

    long count();

    void deleteAll();
}
