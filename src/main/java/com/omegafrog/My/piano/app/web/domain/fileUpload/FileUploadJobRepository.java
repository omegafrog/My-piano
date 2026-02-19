package com.omegafrog.My.piano.app.web.domain.fileUpload;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FileUploadJobRepository {

    FileUploadJob save(FileUploadJob job);

    Optional<FileUploadJob> findById(Long id);

    Optional<FileUploadJob> findByUploadId(String uploadId);

    List<FileUploadJob> findProcessableJobs(LocalDateTime now, int batchSize);

    List<FileUploadJob> findLinkableJobs(LocalDateTime now, int batchSize);
}
