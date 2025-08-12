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
public class PostEventConsumer {

    private final ElasticSearchInstance elasticSearchInstance;
    private final PostRepository postRepository;
    private final EventPublisher eventPublisher;

    @KafkaListener(topics = "post-created-topic", groupId = "mypiano-consumer-group")
    public void handlePostCreated(PostCreatedEvent event) {
        log.info("Received post created event: {}", event.getEventId());
        
        try {
            // Find the post from database
            Post post = postRepository.findById(event.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found: " + event.getPostId()));
            
            // Create elasticsearch index
            elasticSearchInstance.savePostIndex(post);
            log.info("Successfully created elasticsearch index for post: {}", event.getPostId());
            
        } catch (Exception e) {
            log.error("Failed to create elasticsearch index for post: {}", event.getPostId(), e);
            
            // Publish compensation event
            ElasticsearchIndexFailedEvent failedEvent = new ElasticsearchIndexFailedEvent(
                event.getEventId(),
                event.getEventType(),
                event.getPostId(),
                e.getMessage(),
                "ROLLBACK_POST_CREATION"
            );
            eventPublisher.publishElasticsearchIndexFailed(failedEvent);
        }
    }

    @KafkaListener(topics = "post-updated-topic", groupId = "mypiano-consumer-group")
    public void handlePostUpdated(PostUpdatedEvent event) {
        log.info("Received post updated event: {}", event.getEventId());
        
        try {
            // Find the post from database
            Post post = postRepository.findById(event.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found: " + event.getPostId()));
            
            // Update elasticsearch index
            elasticSearchInstance.updatePostIndex(post);
            log.info("Successfully updated elasticsearch index for post: {}", event.getPostId());
            
        } catch (Exception e) {
            log.error("Failed to update elasticsearch index for post: {}", event.getPostId(), e);
            
            // Publish compensation event
            ElasticsearchIndexFailedEvent failedEvent = new ElasticsearchIndexFailedEvent(
                event.getEventId(),
                event.getEventType(),
                event.getPostId(),
                e.getMessage(),
                "RETRY_POST_UPDATE"
            );
            eventPublisher.publishElasticsearchIndexFailed(failedEvent);
        }
    }

    @KafkaListener(topics = "post-deleted-topic", groupId = "mypiano-consumer-group")
    public void handlePostDeleted(PostDeletedEvent event) {
        log.info("Received post deleted event: {}", event.getEventId());
        
        try {
            // Delete elasticsearch index
            elasticSearchInstance.deletePostIndex(event.getPostId());
            log.info("Successfully deleted elasticsearch index for post: {}", event.getPostId());
            
        } catch (Exception e) {
            log.error("Failed to delete elasticsearch index for post: {}", event.getPostId(), e);
            
            // Publish compensation event (less critical for delete operations)
            ElasticsearchIndexFailedEvent failedEvent = new ElasticsearchIndexFailedEvent(
                event.getEventId(),
                event.getEventType(),
                event.getPostId(),
                e.getMessage(),
                "LOG_FAILED_DELETE"
            );
            eventPublisher.publishElasticsearchIndexFailed(failedEvent);
        }
    }
}