package com.omegafrog.My.piano.app.web.service;

import java.time.Duration;
import java.util.Map;

public interface FileUploadStatusStore {
    void putAll(String uploadId, Map<String, String> fields, Duration ttl);

    void put(String uploadId, String field, String value);

    String get(String uploadId, String field);

    Map<String, String> entries(String uploadId);

    boolean exists(String uploadId);
}
