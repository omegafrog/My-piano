package com.omegafrog.My.piano.app.web.infra.Subscription;

import com.omegafrog.My.piano.app.web.domain.notification.Subscription;
import com.omegafrog.My.piano.app.web.domain.notification.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SubscriptionRepositoryImpl implements SubscriptionRepository {

    @Autowired
    private RedisSubscriptionRepository repository;

    @Override
    public Optional<Subscription> findByUserId(Long userId) {
        return repository.findByUserId( userId);
    }

    @Override
    public Subscription save(Subscription subscription) {
        return repository.save(subscription);
    }

    @Override
    public void deleteByUserId(Long userId) {
        repository.deleteByUserId(userId);
    }
}
