package com.omegafrog.My.piano.app.web.infra.fileUpload;

import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJob;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJobRepository;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadLinkStatus;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJobStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaFileUploadJobRepositoryImpl implements FileUploadJobRepository {

    private final SimpleJpaFileUploadJobRepository jpaRepository;

    @Override
    public FileUploadJob save(FileUploadJob job) {
        return jpaRepository.save(job);
    }

    @Override
    public Optional<FileUploadJob> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<FileUploadJob> findByUploadId(String uploadId) {
        return jpaRepository.findByUploadId(uploadId);
    }

    @Override
    public List<FileUploadJob> findProcessableJobs(LocalDateTime now, int batchSize) {
        return jpaRepository.findByStatusInAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                List.of(FileUploadJobStatus.PENDING, FileUploadJobStatus.RETRY),
                now,
                PageRequest.of(0, Math.max(1, batchSize)));
    }

    @Override
    public List<FileUploadJob> findLinkableJobs(LocalDateTime now, int batchSize) {
        return jpaRepository.findByStatusAndLinkStatusInAndLinkNextAttemptAtLessThanEqualAndSheetPostIdIsNotNullOrderByCreatedAtAsc(
                FileUploadJobStatus.COMPLETED,
                List.of(FileUploadLinkStatus.PENDING, FileUploadLinkStatus.RETRY),
                now,
                PageRequest.of(0, Math.max(1, batchSize)));
    }
}
