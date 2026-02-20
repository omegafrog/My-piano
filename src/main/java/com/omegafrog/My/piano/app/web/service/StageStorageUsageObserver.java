package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.fileUpload.StagePdfStorage;
import com.omegafrog.My.piano.app.web.domain.fileUpload.StageStorageUsage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class StageStorageUsageObserver {

    private final StagePdfStorage stagePdfStorage;

    @Value("${file-upload.stage.warn-bytes:1073741824}")
    private long warnBytes;

    @Scheduled(cron = "0 */10 * * * ?")
    public void observe() {
        try {
            StageStorageUsage usage = stagePdfStorage.usage();
            if (usage.totalBytes() >= warnBytes) {
                log.warn("Stage storage usage high. fileCount: {}, totalBytes: {}", usage.fileCount(), usage.totalBytes());
                return;
            }
            log.info("Stage storage usage. fileCount: {}, totalBytes: {}", usage.fileCount(), usage.totalBytes());
        } catch (IOException e) {
            log.warn("Failed to observe stage storage usage.", e);
        }
    }
}
