package com.omegafrog.My.piano.app.web.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadFailedEvent {
    private String eventId;
    private String eventType;
    private String uploadId;
    private String originalFileName;
    private String errorMessage;
    private String failureReason;
    private LocalDateTime timestamp;

    public static FileUploadFailedEvent create(String uploadId, String originalFileName, 
                                               String errorMessage, String failureReason) {
        return FileUploadFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("FILE_UPLOAD_FAILED")
                .uploadId(uploadId)
                .originalFileName(originalFileName)
                .errorMessage(errorMessage)
                .failureReason(failureReason)
                .timestamp(LocalDateTime.now())
                .build();
    }
}