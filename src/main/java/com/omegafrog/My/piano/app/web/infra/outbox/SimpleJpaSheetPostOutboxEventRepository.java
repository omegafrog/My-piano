package com.omegafrog.My.piano.app.web.infra.outbox;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.omegafrog.My.piano.app.web.domain.outbox.SheetPostOutboxEvent;
import com.omegafrog.My.piano.app.web.domain.outbox.SheetPostOutboxEventStatus;

public interface SimpleJpaSheetPostOutboxEventRepository extends JpaRepository<SheetPostOutboxEvent, Long> {

    List<SheetPostOutboxEvent> findByStatusInAndNextAttemptAtLessThanEqualOrderByIdAsc(
            List<SheetPostOutboxEventStatus> statuses,
            LocalDateTime nextAttemptAt,
            Pageable pageable);
}
