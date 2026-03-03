package com.omegafrog.My.piano.app.cache;

import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Component
public class SingleFlightCoordinator {
    /**
     * key(namespace::id) 단위로 현재 진행 중인 로딩 future를 보관한다.
     * 같은 키 요청이 동시에 들어오면 하나의 로더만 실행하고 나머지는 동일 future 결과를 공유한다.
     */
    private final ConcurrentHashMap<String, CompletableFuture<Object>> inFlight = new ConcurrentHashMap<>();

    private String mapKey(String namespace, Object key) {
        return namespace + "::" + String.valueOf(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T loadBlocking(String namespace, Object key, Supplier<T> loader, long waitTimeoutMs)
            throws TimeoutException {
        // 동일 키에 대한 선행 로딩이 있으면 새 로딩을 만들지 않고 해당 결과를 대기한다.
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

        // 선행 로딩이 없을 때만 실제 로더를 실행하고, 완료 후 inFlight에서 제거한다.
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
        // stale-window 비동기 갱신용 경로: 같은 키에 이미 갱신이 돌고 있으면 false를 반환한다.
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
