package com.omegafrog.My.piano.app.web.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PostUpdatedEvent extends PostEvent {
    private String title;
    private String content;
    private String category;
    private Long authorId;
    private String authorUsername;
    
    public PostUpdatedEvent(Long postId, String title, String content, String category, Long authorId, String authorUsername) {
        super(UUID.randomUUID().toString(), postId, LocalDateTime.now(), "POST_UPDATED");
        this.title = title;
        this.content = content;
        this.category = category;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
    }
}