package com.omegafrog.My.piano.app.web.domain.outbox;

public enum UploadOutboxEventStatus {
    PENDING,
    UPLOADING,
    COMPLETED,
    LINKED,
    FAILED
}
