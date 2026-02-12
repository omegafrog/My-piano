package com.omegafrog.My.piano.app.web.domain.fileUpload;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
public class FileUploadJob {

    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String uploadId;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String uuidFileName;

    @Column(name = "temp_file_path", nullable = false)
    private String stagedFilePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileUploadJobStatus status;

    @Column(nullable = false)
    private int attemptCount;

    @Column(nullable = false)
    private int maxAttempts;

    @Column(nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(length = 1000)
    private String lastError;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    @Builder
    public FileUploadJob(String uploadId, String originalFileName, String uuidFileName, String stagedFilePath,
            FileUploadJobStatus status, int maxAttempts, LocalDateTime nextAttemptAt) {
        this.uploadId = uploadId;
        this.originalFileName = originalFileName;
        this.uuidFileName = uuidFileName;
        this.stagedFilePath = stagedFilePath;
        this.status = status;
        this.maxAttempts = maxAttempts <= 0 ? DEFAULT_MAX_ATTEMPTS : maxAttempts;
        this.nextAttemptAt = nextAttemptAt == null ? LocalDateTime.now() : nextAttemptAt;
        this.attemptCount = 0;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canStart(LocalDateTime now) {
        if (status != FileUploadJobStatus.PENDING && status != FileUploadJobStatus.RETRY) {
            return false;
        }
        return !nextAttemptAt.isAfter(now);
    }

    public void markRunning() {
        this.status = FileUploadJobStatus.RUNNING;
        this.lastError = null;
        this.attemptCount++;
    }

    public void markCompleted(LocalDateTime now) {
        this.status = FileUploadJobStatus.COMPLETED;
        this.completedAt = now;
        this.lastError = null;
    }

    public boolean markRetryOrFailed(String errorMessage, int retryDelaySeconds, LocalDateTime now) {
        this.lastError = errorMessage;

        if (attemptCount >= maxAttempts) {
            this.status = FileUploadJobStatus.FAILED;
            this.completedAt = now;
            return false;
        }

        this.status = FileUploadJobStatus.RETRY;
        this.nextAttemptAt = now.plusSeconds(Math.max(1, retryDelaySeconds));
        return true;
    }
}
