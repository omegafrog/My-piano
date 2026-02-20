package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.FileStorageExecutor;
import com.omegafrog.My.piano.app.web.domain.fileUpload.StagePdfStorage;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJob;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJobRepository;
import com.omegafrog.My.piano.app.web.infra.fileUpload.FileUploadRedisReadModelWriter;
import io.awspring.cloud.s3.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class FileUploadJobScheduler {

    private final FileUploadJobRepository fileUploadJobRepository;
    private final FileStorageExecutor fileStorageExecutor;
    private final StagePdfStorage stagePdfStorage;
    private final FileUploadRedisReadModelWriter readModelWriter;
    private final TransactionTemplate transactionTemplate;

    public FileUploadJobScheduler(
            FileUploadJobRepository fileUploadJobRepository,
            FileStorageExecutor fileStorageExecutor,
            StagePdfStorage stagePdfStorage,
            FileUploadRedisReadModelWriter readModelWriter,
            TransactionTemplate transactionTemplate
    ) {
        this.fileUploadJobRepository = fileUploadJobRepository;
        this.fileStorageExecutor = fileStorageExecutor;
        this.stagePdfStorage = stagePdfStorage;
        this.readModelWriter = readModelWriter;
        this.transactionTemplate = transactionTemplate;
    }

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
        FileUploadJob claimed = claimJob(jobId);
        if (claimed == null) {
            return;
        }

        try {
            UploadResult result = executeUpload(claimed);
            completeJobSuccess(claimed.getId(), result);
        } catch (Exception e) {
            completeJobFailure(claimed.getId(), e);
        }
    }

    private FileUploadJob claimJob(Long jobId) {
        return transactionTemplate.execute(status -> {
            FileUploadJob job = fileUploadJobRepository.findById(jobId).orElse(null);
            if (job == null || !job.canStart(LocalDateTime.now())) {
                return null;
            }

            job.markRunning();
            FileUploadJob saved = fileUploadJobRepository.save(job);

            afterCommit(() -> readModelWriter.upsert(saved));
            return saved;
        });
    }

    private UploadResult executeUpload(FileUploadJob job) throws IOException {
        return upload(job);
    }

    private void completeJobSuccess(Long jobId, UploadResult result) {
        transactionTemplate.executeWithoutResult(status -> {
            FileUploadJob job = fileUploadJobRepository.findById(jobId)
                    .orElseThrow(() -> new IllegalStateException("Upload job not found: " + jobId));

            job.updateUploadResult(result.sheetUrl(), result.thumbnailUrls(), result.pageNum());
            job.markCompleted(LocalDateTime.now());

            if (job.getSheetPostId() != null) {
                job.markLinkPending(LocalDateTime.now());
            }

            FileUploadJob saved = fileUploadJobRepository.save(job);

            afterCommit(() -> {
                readModelWriter.upsert(saved);
                stagePdfStorage.deleteIfExists(saved.getStagedFilePath());
            });
        });
    }

    private void completeJobFailure(Long jobId, Exception exception) {
        transactionTemplate.executeWithoutResult(status -> {
            FileUploadJob job = fileUploadJobRepository.findById(jobId)
                    .orElseThrow(() -> new IllegalStateException("Upload job not found: " + jobId));

            boolean willRetry = job.markRetryOrFailed(exception.getMessage(), retryDelaySeconds, LocalDateTime.now());
            FileUploadJob saved = fileUploadJobRepository.save(job);

            afterCommit(() -> {
                readModelWriter.upsert(saved);
                if (!willRetry) {
                    stagePdfStorage.deleteIfExists(saved.getStagedFilePath());
                }
            });

            if (willRetry) {
                log.warn("Upload job retry scheduled. uploadId: {}, attempts: {}/{}", job.getUploadId(),
                        job.getAttemptCount(), job.getMaxAttempts());
            } else {
                log.error("Upload job failed permanently. uploadId: {}", job.getUploadId(), exception);
            }
        });
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

            String sheetUrl = fileStorageExecutor.buildSheetUrl(job.getUuidFileName());
            String thumbnailUrls = fileStorageExecutor.buildThumbnailUrls(job.getUuidFileName(), pageNum);

            return new UploadResult(sheetUrl, thumbnailUrls, pageNum);
        }
    }

    private record UploadResult(String sheetUrl, String thumbnailUrls, int pageNum) {
    }
}
