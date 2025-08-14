package com.omegafrog.My.piano.app.web.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishPostCreated(PostCreatedEvent event) {
        log.info("Publishing post created event: {}", event.getEventId());
        kafkaTemplate.send("post-created-topic", event.getPostId().toString(), event);
    }
    
    public void publishPostUpdated(PostUpdatedEvent event) {
        log.info("Publishing post updated event: {}", event.getEventId());
        kafkaTemplate.send("post-updated-topic", event.getPostId().toString(), event);
    }
    
    public void publishPostDeleted(PostDeletedEvent event) {
        log.info("Publishing post deleted event: {}", event.getEventId());
        kafkaTemplate.send("post-deleted-topic", event.getPostId().toString(), event);
    }
    
    public void publishElasticsearchIndexFailed(ElasticsearchIndexFailedEvent event) {
        log.warn("Publishing elasticsearch index failed event: {}", event.getSagaId());
        kafkaTemplate.send("elasticsearch-failed-topic", event.getPostId().toString(), event);
    }
    
    public void publishCompensationRequired(CompensationEvent event) {
        log.warn("Publishing compensation required event: {}", event.getSagaId());
        kafkaTemplate.send("compensation-topic", event.getPostId().toString(), event);
    }
    
    public void publishFileUploadCompleted(FileUploadCompletedEvent event) {
        log.info("Publishing file upload completed event: {}", event.getEventId());
        kafkaTemplate.send("file-upload-completed-topic", event.getUploadId(), event);
    }
    
    public void publishFileUploadFailed(FileUploadFailedEvent event) {
        log.warn("Publishing file upload failed event: {}", event.getEventId());
        kafkaTemplate.send("file-upload-failed-topic", event.getUploadId(), event);
    }
}