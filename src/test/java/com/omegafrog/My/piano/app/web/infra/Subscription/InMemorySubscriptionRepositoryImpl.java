package com.omegafrog.My.piano.app.web.infra.Subscription;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.omegafrog.My.piano.app.TestResettable;
import com.omegafrog.My.piano.app.web.domain.notification.Subscription;
import com.omegafrog.My.piano.app.web.domain.notification.SubscriptionRepository;

@Repository
@Profile("test")
public class InMemorySubscriptionRepositoryImpl implements SubscriptionRepository, TestResettable {

    private final Map<Long, Subscription> byUserId = new ConcurrentHashMap<>();

    @Override
    public Optional<Subscription> findByUserId(Long userId) {
        return Optional.ofNullable(byUserId.get(userId));
    }

    @Override
    public Subscription save(Subscription subscription) {
        byUserId.put(subscription.getUserId(), subscription);
        return subscription;
    }

    @Override
    public void deleteByUserId(Long userId) {
        byUserId.remove(userId);
    }

    @Override
    public void reset() {
        byUserId.clear();
    }
}
