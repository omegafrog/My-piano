package com.omegafrog.My.piano.app.user.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User save(User user);

    Optional<User> findById(Long id);

    void deleteById(Long id);
}