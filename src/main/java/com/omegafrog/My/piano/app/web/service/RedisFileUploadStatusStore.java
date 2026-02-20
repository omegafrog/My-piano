package com.omegafrog.My.piano.app.web.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class RedisFileUploadStatusStore implements FileUploadStatusStore {

    private static final String KEY_PREFIX = "file-upload:";

    @Qualifier("CommonUserRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void putAll(String uploadId, Map<String, String> fields, Duration ttl) {
        String key = KEY_PREFIX + uploadId;
        redisTemplate.opsForHash().putAll(key, fields);
        redisTemplate.expire(key, ttl);
    }

    @Override
    public void put(String uploadId, String field, String value) {
        redisTemplate.opsForHash().put(KEY_PREFIX + uploadId, field, value);
    }

    @Override
    public String get(String uploadId, String field) {
        Object value = redisTemplate.opsForHash().get(KEY_PREFIX + uploadId, field);
        return value == null ? null : String.valueOf(value);
    }

    @Override
    public Map<String, String> entries(String uploadId) {
        Map<Object, Object> raw = redisTemplate.opsForHash().entries(KEY_PREFIX + uploadId);
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : raw.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue() == null ? null : String.valueOf(entry.getValue()));
        }
        return result;
    }

    @Override
    public boolean exists(String uploadId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + uploadId));
    }
}
