package com.omegafrog.My.piano.app.web.domain.upload;

import java.time.LocalDateTime;

import com.omegafrog.My.piano.app.web.enums.FileUploadStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "upload_job")
@Getter
@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
public class UploadJob {

    @Id
    private String uploadId;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String uuidFileName;

    private Long sheetPostId;

    private String sheetUrl;

    @Lob
    private String thumbnailUrl;

    @Column(nullable = false)
    private int pageNum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileUploadStatus status;

    @Column(length = 500)
    private String errorMessage;

    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    private UploadJob(String uploadId, String originalFileName, String uuidFileName, LocalDateTime now) {
        this.uploadId = uploadId;
        this.originalFileName = originalFileName;
        this.uuidFileName = uuidFileName;
        this.pageNum = 0;
        this.status = FileUploadStatus.UPLOADING;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static UploadJob create(String uploadId, String originalFileName, String uuidFileName, LocalDateTime now) {
        return new UploadJob(uploadId, originalFileName, uuidFileName, now);
    }

    public void assignSheetPostId(Long sheetPostId, LocalDateTime now) {
        this.sheetPostId = sheetPostId;
        this.updatedAt = now;
    }

    public void mergeCompletedData(
            String sheetUrl,
            String thumbnailUrl,
            Integer pageNum,
            String originalFileName,
            LocalDateTime now) {
        if (sheetUrl != null && !sheetUrl.isBlank()) {
            this.sheetUrl = sheetUrl;
        }
        if (thumbnailUrl != null && !thumbnailUrl.isBlank()) {
            this.thumbnailUrl = thumbnailUrl;
        }
        if (pageNum != null && pageNum > 0) {
            this.pageNum = pageNum;
        }
        if ((this.originalFileName == null || this.originalFileName.isBlank())
                && originalFileName != null
                && !originalFileName.isBlank()) {
            this.originalFileName = originalFileName;
        }
        this.status = FileUploadStatus.COMPLETED;
        this.completedAt = now;
        this.updatedAt = now;
        this.errorMessage = null;
        this.failureReason = null;
    }

    public void markFailed(String errorMessage, String failureReason, LocalDateTime now) {
        this.status = FileUploadStatus.FAILED;
        this.errorMessage = errorMessage;
        this.failureReason = failureReason;
        this.updatedAt = now;
    }
}
