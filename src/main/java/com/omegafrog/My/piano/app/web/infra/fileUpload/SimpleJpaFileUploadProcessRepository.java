package com.omegafrog.My.piano.app.web.infra.fileUpload;

import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcess;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadLinkStatus;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcessStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SimpleJpaFileUploadProcessRepository extends JpaRepository<FileUploadProcess, Long> {

    Optional<FileUploadProcess> findByUploadId(String uploadId);

    Optional<FileUploadProcess> findByUuidFileName(String uuidFileName);

    @Query("""
            select j
            from FileUploadProcess j
            where (
                (j.status in :statuses and j.nextAttemptAt <= :now)
                or (j.status = :runningStatus and j.runningLeaseUntil is not null and j.runningLeaseUntil <= :now)
            )
            order by j.createdAt asc
            """)
    List<FileUploadProcess> findByStatusInAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            List<FileUploadProcessStatus> statuses,
            FileUploadProcessStatus runningStatus,
            LocalDateTime now,
            Pageable pageable);

    @Query("""
            select j
            from FileUploadProcess j
            where j.status = :status
              and j.linkStatus in :linkStatuses
              and j.linkNextAttemptAt is not null
              and j.linkNextAttemptAt <= :now
              and j.sheetPostId is not null
            order by j.createdAt asc
            """)
    List<FileUploadProcess> findByStatusAndLinkStatusInAndLinkNextAttemptAtLessThanEqualAndSheetPostIdIsNotNullOrderByCreatedAtAsc(
            FileUploadProcessStatus status,
            List<FileUploadLinkStatus> linkStatuses,
            LocalDateTime now,
            Pageable pageable);
}
