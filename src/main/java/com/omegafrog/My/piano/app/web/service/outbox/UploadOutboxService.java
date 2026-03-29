package com.omegafrog.My.piano.app.web.service.outbox;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.omegafrog.My.piano.app.web.domain.outbox.UploadOutboxEvent;
import com.omegafrog.My.piano.app.web.domain.outbox.UploadOutboxEventRepository;
import com.omegafrog.My.piano.app.web.event.FileUploadCompletedEvent;
import com.omegafrog.My.piano.app.web.event.FileUploadFailedEvent;
import com.omegafrog.My.piano.app.web.event.FileUploadStartedEvent;
import com.omegafrog.My.piano.app.web.domain.outbox.UploadOutboxEventStatus;
import com.omegafrog.My.piano.app.web.service.FileUploadStatusStore;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadOutboxService {

    private final UploadOutboxEventRepository uploadOutboxEventRepository;
    private final FileUploadStatusStore statusStore;

    @Transactional
    public void enqueueStarted(FileUploadStartedEvent event) {
        Map<String, String> updates = new HashMap<>();
        updates.put("status", UploadOutboxEventStatus.UPLOADING.name());
        if (event.getOriginalFileName() != null && !event.getOriginalFileName().isBlank()) {
            updates.put("originalFileName", event.getOriginalFileName());
        }
        statusStore.putAll(event.getUploadId(), updates, Duration.ofHours(1));

        UploadOutboxEvent outboxEvent = UploadOutboxEvent.started(
                event.getEventId(),
                event.getUploadId(),
                event.getOriginalFileName());
        uploadOutboxEventRepository.save(outboxEvent);
    }

    @Transactional
    public void enqueueCompleted(FileUploadCompletedEvent event) {
        Map<String, String> updates = new HashMap<>();
        if (event.getSheetUrl() != null && !event.getSheetUrl().isBlank()) {
            updates.put("sheetUrl", event.getSheetUrl());
        }
        if (event.getThumbnailUrl() != null && !event.getThumbnailUrl().isBlank()) {
            updates.put("thumbnailUrl", event.getThumbnailUrl());
        }
        if (event.getPageNum() > 0) {
            updates.put("pageNum", String.valueOf(event.getPageNum()));
        }
        updates.put("status", UploadOutboxEventStatus.COMPLETED.name());
        updates.put("completedAt", LocalDateTime.now().toString());
        statusStore.putAll(event.getUploadId(), updates, Duration.ofHours(1));

        UploadOutboxEvent outboxEvent = UploadOutboxEvent.completed(
                event.getEventId(),
                event.getUploadId(),
                event.getSheetUrl(),
                event.getThumbnailUrl(),
                event.getOriginalFileName(),
                event.getPageNum());
        uploadOutboxEventRepository.save(outboxEvent);
    }

    @Transactional
    public void enqueueFailed(FileUploadFailedEvent event) {
        statusStore.put(event.getUploadId(), "status", UploadOutboxEventStatus.FAILED.name());

        UploadOutboxEvent outboxEvent = UploadOutboxEvent.failed(
                event.getEventId(),
                event.getUploadId(),
                event.getOriginalFileName(),
                event.getErrorMessage(),
                event.getFailureReason());
        uploadOutboxEventRepository.save(outboxEvent);
    }
}
