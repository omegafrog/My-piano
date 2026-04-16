package com.omegafrog.My.piano.app.batch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.ApplicationArguments;
import org.springframework.batch.core.launch.JobLauncher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchStartupJobRunnerTest {

    private static final ApplicationArguments EMPTY_ARGS = new DefaultApplicationArguments(new String[0]);

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job persistViewCountJob;

    @Mock
    private Job sheetPostCacheWarmupJob;

    @Test
    @DisplayName("startup runner launches both jobs with startup-specific distinct parameters")
    void runShouldLaunchBothJobsWithDistinctParameters() throws Exception {
        when(persistViewCountJob.getName()).thenReturn("PersistViewCountJob");
        when(sheetPostCacheWarmupJob.getName()).thenReturn("SheetPostCacheWarmupJob");
        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(mock(JobExecution.class));

        BatchStartupJobRunner runner =
                new BatchStartupJobRunner(jobLauncher, persistViewCountJob, sheetPostCacheWarmupJob);

        runner.run(EMPTY_ARGS);

        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        ArgumentCaptor<JobParameters> paramsCaptor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher, times(2)).run(jobCaptor.capture(), paramsCaptor.capture());

        List<Job> jobs = jobCaptor.getAllValues();
        List<JobParameters> params = paramsCaptor.getAllValues();

        assertThat(jobs).containsExactly(persistViewCountJob, sheetPostCacheWarmupJob);
        assertThat(params).hasSize(2);
        assertThat(params.get(0).getString("trigger")).isEqualTo("startup");
        assertThat(params.get(1).getString("trigger")).isEqualTo("startup");
        assertThat(params.get(0).getString("startupJob")).isEqualTo("persistViewCountJob");
        assertThat(params.get(1).getString("startupJob")).isEqualTo("sheetPostCacheWarmupJob");
        assertThat(params.get(0).getLong("requestedAt")).isNotNull();
        assertThat(params.get(1).getLong("requestedAt")).isNotNull();
    }

    @Test
    @DisplayName("startup runner keeps launching later jobs when an earlier launch fails")
    void runShouldContinueLaunchingJobsWhenFirstLaunchFails() throws Exception {
        when(persistViewCountJob.getName()).thenReturn("PersistViewCountJob");
        when(sheetPostCacheWarmupJob.getName()).thenReturn("SheetPostCacheWarmupJob");
        when(jobLauncher.run(eq(persistViewCountJob), any(JobParameters.class)))
                .thenThrow(new JobExecutionAlreadyRunningException("already running"));
        when(jobLauncher.run(eq(sheetPostCacheWarmupJob), any(JobParameters.class)))
                .thenReturn(mock(JobExecution.class));

        BatchStartupJobRunner runner =
                new BatchStartupJobRunner(jobLauncher, persistViewCountJob, sheetPostCacheWarmupJob);

        runner.run(EMPTY_ARGS);

        verify(jobLauncher, times(1)).run(eq(persistViewCountJob), any(JobParameters.class));
        verify(jobLauncher, times(1)).run(eq(sheetPostCacheWarmupJob), any(JobParameters.class));
    }
}
