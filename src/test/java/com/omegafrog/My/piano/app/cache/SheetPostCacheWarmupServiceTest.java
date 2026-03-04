package com.omegafrog.My.piano.app.cache;

import com.omegafrog.My.piano.app.external.elasticsearch.ElasticSearchInstance;
import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostIndexRepository;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.FileStorageExecutor;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCountRepository;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostListDto;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import com.omegafrog.My.piano.app.web.service.FileUploadService;
import com.omegafrog.My.piano.app.web.service.SheetPostApplicationService;
import com.omegafrog.My.piano.app.web.service.cache.SheetPostCacheCoordinator;
import com.omegafrog.My.piano.app.web.service.cache.SheetPostListCacheKey;
import com.omegafrog.My.piano.app.web.service.cache.SheetPostListCachePayload;
import com.omegafrog.My.piano.app.web.service.outbox.SheetPostOutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SheetPostCacheWarmupServiceTest {

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
    private SingleFlightCoordinator singleFlightCoordinator;
    private SheetPostCacheCoordinator sheetPostCacheCoordinator;
    private SheetPostApplicationService service;

    @BeforeEach
    void setUp() {
        cacheManager = new ConcurrentMapCacheManager("sheetpostList", "sheetpostDetail");
        singleFlightCoordinator = new SingleFlightCoordinator();
        when(executorProvider.getIfAvailable()).thenReturn(Executors.newFixedThreadPool(2));
        sheetPostCacheCoordinator = new SheetPostCacheCoordinator(cacheManager, singleFlightCoordinator, executorProvider);

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
                sheetPostCacheCoordinator
        );
    }

    @Test
    @DisplayName("warm-up populates caches and updates values when source changes")
    void warmupShouldPopulateAndReplaceCachedValues() throws Exception {
        AtomicReference<String> listTitle = new AtomicReference<>("title-v1");
        AtomicReference<String> detailTitle = new AtomicReference<>("detail-v1");

        when(elasticSearchInstance.searchSheetPost(any(), any(), any(), any(), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    Pageable pageable = invocation.getArgument(4);
                    Page<Long> ids = new PageImpl<>(List.of((long) pageable.getPageNumber() + 1L), pageable, 3);
                    return Pair.of(ids, "raw");
                });

        when(sheetPostRepository.findByIds(anyList(), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    List<Long> ids = invocation.getArgument(0);
                    return ids.stream()
                            .map(id -> new SheetPostListDto(
                                    id,
                                    listTitle.get(),
                                    "artist",
                                    "profile",
                                    "sheet",
                                    Difficulty.MEDIUM,
                                    null,
                                    Instrument.GUITAR_ACOUSTIC,
                                    LocalDateTime.now(),
                                    1000
                            ))
                            .toList();
                });

        when(sheetPostRepository.findById(any(Long.class)))
                .thenAnswer(invocation -> {
                    Long id = invocation.getArgument(0);
                    SheetPost post = org.mockito.Mockito.mock(SheetPost.class);
                    SheetPostDto dto = SheetPostDto.builder()
                            .id(id)
                            .title(detailTitle.get())
                            .content("content")
                            .build();
                    when(post.toDto()).thenReturn(dto);
                    when(post.getViewCount()).thenReturn(5);
                    return Optional.of(post);
                });

        service.warmupSheetPostCaches();

        String key = SheetPostListCacheKey.of(null, null, null, null, org.springframework.data.domain.PageRequest.of(0, 20))
                .asStringKey();
        Cache listCache = cacheManager.getCache("sheetpostList");
        Cache detailCache = cacheManager.getCache("sheetpostDetail");

        assertNotNull(listCache);
        assertNotNull(detailCache);
        Cache.ValueWrapper listValueWrapper = listCache.get(key);
        assertNotNull(listValueWrapper);
        @SuppressWarnings("unchecked")
        SwrCacheValue<SheetPostListCachePayload> listValue = (SwrCacheValue<SheetPostListCachePayload>) listValueWrapper.get();
        assertNotNull(listValue);
        assertEquals("title-v1", listValue.payload().items().get(0).getTitle());

        Cache.ValueWrapper detailValueWrapper = detailCache.get(1L);
        assertNotNull(detailValueWrapper);

        listTitle.set("title-v2");
        detailTitle.set("detail-v2");
        service.warmupSheetPostCaches();

        @SuppressWarnings("unchecked")
        SwrCacheValue<SheetPostListCachePayload> updatedListValue =
                (SwrCacheValue<SheetPostListCachePayload>) listCache.get(key).get();
        assertEquals("title-v2", updatedListValue.payload().items().get(0).getTitle());

        Object updatedDetail = detailCache.get(1L).get();
        assertTrue(updatedDetail instanceof SwrCacheValue<?>);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(updatedListValue);
            oos.flush();
            assertTrue(baos.size() > 0);
        }
    }
}
