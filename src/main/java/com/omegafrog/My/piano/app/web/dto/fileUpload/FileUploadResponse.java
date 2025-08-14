package com.omegafrog.My.piano.app.web.dto.fileUpload;

import com.omegafrog.My.piano.app.web.enums.FileUploadStatus;
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
    private FileUploadStatus status;
    private String message;
    private String originalFileName;
}