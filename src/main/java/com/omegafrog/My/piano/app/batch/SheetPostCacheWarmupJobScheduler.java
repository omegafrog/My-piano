package com.omegafrog.My.piano.app.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("!test")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "cache.warmup", name = "enabled", havingValue = "true")
@Slf4j
public class SheetPostCacheWarmupJobScheduler {

    private final JobLauncher jobLauncher;

    @Qualifier("sheetPostCacheWarmupJob")
    private final Job sheetPostCacheWarmupJob;

    @Scheduled(fixedDelayString = "${cache.warmup.fixed-delay-ms:60000}")
    public void runWarmupJob() {
        Map<String, JobParameter<?>> params = new HashMap<>();
        params.put("requestedAt", new JobParameter<>(System.currentTimeMillis(), Long.class));
        JobParameters jobParameters = new JobParameters(params);
        try {
            jobLauncher.run(sheetPostCacheWarmupJob, jobParameters);
            log.info("sheetpost cache warmup job executed");
        } catch (Exception e) {
            log.error("sheetpost cache warmup job failed", e);
        }
    }
}
