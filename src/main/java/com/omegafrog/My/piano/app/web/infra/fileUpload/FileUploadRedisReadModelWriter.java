package com.omegafrog.My.piano.app.web.infra.fileUpload;

import com.omegafrog.My.piano.app.web.domain.fileUpload.FileUploadProcess;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileUploadRedisReadModelWriter {

    @Qualifier("CommonUserRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${file-upload.redis.ttl-hours:24}")
    private long ttlHours;

    public void upsert(FileUploadProcess job) {
        String redisKey = redisKey(job.getUploadId());

        Map<String, String> hash = new HashMap<>();
        hash.put("status", job.getStatus().name());
        hash.put("sheetPostId", job.getSheetPostId() == null ? "" : job.getSheetPostId().toString());
        hash.put("sheetUrl", job.getSheetUrl() == null ? "" : job.getSheetUrl());
        hash.put("thumbnailUrl", job.getThumbnailUrls() == null ? "" : job.getThumbnailUrls());
        hash.put("originalFileName", job.getOriginalFileName());
        hash.put("uuidFileName", job.getUuidFileName());
        hash.put("pageNum", job.getPageNum() == null ? "" : job.getPageNum().toString());
        hash.put("createdAt", job.getCreatedAt() == null ? "" : job.getCreatedAt().toString());
        hash.put("completedAt", job.getCompletedAt() == null ? "" : job.getCompletedAt().toString());

        try {
            redisTemplate.opsForHash().putAll(redisKey, hash);
            redisTemplate.expire(redisKey, Duration.ofHours(Math.max(1, ttlHours)));
        } catch (Exception e) {
            log.warn("Failed to upsert file upload read model to Redis. uploadId: {}", job.getUploadId(), e);
        }
    }

    public void delete(String uploadId) {
        try {
            redisTemplate.delete(redisKey(uploadId));
        } catch (Exception e) {
            log.warn("Failed to delete file upload read model from Redis. uploadId: {}", uploadId, e);
        }
    }

    private static String redisKey(String uploadId) {
        return "file-upload:" + uploadId;
    }
}
