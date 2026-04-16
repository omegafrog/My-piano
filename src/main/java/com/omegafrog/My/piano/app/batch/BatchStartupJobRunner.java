package com.omegafrog.My.piano.app.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class BatchStartupJobRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;

    @Qualifier("persistViewCountJob")
    private final Job persistViewCountJob;

    @Qualifier("sheetPostCacheWarmupJob")
    private final Job sheetPostCacheWarmupJob;

    @Override
    public void run(ApplicationArguments args) {
        launchJob("persistViewCountJob", persistViewCountJob);
        launchJob("sheetPostCacheWarmupJob", sheetPostCacheWarmupJob);
    }

    private void launchJob(String startupJob, Job job) {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("trigger", "startup")
                .addString("startupJob", startupJob)
                .addLong("requestedAt", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobLauncher.run(job, jobParameters);
            log.info("startup batch job launched. jobName={}, startupJob={}", job.getName(), startupJob);
        } catch (Exception e) {
            log.error("startup batch job launch failed. jobName={}, startupJob={}", job.getName(), startupJob, e);
        }
    }
}
