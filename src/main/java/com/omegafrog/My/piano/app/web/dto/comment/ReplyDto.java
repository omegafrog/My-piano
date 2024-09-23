package com.omegafrog.My.piano.app.web.dto.comment;

import com.omegafrog.My.piano.app.web.domain.Reply;

import java.time.LocalDateTime;

public record ReplyDto(String content, String name, LocalDateTime createdAt) {
    public ReplyDto(Reply reply){
        this(reply.getContent(), reply.getAuthorName(), reply.getCreatedAt());
    }
}
