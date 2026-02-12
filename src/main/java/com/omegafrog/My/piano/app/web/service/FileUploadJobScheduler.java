package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.FileStorageExecutor;
import com.omegafrog.My.piano.app.web.domain.fileUpload.StagePdfStorage;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJob;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJobRepository;
import com.omegafrog.My.piano.app.web.enums.FileUploadStatus;
import io.awspring.cloud.s3.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileUploadJobScheduler {

    private final FileUploadJobRepository fileUploadJobRepository;
    private final FileStorageExecutor fileStorageExecutor;
    private final FileUploadService fileUploadService;
    private final StagePdfStorage stagePdfStorage;

    @Value("${file-upload.job.batch-size:5}")
    private int batchSize;

    @Value("${file-upload.job.retry-delay-seconds:5}")
    private int retryDelaySeconds;

    @Scheduled(fixedDelayString = "${file-upload.job.poll-delay-ms:1000}")
    public void processPendingJobs() {
        List<FileUploadJob> jobs = fileUploadJobRepository.findProcessableJobs(LocalDateTime.now(), batchSize);

        for (FileUploadJob job : jobs) {
            processSingleJob(job.getId());
        }
    }

    private void processSingleJob(Long jobId) {
        FileUploadJob job = fileUploadJobRepository.findById(jobId).orElse(null);
        if (job == null || !job.canStart(LocalDateTime.now())) {
            return;
        }

        job.markRunning();
        fileUploadJobRepository.save(job);
        fileUploadService.updateUploadStatus(job.getUploadId(), FileUploadStatus.UPLOADING);

        try {
            UploadResult result = upload(job);

            fileUploadService.updateUploadData(job.getUploadId(), result.sheetUrl(), result.thumbnailUrls(), result.pageNum());
            fileUploadService.updateUploadStatus(job.getUploadId(), FileUploadStatus.COMPLETED);
            boolean linked = fileUploadService.applyUploadDataToSheetPost(job.getUploadId());
            if (linked) {
                fileUploadService.updateUploadStatus(job.getUploadId(), FileUploadStatus.LINKED);
            }

            job.markCompleted(LocalDateTime.now());
            fileUploadJobRepository.save(job);
            stagePdfStorage.deleteIfExists(job.getStagedFilePath());
        } catch (Exception e) {
            boolean willRetry = job.markRetryOrFailed(e.getMessage(), retryDelaySeconds, LocalDateTime.now());
            fileUploadJobRepository.save(job);

            if (willRetry) {
                fileUploadService.updateUploadStatus(job.getUploadId(), FileUploadStatus.PENDING);
                log.warn("Upload job retry scheduled. uploadId: {}, attempts: {}/{}", job.getUploadId(),
                        job.getAttemptCount(), job.getMaxAttempts());
            } else {
                fileUploadService.updateUploadStatus(job.getUploadId(), FileUploadStatus.FAILED);
                stagePdfStorage.deleteIfExists(job.getStagedFilePath());
                log.error("Upload job failed permanently. uploadId: {}", job.getUploadId(), e);
            }
        }
    }

    private UploadResult upload(FileUploadJob job) throws IOException {
        File tempFile = new File(job.getStagedFilePath());
        if (!tempFile.exists()) {
            throw new IOException("Staged upload file does not exist: " + job.getStagedFilePath());
        }

        try (PDDocument document = Loader.loadPDF(tempFile)) {
            int pageNum = document.getNumberOfPages();

            fileStorageExecutor.uploadSheet(
                    tempFile,
                    job.getUuidFileName(),
                    ObjectMetadata.builder().contentType("application/pdf").build());

            fileStorageExecutor.uploadThumbnail(
                    document,
                    job.getUuidFileName(),
                    new ObjectMetadata.Builder().contentType("jpg").build());

            String sheetUrl = fileUploadService.buildSheetUrl(job.getUuidFileName());
            String thumbnailUrls = fileUploadService.buildThumbnailUrls(job.getUuidFileName(), pageNum);

            return new UploadResult(sheetUrl, thumbnailUrls, pageNum);
        }
    }

    private record UploadResult(String sheetUrl, String thumbnailUrls, int pageNum) {
    }
}
