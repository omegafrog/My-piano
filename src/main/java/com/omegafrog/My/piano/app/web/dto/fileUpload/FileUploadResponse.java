package com.omegafrog.My.piano.app.web.dto.fileUpload;

import com.omegafrog.My.piano.app.web.domain.outbox.UploadOutboxEventStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private String uploadId;
    private UploadOutboxEventStatus status;
    private String message;
    private String originalFileName;
}
