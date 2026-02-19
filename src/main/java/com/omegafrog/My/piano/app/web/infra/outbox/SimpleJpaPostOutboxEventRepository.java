package com.omegafrog.My.piano.app.web.infra.outbox;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.omegafrog.My.piano.app.web.domain.outbox.PostOutboxEvent;
import com.omegafrog.My.piano.app.web.domain.outbox.PostOutboxEventStatus;

public interface SimpleJpaPostOutboxEventRepository extends JpaRepository<PostOutboxEvent, Long> {

    List<PostOutboxEvent> findByStatusInAndNextAttemptAtLessThanEqualOrderByIdAsc(
            List<PostOutboxEventStatus> statuses,
            LocalDateTime nextAttemptAt,
            Pageable pageable);
}
