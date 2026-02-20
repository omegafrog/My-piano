package com.omegafrog.My.piano.app.web.service.outbox;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.omegafrog.My.piano.app.web.domain.outbox.UploadOutboxEvent;
import com.omegafrog.My.piano.app.web.domain.outbox.UploadOutboxEventRepository;
import com.omegafrog.My.piano.app.web.domain.outbox.UploadOutboxEventType;
import com.omegafrog.My.piano.app.web.service.FileUploadService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UploadOutboxProcessor {

    private final UploadOutboxEventRepository uploadOutboxEventRepository;
    private final FileUploadService fileUploadService;
    private final Clock clock;
    private final int batchSize;
    private final Duration retryDelay;

    @Autowired
    public UploadOutboxProcessor(
            UploadOutboxEventRepository uploadOutboxEventRepository,
            FileUploadService fileUploadService,
            @Value("${upload-outbox.batch-size:100}") int batchSize,
            @Value("${upload-outbox.retry-delay-seconds:60}") long retryDelaySeconds) {
        this(
                uploadOutboxEventRepository,
                fileUploadService,
                Clock.systemUTC(),
                batchSize,
                Duration.ofSeconds(retryDelaySeconds));
    }

    UploadOutboxProcessor(
            UploadOutboxEventRepository uploadOutboxEventRepository,
            FileUploadService fileUploadService,
            Clock clock,
            int batchSize,
            Duration retryDelay) {
        this.uploadOutboxEventRepository = uploadOutboxEventRepository;
        this.fileUploadService = fileUploadService;
        this.clock = clock;
        this.batchSize = batchSize;
        this.retryDelay = retryDelay;
    }

    @Scheduled(fixedDelayString = "${upload-outbox.poll-delay-ms:1000}")
    @Transactional
    public void processPendingEvents() {
        LocalDateTime now = now();
        List<UploadOutboxEvent> events = uploadOutboxEventRepository.findProcessable(now, batchSize);
        for (UploadOutboxEvent event : events) {
            processSingleEvent(event);
        }
    }

    private void processSingleEvent(UploadOutboxEvent event) {
        LocalDateTime now = now();

        try {
            if (event.getEventType() == UploadOutboxEventType.FILE_UPLOAD_COMPLETED) {
                fileUploadService.applyUploadCompleted(
                        event.getUploadId(),
                        event.getSheetUrl(),
                        event.getThumbnailUrl(),
                        event.getOriginalFileName(),
                        event.getPageNum());
            } else {
                fileUploadService.applyUploadFailed(
                        event.getUploadId(),
                        event.getOriginalFileName(),
                        event.getErrorMessage(),
                        event.getFailureReason());
            }

            event.markCompleted(now);
        } catch (Exception e) {
            log.error("Failed to process upload outbox event. eventId={}, uploadId={}", event.getEventId(), event.getUploadId(), e);
            event.markFailed(e.getMessage(), now.plus(retryDelay));
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }
}
