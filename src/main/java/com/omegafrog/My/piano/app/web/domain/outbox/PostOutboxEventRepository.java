package com.omegafrog.My.piano.app.web.domain.outbox;

import java.time.LocalDateTime;
import java.util.List;

public interface PostOutboxEventRepository {

    PostOutboxEvent save(PostOutboxEvent event);

    List<PostOutboxEvent> findProcessable(LocalDateTime now, int batchSize);
}
