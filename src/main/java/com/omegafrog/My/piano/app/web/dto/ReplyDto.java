package com.omegafrog.My.piano.app.web.dto;

import java.time.LocalDateTime;

public record ReplyDto(String content, String name, LocalDateTime createdAt) {
}
