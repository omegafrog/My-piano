package com.omegafrog.My.piano.app.web.domain;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class S3UploadFileExecutor {

    private final S3Template s3Template;
    @Value("${spring.cloud.aws.bucket.name}")
    private String bucketName;

    @Async("ThreadPoolTaskExecutor")
    public void uploadSheet(File file, String filename, ObjectMetadata metadata) throws IOException {
        log.info("upload sheet start");
        FileInputStream inputStream = new FileInputStream(file);
        s3Template.upload(bucketName, filename, inputStream, metadata);
        log.info("upload sheet end");
    }
}
