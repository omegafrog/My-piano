package com.omegafrog.My.piano.app.cache;

import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Component
public class SingleFlightCoordinator {
    private final ConcurrentHashMap<String, CompletableFuture<Object>> inFlight = new ConcurrentHashMap<>();

    private String mapKey(String namespace, Object key) {
        return namespace + "::" + String.valueOf(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T loadBlocking(String namespace, Object key, Supplier<T> loader, long waitTimeoutMs)
            throws TimeoutException {
        String mapKey = mapKey(namespace, key);
        CompletableFuture<Object> mine = new CompletableFuture<>();
        CompletableFuture<Object> existing = inFlight.putIfAbsent(mapKey, mine);

        if (existing != null) {
            try {
                return (T) existing.get(waitTimeoutMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("single-flight wait interrupted", e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new RuntimeException("single-flight loader failed", cause);
            }
        }

        try {
            T loaded = loader.get();
            mine.complete(loaded);
            return loaded;
        } catch (Exception e) {
            mine.completeExceptionally(e);
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException(e);
        } finally {
            inFlight.remove(mapKey, mine);
        }
    }

    public <T> boolean refreshAsyncIfAbsent(
            String namespace,
            Object key,
            Supplier<T> loader,
            Executor executor
    ) {
        String mapKey = mapKey(namespace, key);
        CompletableFuture<Object> mine = new CompletableFuture<>();
        CompletableFuture<Object> existing = inFlight.putIfAbsent(mapKey, mine);
        if (existing != null) {
            return false;
        }

        AtomicBoolean started = new AtomicBoolean(false);
        try {
            executor.execute(() -> {
                started.set(true);
                try {
                    T loaded = loader.get();
                    mine.complete(loaded);
                } catch (Exception e) {
                    mine.completeExceptionally(e);
                } finally {
                    inFlight.remove(mapKey, mine);
                }
            });
            return true;
        } catch (Exception e) {
            if (!started.get()) {
                inFlight.remove(mapKey, mine);
            }
            return false;
        }
    }

    public int inFlightSize() {
        return inFlight.size();
    }
}
