package com.omegafrog.My.piano.app.web.domain.notification;

import java.util.Optional;

public interface SubscriptionRepository {
    Optional<Subscription> findByUserId(Long userId);

    Subscription save(Subscription subscription);

    void deleteByUserId(Long userId);


}
