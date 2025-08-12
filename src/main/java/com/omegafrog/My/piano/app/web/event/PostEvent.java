package com.omegafrog.My.piano.app.web.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class PostEvent {
    private String eventId;
    private Long postId;
    private LocalDateTime timestamp;
    private String eventType;
}