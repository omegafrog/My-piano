package com.omegafrog.My.piano.app.web.event;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SheetPostCreatedAfterCommitEvent {

    private final String uploadId;
    private final Long sheetPostId;
    private final LocalDateTime createdAt;

    public SheetPostCreatedAfterCommitEvent(String uploadId, Long sheetPostId) {
        this.uploadId = uploadId;
        this.sheetPostId = sheetPostId;
        this.createdAt = LocalDateTime.now();
    }
}
