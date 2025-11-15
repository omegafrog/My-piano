package com.omegafrog.My.piano.app.utils.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
public class PerformanceLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceLoggingAspect.class);

    @Pointcut("execution(* com.omegafrog.My.piano.app.web.controller.FileUploadController.uploadFile(..)) || " +
              "execution(* com.omegafrog.My.piano.app.web.controller.SheetPostController.writeSheetPost(..)) || " +
              "execution(* com.omegafrog.My.piano.app.web.service.FileUploadService.createTempFile(..)) || " +
              "execution(* com.omegafrog.My.piano.app.web.domain.LocalFileStorageExecutor.generateThumbnail(..)) || " +
              "execution(* com.omegafrog.My.piano.app.web.domain.S3UploadFileExecutor.generateThumbnail(..)) || " +
              "execution(* com.omegafrog.My.piano.app.web.domain.LocalFileStorageExecutor.uploadSheetAsync(..)) || " +
              "execution(* com.omegafrog.My.piano.app.web.domain.S3UploadFileExecutor.uploadSheetAsync(..)) || "+
              "execution(* com.omegafrog.My.piano.app.web.domain.LocalFileStorageExecutor.uploadThumbnailAsync(..))")
    public void controllerMethodsToLog() {}

    @Around("controllerMethodsToLog()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("{} execution started.", methodName);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object result = joinPoint.proceed();

        stopWatch.stop();
        log.info("{} execution finished. Total time: {} ms", methodName, stopWatch.getTotalTimeMillis());

        return result;
    }
}
