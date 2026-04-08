package com.omegafrog.My.piano.app.web.infra.fileUpload;

import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcess;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcessRepository;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadLinkStatus;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaFileUploadProcessRepositoryImpl implements FileUploadProcessRepository {

    private final SimpleJpaFileUploadProcessRepository jpaRepository;

    @Override
    public FileUploadProcess save(FileUploadProcess job) {
        return jpaRepository.save(job);
    }

    @Override
    public Optional<FileUploadProcess> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<FileUploadProcess> findByUploadId(String uploadId) {
        return jpaRepository.findByUploadId(uploadId);
    }

    @Override
    public Optional<FileUploadProcess> findByUuidFileName(String uuidFileName) {
        return jpaRepository.findByUuidFileName(uuidFileName);
    }

    @Override
    public List<FileUploadProcess> findProcessableJobs(LocalDateTime now, int batchSize) {
        return jpaRepository.findByStatusInAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                List.of(FileUploadProcessStatus.PENDING, FileUploadProcessStatus.RETRY),
                FileUploadProcessStatus.RUNNING,
                now,
                PageRequest.of(0, Math.max(1, batchSize)));
    }

    @Override
    public List<FileUploadProcess> findLinkableJobs(LocalDateTime now, int batchSize) {
        return jpaRepository.findByStatusAndLinkStatusInAndLinkNextAttemptAtLessThanEqualAndSheetPostIdIsNotNullOrderByCreatedAtAsc(
                FileUploadProcessStatus.COMPLETED,
                List.of(FileUploadLinkStatus.PENDING, FileUploadLinkStatus.RETRY, FileUploadLinkStatus.RUNNING),
                now,
                PageRequest.of(0, Math.max(1, batchSize)));
    }
}
