package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcess;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcessRepository;
import com.omegafrog.My.piano.app.web.infra.fileUpload.FileUploadRedisReadModelWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileUploadProcessAcquisitionService {

    private final FileUploadProcessRepository fileUploadProcessRepository;
    private final FileUploadRedisReadModelWriter readModelWriter;
    private final TransactionTemplate transactionTemplate;

    @Value("${file-upload.job.lease-seconds:300}")
    private int leaseSeconds;

    public List<FileUploadProcess> findClaimableJobs(LocalDateTime now, int batchSize) {
        return fileUploadProcessRepository.findProcessableJobs(now, batchSize);
    }

    public FileUploadProcess claimJob(Long jobId) {
        return transactionTemplate.execute(status -> {
            FileUploadProcess job = fileUploadProcessRepository.findById(jobId).orElse(null);
            if (job == null || !job.canStart(LocalDateTime.now())) {
                return null;
            }

            job.markRunning(LocalDateTime.now(), leaseSeconds);
            FileUploadProcess saved = fileUploadProcessRepository.save(job);

            afterCommit(() -> readModelWriter.upsert(saved));
            return saved;
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
}
