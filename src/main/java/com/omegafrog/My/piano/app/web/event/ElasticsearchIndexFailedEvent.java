package com.omegafrog.My.piano.app.web.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ElasticsearchIndexFailedEvent extends SagaEvent {
    private String originalEventType;
    private Long postId;
    private String compensationAction;
    
    public ElasticsearchIndexFailedEvent(String originalEventId, String originalEventType, Long postId, String failureReason, String compensationAction) {
        super(UUID.randomUUID().toString(), originalEventId, "ELASTICSEARCH_INDEX_FAILED", LocalDateTime.now(), failureReason);
        this.originalEventType = originalEventType;
        this.postId = postId;
        this.compensationAction = compensationAction;
    }
}