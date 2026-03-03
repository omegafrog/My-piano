package com.omegafrog.My.piano.app.batch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SheetPostCacheWarmupJobSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job sheetPostCacheWarmupJob;

    @Test
    @DisplayName("scheduler triggers warmup job launcher")
    void runWarmupJobShouldInvokeJobLauncher() throws Exception {
        SheetPostCacheWarmupJobScheduler scheduler =
                new SheetPostCacheWarmupJobScheduler(jobLauncher, sheetPostCacheWarmupJob);

        scheduler.runWarmupJob();

        verify(jobLauncher, times(1)).run(any(Job.class), any(JobParameters.class));
    }
}
