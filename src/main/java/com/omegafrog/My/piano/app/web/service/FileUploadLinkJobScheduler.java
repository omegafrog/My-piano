package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJob;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileUploadLinkJobScheduler {

    private final FileUploadJobRepository fileUploadJobRepository;
    private final FileUploadLinkService fileUploadLinkService;

    @Value("${file-upload.link.batch-size:5}")
    private int batchSize;

    @Value("${file-upload.link.retry-delay-seconds:5}")
    private int retryDelaySeconds;

    @Scheduled(fixedDelayString = "${file-upload.link.poll-delay-ms:1000}")
    public void processLinkableJobs() {
        List<FileUploadJob> jobs = fileUploadJobRepository.findLinkableJobs(LocalDateTime.now(), batchSize);
        for (FileUploadJob job : jobs) {
            fileUploadLinkService.processLinkJob(job.getId(), retryDelaySeconds);
        }
    }
}
