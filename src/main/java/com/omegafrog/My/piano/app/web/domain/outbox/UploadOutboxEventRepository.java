package com.omegafrog.My.piano.app.web.domain.outbox;

import java.time.LocalDateTime;
import java.util.List;

public interface UploadOutboxEventRepository {

    UploadOutboxEvent save(UploadOutboxEvent event);

    List<UploadOutboxEvent> findProcessable(LocalDateTime now, int batchSize);
}
