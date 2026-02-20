package com.omegafrog.My.piano.app.web.infra.outbox;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.omegafrog.My.piano.app.web.domain.outbox.UploadOutboxEvent;
import com.omegafrog.My.piano.app.web.domain.outbox.UploadOutboxEventStatus;

public interface SimpleJpaUploadOutboxEventRepository extends JpaRepository<UploadOutboxEvent, Long> {

    List<UploadOutboxEvent> findByStatusInAndNextAttemptAtLessThanEqualOrderByIdAsc(
            List<UploadOutboxEventStatus> statuses,
            LocalDateTime nextAttemptAt,
            Pageable pageable);
}
