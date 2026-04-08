package com.omegafrog.My.piano.app.web.dto.fileUpload;

import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcessStatus;

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
    private FileUploadProcessStatus status;
    private String message;
    private String originalFileName;
}
