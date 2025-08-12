package com.omegafrog.My.piano.app.web.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PostDeletedEvent extends PostEvent {
    
    public PostDeletedEvent(Long postId) {
        super(UUID.randomUUID().toString(), postId, LocalDateTime.now(), "POST_DELETED");
    }
}