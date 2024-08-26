package com.omegafrog.My.piano.app.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("!test")
@Slf4j
public class RankingScheduler {
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job UpdateRankingJob;

//    @Scheduled(cron = "0 0 12 * * ?")
    @Scheduled(cron = "0 0/30 * * * ?")
    public void jobScheduled() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        log.info("scheduling start");
        Map<String, JobParameter<?>> jobParameterMap = new HashMap<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        Date time = new Date();
        String timeString = simpleDateFormat.format(time);
        jobParameterMap.put("date", new JobParameter<>(timeString, String.class));
        JobParameters jobParameters = new JobParameters(jobParameterMap);
        JobExecution jobExecution = jobLauncher.run(UpdateRankingJob, jobParameters);
        while (jobExecution.isRunning()) {
            log.info("batch is running. Start at {}.", jobExecution.getJobParameters().getString("date"));
        }
    }
}
