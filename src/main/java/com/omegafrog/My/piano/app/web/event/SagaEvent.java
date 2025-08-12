package com.omegafrog.My.piano.app.web.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class SagaEvent {
    private String sagaId;
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String failureReason;
}