package com.omegafrog.My.piano.app.web.service.outbox;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.omegafrog.My.piano.app.external.elasticsearch.ElasticSearchInstance;
import com.omegafrog.My.piano.app.web.domain.outbox.PostIndexVersion;
import com.omegafrog.My.piano.app.web.domain.outbox.PostIndexVersionRepository;
import com.omegafrog.My.piano.app.web.domain.outbox.PostOutboxEvent;
import com.omegafrog.My.piano.app.web.domain.outbox.PostOutboxEventRepository;
import com.omegafrog.My.piano.app.web.domain.outbox.PostOutboxEventType;
import com.omegafrog.My.piano.app.web.domain.outbox.ProcessedPostEvent;
import com.omegafrog.My.piano.app.web.domain.outbox.ProcessedPostEventRepository;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PostOutboxProcessor {

    private final PostOutboxEventRepository postOutboxEventRepository;
    private final ProcessedPostEventRepository processedPostEventRepository;
    private final PostIndexVersionRepository postIndexVersionRepository;
    private final PostRepository postRepository;
    private final ElasticSearchInstance elasticSearchInstance;
    private final Clock clock;
    private final int batchSize;
    private final Duration retryDelay;

    @Autowired
    public PostOutboxProcessor(
            PostOutboxEventRepository postOutboxEventRepository,
            ProcessedPostEventRepository processedPostEventRepository,
            PostIndexVersionRepository postIndexVersionRepository,
            PostRepository postRepository,
            ElasticSearchInstance elasticSearchInstance,
            @Value("${post-outbox.batch-size:100}") int batchSize,
            @Value("${post-outbox.retry-delay-seconds:60}") long retryDelaySeconds) {
        this(
                postOutboxEventRepository,
                processedPostEventRepository,
                postIndexVersionRepository,
                postRepository,
                elasticSearchInstance,
                Clock.systemUTC(),
                batchSize,
                Duration.ofSeconds(retryDelaySeconds));
    }

    PostOutboxProcessor(
            PostOutboxEventRepository postOutboxEventRepository,
            ProcessedPostEventRepository processedPostEventRepository,
            PostIndexVersionRepository postIndexVersionRepository,
            PostRepository postRepository,
            ElasticSearchInstance elasticSearchInstance,
            Clock clock,
            int batchSize,
            Duration retryDelay) {
        this.postOutboxEventRepository = postOutboxEventRepository;
        this.processedPostEventRepository = processedPostEventRepository;
        this.postIndexVersionRepository = postIndexVersionRepository;
        this.postRepository = postRepository;
        this.elasticSearchInstance = elasticSearchInstance;
        this.clock = clock;
        this.batchSize = batchSize;
        this.retryDelay = retryDelay;
    }

    @Scheduled(fixedDelayString = "${post-outbox.poll-delay-ms:1000}")
    @Transactional
    public void processPendingEvents() {
        LocalDateTime now = now();
        List<PostOutboxEvent> events = postOutboxEventRepository.findProcessable(now, batchSize);
        for (PostOutboxEvent event : events) {
            processSingleEvent(event);
        }
    }

    private void processSingleEvent(PostOutboxEvent event) {
        LocalDateTime now = now();
        String eventId = event.getEventId();

        if (processedPostEventRepository.existsByEventId(eventId)) {
            event.markCompleted(now);
            return;
        }

        PostIndexVersion versionState = postIndexVersionRepository.findByPostId(event.getPostId())
                .orElse(null);

        if (versionState != null && versionState.isStale(event.getEventVersion())) {
            processedPostEventRepository.save(
                    ProcessedPostEvent.of(eventId, event.getPostId(), event.getEventVersion(), now));
            event.markCompleted(now);
            return;
        }

        try {
            applyToElastic(event);

            processedPostEventRepository.save(
                    ProcessedPostEvent.of(eventId, event.getPostId(), event.getEventVersion(), now));

            PostIndexVersion upsertTarget = versionState == null
                    ? PostIndexVersion.initialize(event.getPostId())
                    : versionState;
            upsertTarget.apply(event.getEventVersion(), eventId, now);
            postIndexVersionRepository.save(upsertTarget);
            event.markCompleted(now);
        } catch (Exception e) {
            log.error("Failed to process post outbox event. eventId={}, postId={}", eventId, event.getPostId(), e);
            event.markFailed(e.getMessage(), now.plus(retryDelay));
        }
    }

    private void applyToElastic(PostOutboxEvent event) throws Exception {
        PostOutboxEventType eventType = event.getEventType();

        if (eventType == PostOutboxEventType.POST_DELETED) {
            elasticSearchInstance.deletePostIndex(event.getPostId());
            return;
        }

        Post post = postRepository.findById(event.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find post. id=" + event.getPostId()));

        if (eventType == PostOutboxEventType.POST_CREATED) {
            elasticSearchInstance.savePostIndex(post);
            return;
        }

        elasticSearchInstance.updatePostIndex(post);
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }
}
