package com.omegafrog.My.piano.app.web.infra.outbox;

import org.springframework.stereotype.Repository;

import com.omegafrog.My.piano.app.web.domain.outbox.ProcessedPostEvent;
import com.omegafrog.My.piano.app.web.domain.outbox.ProcessedPostEventRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaProcessedPostEventRepositoryImpl implements ProcessedPostEventRepository {

    private final SimpleJpaProcessedPostEventRepository jpaRepository;

    @Override
    public boolean existsByEventId(String eventId) {
        return jpaRepository.existsById(eventId);
    }

    @Override
    public ProcessedPostEvent save(ProcessedPostEvent processedPostEvent) {
        return jpaRepository.save(processedPostEvent);
    }
}
