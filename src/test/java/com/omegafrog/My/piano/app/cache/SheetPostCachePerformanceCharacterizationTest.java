package com.omegafrog.My.piano.app.cache;

import com.omegafrog.My.piano.app.external.elasticsearch.ElasticSearchInstance;
import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostIndexRepository;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.FileStorageExecutor;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCountRepository;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostListDto;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import com.omegafrog.My.piano.app.web.service.FileUploadService;
import com.omegafrog.My.piano.app.web.service.SheetPostApplicationService;
import com.omegafrog.My.piano.app.web.service.cache.SheetPostCacheCoordinator;
import com.omegafrog.My.piano.app.web.service.outbox.SheetPostOutboxService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SheetPostCachePerformanceCharacterizationTest {

    @Mock
    private SheetPostIndexRepository sheetPostIndexRepository;
    @Mock
    private SheetPostRepository sheetPostRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ElasticSearchInstance elasticSearchInstance;
    @Mock
    private FileStorageExecutor fileStorageExecutor;
    @Mock
    private SheetPostViewCountRepository sheetPostViewCountRepository;
    @Mock
    private AuthenticationUtil authenticationUtil;
    @Mock
    private FileUploadService fileUploadService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private SheetPostOutboxService sheetPostOutboxService;
    @Mock
    private ObjectProvider<Executor> executorProvider;

    private ConcurrentMapCacheManager cacheManager;
    private ExecutorService refreshExecutor;
    private SheetPostApplicationService cachedService;
    private SheetPostApplicationService uncachedService;

    @BeforeEach
    void setUp() {
        cacheManager = new ConcurrentMapCacheManager("sheetpostList", "sheetpostDetail");
        refreshExecutor = Executors.newFixedThreadPool(2);
        when(executorProvider.getIfAvailable()).thenReturn(refreshExecutor);
        when(sheetPostViewCountRepository.getViewCountsByIds(anyList())).thenReturn(Map.of(1L, 0));

        cachedService = createService(cacheManager);
        uncachedService = createService(new NoOpCacheManager());
    }

    @AfterEach
    void tearDown() {
        refreshExecutor.shutdownNow();
    }

    @Test
    @DisplayName("same list query becomes materially faster after cache warm-up")
    void cachedListRequestShouldBeFasterThanColdRequest() throws Exception {
        AtomicInteger searchCalls = new AtomicInteger();

        when(elasticSearchInstance.searchSheetPost(any(), any(), any(), any(), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    searchCalls.incrementAndGet();
                    Thread.sleep(180);
                    Pageable pageable = invocation.getArgument(4);
                    return Pair.of(new PageImpl<>(List.of(1L), pageable, 1), "raw");
                });

        when(sheetPostRepository.findByIds(anyList(), any(Pageable.class)))
                .thenReturn(List.of(dto(1L, "cached-title")));

        Duration cold = measure(() -> cachedService.getSheetPosts(null, null, null, null, PageRequest.of(0, 20)));
        Duration warm = measure(() -> cachedService.getSheetPosts(null, null, null, null, PageRequest.of(0, 20)));

        assertEquals(1, searchCalls.get(), "warm request should reuse cached payload");
        assertTrue(cold.toMillis() >= 150, "cold request should include mocked backend latency");
        assertTrue(warm.toMillis() < 80, "warm request should avoid expensive backend round-trip");
        assertTrue(warm.multipliedBy(3).compareTo(cold) < 0,
                "warm request should be materially faster than cold request");
    }

    @Test
    @DisplayName("warm cache hit outperforms no-cache baseline with fewer backend calls")
    void warmCacheHitShouldBeatNoCacheBaseline() throws Exception {
        AtomicInteger searchCalls = new AtomicInteger();

        when(elasticSearchInstance.searchSheetPost(any(), any(), any(), any(), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    searchCalls.incrementAndGet();
                    Thread.sleep(180);
                    Pageable pageable = invocation.getArgument(4);
                    return Pair.of(new PageImpl<>(List.of(1L), pageable, 1), "raw");
                });

        when(sheetPostRepository.findByIds(anyList(), any(Pageable.class)))
                .thenReturn(List.of(dto(1L, "cached-title")));

        PerformanceSnapshot uncached = captureSamples(
                () -> uncachedService.getSheetPosts(null, null, null, null, PageRequest.of(0, 20)),
                5
        );

        cachedService.getSheetPosts(null, null, null, null, PageRequest.of(0, 20));
        PerformanceSnapshot warmCached = captureSamples(
                () -> cachedService.getSheetPosts(null, null, null, null, PageRequest.of(0, 20)),
                5
        );

        Duration uncachedMedian = uncached.median();
        Duration warmCachedMedian = warmCached.median();
        double improvementRatio = (double) uncachedMedian.toNanos() / warmCachedMedian.toNanos();

        assertEquals(6, searchCalls.get(), "uncached baseline should hit backend on every sample while warm cache should not");
        assertTrue(uncachedMedian.toMillis() >= 150, "uncached median should include mocked backend latency");
        assertTrue(warmCachedMedian.toMillis() < 80, "warm cache median should avoid expensive backend round-trip");
        assertTrue(improvementRatio >= 3.0,
                () -> "expected at least 3x improvement but got " + formatSnapshot(uncachedMedian, warmCachedMedian, improvementRatio, searchCalls.get()));

        System.out.println(formatSnapshot(uncachedMedian, warmCachedMedian, improvementRatio, searchCalls.get()));
    }

    private SheetPostApplicationService createService(CacheManager cacheManager) {
        SheetPostCacheCoordinator sheetPostCacheCoordinator = new SheetPostCacheCoordinator(
                cacheManager,
                new SingleFlightCoordinator(),
                executorProvider
        );

        return new SheetPostApplicationService(
                sheetPostIndexRepository,
                sheetPostRepository,
                userRepository,
                elasticSearchInstance,
                fileStorageExecutor,
                sheetPostViewCountRepository,
                authenticationUtil,
                fileUploadService,
                eventPublisher,
                sheetPostOutboxService,
                sheetPostCacheCoordinator
        );
    }

    private PerformanceSnapshot captureSamples(ThrowingRunnable runnable, int sampleCount) throws Exception {
        List<Duration> samples = new ArrayList<>();
        for (int i = 0; i < sampleCount; i++) {
            samples.add(measure(runnable));
        }
        return new PerformanceSnapshot(samples);
    }

    private Duration measure(ThrowingRunnable runnable) throws Exception {
        long start = System.nanoTime();
        runnable.run();
        return Duration.ofNanos(System.nanoTime() - start);
    }

    private String formatSnapshot(Duration uncachedMedian, Duration warmCachedMedian, double improvementRatio, int backendCalls) {
        return "uncached median=" + String.format("%.3f", uncachedMedian.toNanos() / 1_000_000.0)
                + "ms, warm-cache median=" + String.format("%.3f", warmCachedMedian.toNanos() / 1_000_000.0)
                + "ms, improvement=" + String.format("%.2f", improvementRatio)
                + "x, backendCalls=" + backendCalls;
    }

    private SheetPostListDto dto(Long id, String title) {
        return new SheetPostListDto(
                id,
                title,
                "artist",
                "profile",
                "sheet",
                Difficulty.MEDIUM,
                null,
                Instrument.GUITAR_ACOUSTIC,
                LocalDateTime.now(),
                1000
        );
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private record PerformanceSnapshot(List<Duration> samples) {
        private Duration median() {
            List<Duration> sorted = new ArrayList<>(samples);
            sorted.sort(Comparator.naturalOrder());
            return sorted.get(sorted.size() / 2);
        }
    }
}
