package com.omegafrog.My.piano.app.web.event;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadStartedEvent {
    private String eventId;
    private String eventType;
    private String uploadId;
    private String originalFileName;
    private LocalDateTime timestamp;

    public static FileUploadStartedEvent create(String uploadId, String originalFileName) {
        return FileUploadStartedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("FILE_UPLOAD_STARTED")
                .uploadId(uploadId)
                .originalFileName(originalFileName)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
