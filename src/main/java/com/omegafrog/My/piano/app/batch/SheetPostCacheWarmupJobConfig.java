package com.omegafrog.My.piano.app.batch;

import com.omegafrog.My.piano.app.web.service.SheetPostApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SheetPostCacheWarmupJobConfig {

    private final PlatformTransactionManager transactionManager;
    private final SheetPostApplicationService sheetPostApplicationService;

    @Bean("sheetPostCacheWarmupJob")
    public Job sheetPostCacheWarmupJob(JobRepository jobRepository) {
        return new JobBuilder("SheetPostCacheWarmupJob", jobRepository)
                .start(sheetPostCacheWarmupStep(jobRepository))
                .build();
    }

    @Bean
    public Step sheetPostCacheWarmupStep(JobRepository jobRepository) {
        return new StepBuilder("sheetPostCacheWarmupStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    sheetPostApplicationService.warmupSheetPostCaches();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
