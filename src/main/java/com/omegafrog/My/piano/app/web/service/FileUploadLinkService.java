package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJob;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJobRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.infra.fileUpload.FileUploadRedisReadModelWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadLinkService {

    private static final int DEFAULT_RETRY_DELAY_SECONDS = 5;

    private final FileUploadJobRepository fileUploadJobRepository;
    private final SheetPostRepository sheetPostRepository;
    private final FileUploadRedisReadModelWriter readModelWriter;

    @Transactional
    public void linkUploadToSheetPost(String uploadId, Long sheetPostId) {
        FileUploadJob job = fileUploadJobRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid uploadId: " + uploadId));

        job.assignSheetPostId(sheetPostId);

        // upload이 완료되어 있다면 link 작업을 바로 스케줄 가능하게 만든다.
        if (job.isUploadCompleted()) {
            job.markLinkPending(LocalDateTime.now());
        }

        fileUploadJobRepository.save(job);
        readModelWriter.upsert(job);

        // 업로드가 이미 완료된 상태라면 즉시 링크를 시도한다.
        if (job.isUploadCompleted()) {
            processLinkJob(job.getId(), DEFAULT_RETRY_DELAY_SECONDS);
        }
    }

    @Transactional
    public void processLinkJob(Long jobId, int retryDelaySeconds) {
        FileUploadJob job = fileUploadJobRepository.findById(jobId).orElse(null);
        if (job == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (!job.canLink(now)) {
            return;
        }

        job.markLinkRunning();
        fileUploadJobRepository.save(job);
        readModelWriter.upsert(job);

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
            fileUploadJobRepository.save(job);
            readModelWriter.upsert(job);
        } catch (Exception e) {
            boolean willRetry = job.markLinkRetryOrFailed(e.getMessage(), retryDelaySeconds, now);
            fileUploadJobRepository.save(job);
            readModelWriter.upsert(job);
            if (!willRetry) {
                log.error("Link job failed permanently. uploadId: {}, sheetPostId: {}", job.getUploadId(),
                        job.getSheetPostId(), e);
            }
        }
    }
}
