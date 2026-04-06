package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcess;
import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcessRepository;
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
public class FileUploadLinkScheduler {

    private final FileUploadProcessRepository fileUploadProcessRepository;
    private final FileUploadLinkService fileUploadLinkService;

    @Value("${file-upload.link.batch-size:5}")
    private int batchSize;

    @Value("${file-upload.link.retry-delay-seconds:5}")
    private int retryDelaySeconds;

    @Scheduled(fixedDelayString = "${file-upload.link.poll-delay-ms:1000}")
    public void processLinkableJobs() {
        List<FileUploadProcess> jobs = fileUploadProcessRepository.findLinkableJobs(LocalDateTime.now(), batchSize);
        for (FileUploadProcess job : jobs) {
            fileUploadLinkService.processLinkJob(job.getId(), retryDelaySeconds);
        }
    }
}
