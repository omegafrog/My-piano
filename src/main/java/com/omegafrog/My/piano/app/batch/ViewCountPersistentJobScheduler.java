package com.omegafrog.My.piano.app.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ViewCountPersistentJobScheduler {
    private final JobLauncher jobLauncher;

    @Qualifier("persistViewCountJob")
    @Autowired
    private Job viewCountPersistentJob;

    @Scheduled(cron = "0 0/1 * * * ?")
    //@Scheduled(cron = "0 0 0 * * ?")
    public void jobScheduled() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        Map<String, JobParameter<?>> jobParameterMap = new HashMap<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        Date time = new Date();
        String timeString = simpleDateFormat.format(time);
        jobParameterMap.put("date", new JobParameter<>(timeString, String.class));
        JobParameters jobParameters = new JobParameters(jobParameterMap);
        JobExecution jobExecution = jobLauncher.run(viewCountPersistentJob, jobParameters);
        while (jobExecution.isRunning()) {
            log.info("batch is running. Start at {}.", jobExecution.getJobParameters().getString("date"));
        }

    }
}
