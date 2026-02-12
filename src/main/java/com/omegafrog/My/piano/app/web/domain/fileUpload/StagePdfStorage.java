package com.omegafrog.My.piano.app.web.domain.fileUpload;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StagePdfStorage {

    StagedPdf stage(MultipartFile file, String uploadId) throws IOException;

    boolean deleteIfExists(String stagePath);

    StageStorageUsage usage() throws IOException;
}
