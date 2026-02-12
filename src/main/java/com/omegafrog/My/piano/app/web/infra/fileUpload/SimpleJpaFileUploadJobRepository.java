package com.omegafrog.My.piano.app.web.infra.fileUpload;

import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJob;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJobStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SimpleJpaFileUploadJobRepository extends JpaRepository<FileUploadJob, Long> {

    List<FileUploadJob> findByStatusInAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            List<FileUploadJobStatus> statuses,
            LocalDateTime now,
            Pageable pageable);
}
