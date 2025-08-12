package com.omegafrog.My.piano.app.web.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CompensationEvent extends SagaEvent {
    private String originalEventType;
    private Long postId;
    private String compensationAction;
    private Object originalData;
    
    public CompensationEvent(String originalEventId, String originalEventType, Long postId, String compensationAction, Object originalData, String failureReason) {
        super(UUID.randomUUID().toString(), originalEventId, "COMPENSATION_REQUIRED", LocalDateTime.now(), failureReason);
        this.originalEventType = originalEventType;
        this.postId = postId;
        this.compensationAction = compensationAction;
        this.originalData = originalData;
    }
}