package com.omegafrog.My.piano.app.web.domain.outbox;

public interface ProcessedPostEventRepository {

    boolean existsByEventId(String eventId);

    ProcessedPostEvent save(ProcessedPostEvent processedPostEvent);
}
