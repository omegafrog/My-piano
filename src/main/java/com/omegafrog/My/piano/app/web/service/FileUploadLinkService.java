package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcess;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcessRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.infra.fileUpload.FileUploadRedisReadModelWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadLinkService {

    private static final int DEFAULT_RETRY_DELAY_SECONDS = 5;
    private static final int DEFAULT_LINK_LEASE_SECONDS = 300;

    private final FileUploadProcessRepository fileUploadProcessRepository;
    private final SheetPostRepository sheetPostRepository;
    private final FileUploadRedisReadModelWriter readModelWriter;

    @Transactional
    public void linkUploadToSheetPost(String uploadId, Long sheetPostId) {
        FileUploadProcess job = fileUploadProcessRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid uploadId: " + uploadId));

        job.assignSheetPostId(sheetPostId);

        // upload이 완료되어 있다면 link 작업을 바로 스케줄 가능하게 만든다.
        if (job.isUploadCompleted()) {
            job.markLinkPending(LocalDateTime.now());
        }

        fileUploadProcessRepository.save(job);
        afterCommit(() -> readModelWriter.upsert(job));

        // 업로드가 이미 완료된 상태라면 즉시 링크를 시도한다.
        if (job.isUploadCompleted()) {
            processLinkJob(job.getId(), DEFAULT_RETRY_DELAY_SECONDS);
        }
    }

    @Transactional
    public void processLinkJob(Long jobId, int retryDelaySeconds) {
        FileUploadProcess job = fileUploadProcessRepository.findById(jobId).orElse(null);
        if (job == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (!job.canLink(now)) {
            return;
        }

        job.markLinkRunning(now, DEFAULT_LINK_LEASE_SECONDS);
        fileUploadProcessRepository.save(job);
        afterCommit(() -> readModelWriter.upsert(job));

        try {
            if (job.getSheetUrl() == null || job.getSheetUrl().isEmpty()) {
                throw new IllegalStateException("sheetUrl not ready");
            }
            if (job.getThumbnailUrls() == null || job.getThumbnailUrls().isEmpty()) {
                throw new IllegalStateException("thumbnailUrls not ready");
            }
            if (job.getPageNum() == null || job.getPageNum() <= 0) {
                throw new IllegalStateException("pageNum not ready");
            }

            SheetPost sheetPost = sheetPostRepository.findById(job.getSheetPostId())
                    .orElseThrow(() -> new IllegalStateException("SheetPost not found: " + job.getSheetPostId()));

            sheetPost.getSheet().updateUrls(job.getSheetUrl(), job.getThumbnailUrls());
            sheetPost.getSheet().updatePageNum(job.getPageNum());
            sheetPost.getSheet().updateOriginalFileName(job.getOriginalFileName());

            sheetPostRepository.save(sheetPost);

            job.markLinked(now);
            fileUploadProcessRepository.save(job);
            afterCommit(() -> readModelWriter.upsert(job));
        } catch (Exception e) {
            boolean willRetry = job.markLinkRetryOrFailed(e.getMessage(), retryDelaySeconds, now);
            fileUploadProcessRepository.save(job);
            afterCommit(() -> readModelWriter.upsert(job));
            if (!willRetry) {
                log.error("Link job failed permanently. uploadId: {}, sheetPostId: {}", job.getUploadId(),
                        job.getSheetPostId(), e);
            }
        }
    }

    private static void afterCommit(Runnable runnable) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            runnable.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runnable.run();
            }
        });
    }
}
