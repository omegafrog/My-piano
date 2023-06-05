package com.omegafrog.My.piano.app.user.infrastructure;

import com.omegafrog.My.piano.app.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimpleJpaUserRepository extends JpaRepository<User, Long> {

}
