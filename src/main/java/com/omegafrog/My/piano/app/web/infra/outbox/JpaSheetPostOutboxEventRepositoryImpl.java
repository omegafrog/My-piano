package com.omegafrog.My.piano.app.web.infra.outbox;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.omegafrog.My.piano.app.web.domain.outbox.SheetPostOutboxEvent;
import com.omegafrog.My.piano.app.web.domain.outbox.SheetPostOutboxEventRepository;
import com.omegafrog.My.piano.app.web.domain.outbox.SheetPostOutboxEventStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaSheetPostOutboxEventRepositoryImpl implements SheetPostOutboxEventRepository {

    private final SimpleJpaSheetPostOutboxEventRepository jpaRepository;

    @Override
    public SheetPostOutboxEvent save(SheetPostOutboxEvent event) {
        return jpaRepository.save(event);
    }

    @Override
    public List<SheetPostOutboxEvent> findProcessable(LocalDateTime now, int batchSize) {
        return jpaRepository.findByStatusInAndNextAttemptAtLessThanEqualOrderByIdAsc(
                List.of(SheetPostOutboxEventStatus.PENDING, SheetPostOutboxEventStatus.FAILED),
                now,
                PageRequest.of(0, Math.max(1, batchSize)));
    }
}
