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
public class FileUploadCompletedEvent {
    private String eventId;
    private String eventType;
    private String uploadId;
    private String sheetUrl;
    private String thumbnailUrl;
    private String originalFileName;
    private int pageNum;
    private LocalDateTime timestamp;

    public static FileUploadCompletedEvent create(String uploadId, String sheetUrl, String thumbnailUrl, 
                                                  String originalFileName, int pageNum) {
        return FileUploadCompletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("FILE_UPLOAD_COMPLETED")
                .uploadId(uploadId)
                .sheetUrl(sheetUrl)
                .thumbnailUrl(thumbnailUrl)
                .originalFileName(originalFileName)
                .pageNum(pageNum)
                .timestamp(LocalDateTime.now())
                .build();
    }
}