package com.omegafrog.My.piano.app.web.domain.fileUpload;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FileUploadJobRepository {

    FileUploadJob save(FileUploadJob job);

    Optional<FileUploadJob> findById(Long id);

    List<FileUploadJob> findProcessableJobs(LocalDateTime now, int batchSize);
}
