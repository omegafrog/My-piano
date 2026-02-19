package com.omegafrog.My.piano.app.web.domain.outbox;

import java.time.LocalDateTime;
import java.util.List;

public interface SheetPostOutboxEventRepository {

    SheetPostOutboxEvent save(SheetPostOutboxEvent event);

    List<SheetPostOutboxEvent> findProcessable(LocalDateTime now, int batchSize);
}
