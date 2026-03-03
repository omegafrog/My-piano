package com.omegafrog.My.piano.app.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleFlightCoordinatorTest {

    @Test
    @DisplayName("loadBlocking collapses concurrent calls into one loader execution")
    void loadBlockingShouldRunLoaderOnce() {
        SingleFlightCoordinator coordinator = new SingleFlightCoordinator();
        AtomicInteger loaderCalls = new AtomicInteger();

        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<CompletableFuture<Integer>> futures = java.util.stream.IntStream.range(0, 8)
                    .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return coordinator.loadBlocking(
                                    "ns",
                                    "key",
                                    () -> {
                                        loaderCalls.incrementAndGet();
                                        try {
                                            Thread.sleep(150);
                                        } catch (InterruptedException e) {
                                            Thread.currentThread().interrupt();
                                        }
                                        return 42;
                                    },
                                    1_000
                            );
                        } catch (java.util.concurrent.TimeoutException e) {
                            throw new RuntimeException(e);
                        }
                    }, executor))
                    .toList();

            List<Integer> results = futures.stream().map(CompletableFuture::join).toList();
            assertTrue(results.stream().allMatch(v -> v == 42));
            assertEquals(1, loaderCalls.get());
            assertEquals(0, coordinator.inFlightSize());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("refreshAsyncIfAbsent allows only one in-flight refresh per key")
    void refreshAsyncIfAbsentShouldStartOnlyOnce() throws Exception {
        SingleFlightCoordinator coordinator = new SingleFlightCoordinator();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch release = new CountDownLatch(1);

        try {
            AtomicInteger started = new AtomicInteger();
            List<Boolean> results = java.util.stream.IntStream.range(0, 6)
                    .mapToObj(i -> coordinator.refreshAsyncIfAbsent(
                            "ns",
                            "key",
                            () -> {
                                started.incrementAndGet();
                                try {
                                    release.await(500, TimeUnit.MILLISECONDS);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                return i;
                            },
                            executor
                    )).toList();

            assertEquals(1, results.stream().filter(v -> v).count());
            release.countDown();
            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);
            assertEquals(1, started.get());
            assertEquals(0, coordinator.inFlightSize());
        } finally {
            executor.shutdownNow();
        }
    }
}
