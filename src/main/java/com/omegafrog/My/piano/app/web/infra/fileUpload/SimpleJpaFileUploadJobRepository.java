package com.omegafrog.My.piano.app.web.infra.fileUpload;

import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJob;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadLinkStatus;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJobStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SimpleJpaFileUploadJobRepository extends JpaRepository<FileUploadJob, Long> {

    Optional<FileUploadJob> findByUploadId(String uploadId);

    List<FileUploadJob> findByStatusInAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            List<FileUploadJobStatus> statuses,
            LocalDateTime now,
            Pageable pageable);

    List<FileUploadJob> findByStatusAndLinkStatusInAndLinkNextAttemptAtLessThanEqualAndSheetPostIdIsNotNullOrderByCreatedAtAsc(
            FileUploadJobStatus status,
            List<FileUploadLinkStatus> linkStatuses,
            LocalDateTime now,
            Pageable pageable);
}
