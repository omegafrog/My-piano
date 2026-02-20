package com.omegafrog.My.piano.app.web.infra.outbox;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.omegafrog.My.piano.app.web.domain.outbox.PostOutboxEvent;
import com.omegafrog.My.piano.app.web.domain.outbox.PostOutboxEventRepository;
import com.omegafrog.My.piano.app.web.domain.outbox.PostOutboxEventStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaPostOutboxEventRepositoryImpl implements PostOutboxEventRepository {

    private final SimpleJpaPostOutboxEventRepository jpaRepository;

    @Override
    public PostOutboxEvent save(PostOutboxEvent event) {
        return jpaRepository.save(event);
    }

    @Override
    public List<PostOutboxEvent> findProcessable(LocalDateTime now, int batchSize) {
        return jpaRepository.findByStatusInAndNextAttemptAtLessThanEqualOrderByIdAsc(
                List.of(PostOutboxEventStatus.PENDING, PostOutboxEventStatus.FAILED),
                now,
                PageRequest.of(0, Math.max(1, batchSize)));
    }
}
