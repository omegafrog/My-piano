package com.omegafrog.My.piano.app.web.domain.outbox;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "upload_outbox_event", indexes = {
        @Index(name = "idx_upload_outbox_status_next_attempt", columnList = "status,nextAttemptAt")
})
@Getter
@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
public class UploadOutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventId;

    @Column(nullable = false)
    private String uploadId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UploadOutboxEventType eventType;

    private String sheetUrl;

    @Lob
    private String thumbnailUrl;

    private String originalFileName;

    @Column(nullable = false)
    private int pageNum;

    @Column(length = 500)
    private String errorMessage;

    private String failureReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UploadOutboxEventStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(length = 500)
    private String lastError;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private UploadOutboxEvent(
            String eventId,
            String uploadId,
            UploadOutboxEventType eventType,
            String sheetUrl,
            String thumbnailUrl,
            String originalFileName,
            int pageNum,
            String errorMessage,
            String failureReason) {
        this.eventId = eventId;
        this.uploadId = uploadId;
        this.eventType = eventType;
        this.sheetUrl = sheetUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.originalFileName = originalFileName;
        this.pageNum = pageNum;
        this.errorMessage = errorMessage;
        this.failureReason = failureReason;
        this.status = UploadOutboxEventStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = LocalDateTime.now();
        this.nextAttemptAt = this.createdAt;
    }

    public static UploadOutboxEvent completed(
            String eventId,
            String uploadId,
            String sheetUrl,
            String thumbnailUrl,
            String originalFileName,
            int pageNum) {
        return new UploadOutboxEvent(
                eventId,
                uploadId,
                UploadOutboxEventType.FILE_UPLOAD_COMPLETED,
                sheetUrl,
                thumbnailUrl,
                originalFileName,
                pageNum,
                null,
                null);
    }

    public static UploadOutboxEvent failed(
            String eventId,
            String uploadId,
            String originalFileName,
            String errorMessage,
            String failureReason) {
        return new UploadOutboxEvent(
                eventId,
                uploadId,
                UploadOutboxEventType.FILE_UPLOAD_FAILED,
                null,
                null,
                originalFileName,
                0,
                errorMessage,
                failureReason);
    }

    public void markCompleted(LocalDateTime now) {
        this.status = UploadOutboxEventStatus.COMPLETED;
        this.processedAt = now;
        this.lastError = null;
    }

    public void markFailed(String errorMessage, LocalDateTime nextAttemptAt) {
        this.status = UploadOutboxEventStatus.FAILED;
        this.retryCount += 1;
        this.lastError = errorMessage;
        this.nextAttemptAt = nextAttemptAt;
    }
}
