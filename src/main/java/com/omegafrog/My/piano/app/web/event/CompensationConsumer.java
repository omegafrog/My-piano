package com.omegafrog.My.piano.app.web.event;

import com.omegafrog.My.piano.app.external.elasticsearch.ElasticSearchInstance;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CompensationConsumer {

    private final PostRepository postRepository;
    private final ElasticSearchInstance elasticSearchInstance;
    private final EventPublisher eventPublisher;

    @KafkaListener(topics = "elasticsearch-failed-topic", groupId = "mypiano-compensation-group")
    public void handleElasticsearchIndexFailed(ElasticsearchIndexFailedEvent event) {
        log.warn("Handling elasticsearch index failure: {} for post: {}", event.getSagaId(), event.getPostId());
        
        switch (event.getCompensationAction()) {
            case "ROLLBACK_POST_CREATION":
                rollbackPostCreation(event);
                break;
            case "RETRY_POST_UPDATE":
                retryPostUpdate(event);
                break;
            case "LOG_FAILED_DELETE":
                logFailedDelete(event);
                break;
            default:
                log.warn("Unknown compensation action: {}", event.getCompensationAction());
        }
    }

    @KafkaListener(topics = "compensation-topic", groupId = "mypiano-compensation-group")
    public void handleCompensationRequired(CompensationEvent event) {
        log.warn("Handling compensation event: {} for post: {}", event.getSagaId(), event.getPostId());
        
        switch (event.getCompensationAction()) {
            case "ROLLBACK_POST_CREATION":
                rollbackPostCreationFromCompensation(event);
                break;
            case "RESTORE_POST_STATE":
                restorePostState(event);
                break;
            default:
                log.warn("Unknown compensation action: {}", event.getCompensationAction());
        }
    }

    private void rollbackPostCreation(ElasticsearchIndexFailedEvent event) {
        try {
            log.info("Rolling back post creation for post: {}", event.getPostId());
            
            // Delete the post from database as compensation
            postRepository.deleteById(event.getPostId());
            
            log.info("Successfully rolled back post creation for post: {}", event.getPostId());
        } catch (Exception e) {
            log.error("Failed to rollback post creation for post: {}", event.getPostId(), e);
            
            // Create further compensation event if rollback fails
            CompensationEvent compensationEvent = new CompensationEvent(
                event.getEventId(),
                event.getOriginalEventType(),
                event.getPostId(),
                "MANUAL_CLEANUP_REQUIRED",
                null,
                "Automatic rollback failed: " + e.getMessage()
            );
            eventPublisher.publishCompensationRequired(compensationEvent);
        }
    }

    private void retryPostUpdate(ElasticsearchIndexFailedEvent event) {
        try {
            log.info("Retrying elasticsearch update for post: {}", event.getPostId());
            
            Post post = postRepository.findById(event.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found: " + event.getPostId()));
            
            // Retry elasticsearch update
            elasticSearchInstance.updatePostIndex(post);
            
            log.info("Successfully retried elasticsearch update for post: {}", event.getPostId());
        } catch (Exception e) {
            log.error("Retry failed for post: {}, scheduling manual intervention", event.getPostId(), e);
            
            CompensationEvent compensationEvent = new CompensationEvent(
                event.getEventId(),
                event.getOriginalEventType(),
                event.getPostId(),
                "MANUAL_SYNC_REQUIRED",
                null,
                "Retry failed: " + e.getMessage()
            );
            eventPublisher.publishCompensationRequired(compensationEvent);
        }
    }

    private void logFailedDelete(ElasticsearchIndexFailedEvent event) {
        // For delete operations, we just log the failure
        // This is less critical as the post is already deleted from the main database
        log.warn("Elasticsearch delete failed for post: {}, reason: {}", 
                event.getPostId(), event.getFailureReason());
        
        // Could implement cleanup job to periodically clean up orphaned elasticsearch indices
        log.info("Scheduling cleanup job for orphaned elasticsearch index: {}", event.getPostId());
    }

    private void rollbackPostCreationFromCompensation(CompensationEvent event) {
        try {
            log.info("Rolling back post creation from compensation event for post: {}", event.getPostId());
            
            postRepository.deleteById(event.getPostId());
            
            log.info("Successfully completed compensation rollback for post: {}", event.getPostId());
        } catch (Exception e) {
            log.error("Compensation rollback failed for post: {}, manual intervention required", 
                     event.getPostId(), e);
        }
    }

    private void restorePostState(CompensationEvent event) {
        try {
            log.info("Restoring post state from compensation event for post: {}", event.getPostId());
            
            // This would involve restoring post to previous state
            // Implementation depends on what original data is stored in the event
            if (event.getOriginalData() != null) {
                // Restore logic here
                log.info("Post state restoration logic would be implemented here");
            }
            
            log.info("Post state restoration completed for post: {}", event.getPostId());
        } catch (Exception e) {
            log.error("Post state restoration failed for post: {}", event.getPostId(), e);
        }
    }
}