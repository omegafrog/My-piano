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

import com.omegafrog.My.piano.app.external.elasticsearch.ElasticSearchInstance;
import com.omegafrog.My.piano.app.web.domain.outbox.SheetPostOutboxEvent;
import com.omegafrog.My.piano.app.web.domain.outbox.SheetPostOutboxEventRepository;
import com.omegafrog.My.piano.app.web.domain.outbox.SheetPostOutboxEventType;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SheetPostOutboxProcessor {

    private final SheetPostOutboxEventRepository sheetPostOutboxEventRepository;
    private final SheetPostRepository sheetPostRepository;
    private final ElasticSearchInstance elasticSearchInstance;
    private final Clock clock;
    private final int batchSize;
    private final Duration retryDelay;

    @Autowired
    public SheetPostOutboxProcessor(
            SheetPostOutboxEventRepository sheetPostOutboxEventRepository,
            SheetPostRepository sheetPostRepository,
            ElasticSearchInstance elasticSearchInstance,
            @Value("${sheet-post-outbox.batch-size:100}") int batchSize,
            @Value("${sheet-post-outbox.retry-delay-seconds:60}") long retryDelaySeconds) {
        this(
                sheetPostOutboxEventRepository,
                sheetPostRepository,
                elasticSearchInstance,
                Clock.systemUTC(),
                batchSize,
                Duration.ofSeconds(retryDelaySeconds));
    }

    SheetPostOutboxProcessor(
            SheetPostOutboxEventRepository sheetPostOutboxEventRepository,
            SheetPostRepository sheetPostRepository,
            ElasticSearchInstance elasticSearchInstance,
            Clock clock,
            int batchSize,
            Duration retryDelay) {
        this.sheetPostOutboxEventRepository = sheetPostOutboxEventRepository;
        this.sheetPostRepository = sheetPostRepository;
        this.elasticSearchInstance = elasticSearchInstance;
        this.clock = clock;
        this.batchSize = batchSize;
        this.retryDelay = retryDelay;
    }

    @Scheduled(fixedDelayString = "${sheet-post-outbox.poll-delay-ms:1000}")
    @Transactional
    public void processPendingEvents() {
        LocalDateTime now = now();
        List<SheetPostOutboxEvent> events = sheetPostOutboxEventRepository.findProcessable(now, batchSize);
        for (SheetPostOutboxEvent event : events) {
            processSingleEvent(event);
        }
    }

    private void processSingleEvent(SheetPostOutboxEvent event) {
        LocalDateTime now = now();

        try {
            if (event.getEventType() == SheetPostOutboxEventType.SHEET_POST_CREATED) {
                SheetPost sheetPost = sheetPostRepository.findById(event.getSheetPostId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Cannot find sheet post. id=" + event.getSheetPostId()));
                elasticSearchInstance.invertIndexingSheetPost(sheetPost);
            }
            event.markCompleted(now);
        } catch (Exception e) {
            log.error(
                    "Failed to process sheet post outbox event. eventId={}, sheetPostId={}",
                    event.getEventId(),
                    event.getSheetPostId(),
                    e);
            event.markFailed(e.getMessage(), now.plus(retryDelay));
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }
}
