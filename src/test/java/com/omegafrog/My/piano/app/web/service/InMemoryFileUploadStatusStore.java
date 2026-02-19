package com.omegafrog.My.piano.app.web.service;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.omegafrog.My.piano.app.TestResettable;

@Component
@Profile("test")
public class InMemoryFileUploadStatusStore implements FileUploadStatusStore, TestResettable {

    private final Map<String, Map<String, String>> store = new ConcurrentHashMap<>();

    @Override
    public void putAll(String uploadId, Map<String, String> fields, Duration ttl) {
        store.compute(uploadId, (key, existing) -> {
            Map<String, String> next = existing == null ? new HashMap<>() : new HashMap<>(existing);
            next.putAll(fields);
            return next;
        });
    }

    @Override
    public void put(String uploadId, String field, String value) {
        putAll(uploadId, Collections.singletonMap(field, value), Duration.ZERO);
    }

    @Override
    public String get(String uploadId, String field) {
        Map<String, String> entry = store.get(uploadId);
        return entry == null ? null : entry.get(field);
    }

    @Override
    public Map<String, String> entries(String uploadId) {
        Map<String, String> entry = store.get(uploadId);
        return entry == null ? Map.of() : new HashMap<>(entry);
    }

    @Override
    public boolean exists(String uploadId) {
        return store.containsKey(uploadId);
    }

    @Override
    public void reset() {
        store.clear();
    }
}
