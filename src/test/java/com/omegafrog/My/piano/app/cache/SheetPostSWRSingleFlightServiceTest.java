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
import com.omegafrog.My.piano.app.web.service.cache.SheetPostListCacheKey;
import com.omegafrog.My.piano.app.web.service.cache.SheetPostListCachePayload;
import com.omegafrog.My.piano.app.web.service.outbox.SheetPostOutboxService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SheetPostSWRSingleFlightServiceTest {

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
    private SheetPostApplicationService service;
    private ExecutorService refreshExecutor;

    @BeforeEach
    void setUp() {
        cacheManager = new ConcurrentMapCacheManager("sheetpostList", "sheetpostDetail");
        refreshExecutor = Executors.newFixedThreadPool(4);
        when(executorProvider.getIfAvailable()).thenReturn(refreshExecutor);
        when(sheetPostViewCountRepository.getViewCountsByIds(anyList())).thenReturn(Map.of(1L, 0));

        service = new SheetPostApplicationService(
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
                cacheManager,
                new SingleFlightCoordinator(),
                executorProvider
        );
    }

    @AfterEach
    void tearDown() {
        refreshExecutor.shutdownNow();
    }

    @Test
    @DisplayName("cache stampede on miss triggers single-flight once")
    void singleFlightShouldCollapseConcurrentMisses() throws Exception {
        AtomicInteger searchCalls = new AtomicInteger();
        when(elasticSearchInstance.searchSheetPost(any(), any(), any(), any(), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    searchCalls.incrementAndGet();
                    Thread.sleep(200);
                    Pageable pageable = invocation.getArgument(4);
                    Page<Long> ids = new PageImpl<>(List.of(1L), pageable, 1);
                    return Pair.of(ids, "raw");
                });

        when(sheetPostRepository.findByIds(anyList(), any(Pageable.class)))
                .thenReturn(List.of(dto(1L, "single-flight")));

        ExecutorService callers = Executors.newFixedThreadPool(8);
        try {
            var futures = java.util.stream.IntStream.range(0, 8)
                    .mapToObj(i -> callers.submit(() -> service.getSheetPosts(null, null, null, null, PageRequest.of(0, 20))))
                    .toList();
            for (Future<?> future : futures) {
                future.get(3, TimeUnit.SECONDS);
            }
        } finally {
            callers.shutdownNow();
        }

        assertEquals(1, searchCalls.get());
    }

    @Test
    @DisplayName("stale-window returns stale and refreshes once with jittered TTL")
    void staleWindowShouldReturnStaleAndRefreshOnce() throws Exception {
        Cache listCache = cacheManager.getCache("sheetpostList");
        assertNotNull(listCache);
        String key = SheetPostListCacheKey.of(null, null, null, null, PageRequest.of(0, 20)).asStringKey();

        long now = System.currentTimeMillis();
        SheetPostListCachePayload stalePayload = new SheetPostListCachePayload(
                List.of(dto(1L, "stale-title")),
                1,
                "raw"
        );
        SwrCacheValue<SheetPostListCachePayload> stale = new SwrCacheValue<>(
                stalePayload,
                now - 100_000,
                now - 1_000,
                now + 50_000
        );
        listCache.put(key, stale);

        AtomicInteger refreshCalls = new AtomicInteger();
        CountDownLatch refreshLatch = new CountDownLatch(1);
        when(elasticSearchInstance.searchSheetPost(any(), any(), any(), any(), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    refreshCalls.incrementAndGet();
                    refreshLatch.countDown();
                    Pageable pageable = invocation.getArgument(4);
                    Page<Long> ids = new PageImpl<>(List.of(1L), pageable, 1);
                    return Pair.of(ids, "raw-refresh");
                });
        when(sheetPostRepository.findByIds(anyList(), any(Pageable.class)))
                .thenReturn(List.of(dto(1L, "fresh-title")));

        String returnedTitle = service.getSheetPosts(null, null, null, null, PageRequest.of(0, 20))
                .getContent().get(0).getTitle();
        assertEquals("stale-title", returnedTitle);

        assertTrue(refreshLatch.await(2, TimeUnit.SECONDS));
        assertEquals(1, refreshCalls.get());

        SwrCacheValue<SheetPostListCachePayload> refreshed = null;
        for (int i = 0; i < 20; i++) {
            @SuppressWarnings("unchecked")
            SwrCacheValue<SheetPostListCachePayload> current =
                    (SwrCacheValue<SheetPostListCachePayload>) listCache.get(key).get();
            refreshed = current;
            if (current != null && "fresh-title".equals(current.payload().items().get(0).getTitle())) {
                break;
            }
            Thread.sleep(50);
        }
        assertNotNull(refreshed);
        assertEquals("fresh-title", refreshed.payload().items().get(0).getTitle());

        long softTtl = refreshed.softExpireAtEpochMs() - refreshed.createdAtEpochMs();
        long hardTtl = refreshed.hardExpireAtEpochMs() - refreshed.createdAtEpochMs();
        assertTrue(softTtl >= 70_000 && softTtl <= 100_000);
        assertEquals(120_000, hardTtl);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(refreshed);
            oos.flush();
            assertTrue(baos.size() > 0);
        }
    }

    @Test
    @DisplayName("non-cacheable list key bypasses cache")
    void nonCacheableKeyShouldBypassCache() {
        when(elasticSearchInstance.searchSheetPost(any(), any(), any(), any(), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    Pageable pageable = invocation.getArgument(4);
                    return Pair.of(new PageImpl<>(List.of(1L), pageable, 1), "raw");
                });
        when(sheetPostRepository.findByIds(anyList(), any(Pageable.class)))
                .thenReturn(List.of(dto(1L, "bypass")));

        service.getSheetPosts(null, null, null, null, PageRequest.of(3, 20));

        Cache listCache = cacheManager.getCache("sheetpostList");
        assertNotNull(listCache);
        String nonCacheKey = SheetPostListCacheKey.of(null, null, null, null, PageRequest.of(3, 20)).asStringKey();
        assertNull(listCache.get(nonCacheKey));
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
}
