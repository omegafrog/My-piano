package com.omegafrog.My.piano.app;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

@Configuration
@EnableAsync
@Profile("test")
public class TestAsyncConfig {

    @Bean(name = "ThreadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(2);
        taskExecutor.setMaxPoolSize(2);
        taskExecutor.setQueueCapacity(100);

        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory("ThreadPoolTaskExecutor:");
        threadFactory.setDaemon(true);
        taskExecutor.setThreadFactory(threadFactory);

        taskExecutor.setWaitForTasksToCompleteOnShutdown(false);
        taskExecutor.setAwaitTerminationSeconds(1);
        taskExecutor.initialize();
        return taskExecutor;
    }
}
