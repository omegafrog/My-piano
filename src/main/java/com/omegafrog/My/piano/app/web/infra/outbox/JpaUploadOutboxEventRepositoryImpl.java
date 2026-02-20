package com.omegafrog.My.piano.app.web.infra.outbox;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.omegafrog.My.piano.app.web.domain.outbox.UploadOutboxEvent;
import com.omegafrog.My.piano.app.web.domain.outbox.UploadOutboxEventRepository;
import com.omegafrog.My.piano.app.web.domain.outbox.UploadOutboxEventStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaUploadOutboxEventRepositoryImpl implements UploadOutboxEventRepository {

    private final SimpleJpaUploadOutboxEventRepository jpaRepository;

    @Override
    public UploadOutboxEvent save(UploadOutboxEvent event) {
        return jpaRepository.save(event);
    }

    @Override
    public List<UploadOutboxEvent> findProcessable(LocalDateTime now, int batchSize) {
        return jpaRepository.findByStatusInAndNextAttemptAtLessThanEqualOrderByIdAsc(
                List.of(UploadOutboxEventStatus.PENDING, UploadOutboxEventStatus.FAILED),
                now,
                PageRequest.of(0, Math.max(1, batchSize)));
    }
}
