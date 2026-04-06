package com.omegafrog.My.piano.app.web.domain.fileUpload;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FileUploadProcessRepository {

    FileUploadProcess save(FileUploadProcess job);

    Optional<FileUploadProcess> findById(Long id);

    Optional<FileUploadProcess> findByUploadId(String uploadId);

    Optional<FileUploadProcess> findByUuidFileName(String uuidFileName);

    List<FileUploadProcess> findProcessableJobs(LocalDateTime now, int batchSize);

    List<FileUploadProcess> findLinkableJobs(LocalDateTime now, int batchSize);
}
