package com.omegafrog.My.piano.app.web.infra.Subscription;

import com.omegafrog.My.piano.app.web.domain.notification.Subscription;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RedisSubscriptionRepository extends CrudRepository<Subscription, String> {
    Optional<Subscription> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
