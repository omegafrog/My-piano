package com.omegafrog.My.piano.app.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class ViewCountPersistentJobScheduler {
    private final JobLauncher jobLauncher;

    @Qualifier("persistCountJob")
    @Autowired
    private Job countPersistentJob;

    @Scheduled(cron = "0 0 */6 * * ?")
    //@Scheduled(cron = "0 0/1 * * * ?")
    public void jobScheduled() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        long jobStartTime = System.currentTimeMillis();
        
        Map<String, JobParameter<?>> jobParameterMap = new HashMap<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        Date time = new Date();
        String timeString = simpleDateFormat.format(time);
        jobParameterMap.put("date", new JobParameter<>(timeString, String.class));
        JobParameters jobParameters = new JobParameters(jobParameterMap);
        
        log.info("Starting ViewCount/LikeCount batch job at {}", timeString);
        
        JobExecution jobExecution = jobLauncher.run(countPersistentJob, jobParameters);
        while (jobExecution.isRunning()) {
            log.debug("Batch is running. Started at {}.", jobExecution.getJobParameters().getString("date"));
        }
        
        long jobEndTime = System.currentTimeMillis();
        long totalJobTime = jobEndTime - jobStartTime;
        
        // 배치 실행 결과 메트릭 로깅
        BatchStatus status = jobExecution.getStatus();
        ExitStatus exitStatus = jobExecution.getExitStatus();
        
        log.info("ViewCount/LikeCount batch job completed - Status: {}, Exit: {}, Total time: {}ms, Steps executed: {}", 
                status, exitStatus.getExitCode(), totalJobTime, jobExecution.getStepExecutions().size());
        
        if (status == BatchStatus.FAILED) {
            log.error("Batch job failed with exit description: {}", exitStatus.getExitDescription());
            jobExecution.getAllFailureExceptions().forEach(ex -> 
                log.error("Batch failure exception: ", ex)
            );
        }
        
        // 각 Step별 메트릭 로깅
        jobExecution.getStepExecutions().forEach(stepExecution -> {
            log.info("Step: {}, Read: {}, Write: {}, Skip: {}, Commit: {}, Rollback: {}, Duration: {}ms",
                    stepExecution.getStepName(),
                    stepExecution.getReadCount(),
                    stepExecution.getWriteCount(),
                    stepExecution.getSkipCount(),
                    stepExecution.getCommitCount(),
                    stepExecution.getRollbackCount(),
                    java.time.Duration.between(stepExecution.getStartTime(), stepExecution.getEndTime()).toMillis()
            );
        });
    }
}
