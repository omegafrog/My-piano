package com.omegafrog.My.piano.app.web.domain;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

@Slf4j
@RequiredArgsConstructor
public class S3UploadFileExecutor {

    private final S3Template s3Template;
    @Value("${spring.cloud.aws.bucket.name}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Async("ThreadPoolTaskExecutor")
    public void uploadSheet(File file, String filename, ObjectMetadata metadata) throws IOException {
        log.info("upload sheet start");
        FileInputStream inputStream = new FileInputStream(file);
        s3Template.upload(bucketName, filename, inputStream, metadata);
        log.info("upload sheet end");
    }

    @Async("ThreadPoolTaskExecutor")
    public void uploadProfileImg(MultipartFile profileImg, String filename, ObjectMetadata metadata) throws IOException {
        log.info("upload profile start");
        // 임시 파일을 만들어 저장
        File temp = File.createTempFile("temp", ".data");
        temp.deleteOnExit();
        ReadableByteChannel src = profileImg.getResource().readableChannel();
        FileChannel dest = new FileOutputStream(temp).getChannel();
        dest.transferFrom(src,0, profileImg.getSize());
        
        FileInputStream inputStream = new FileInputStream(temp);
        s3Template.upload(bucketName, filename, inputStream, metadata);
        log.info("upload profile end");
    }
    @Async("ThreadPoolTaskExecutor")
    public void removeProfileImg(String url){
        log.info("remove profile start");
        String s3Url = new StringBuilder().append("s3://").append(bucketName).append("/").append(url).toString();

        s3Template.deleteObject(s3Url);
        log.info("remove profile end");

    }

}
