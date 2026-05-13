package com.omegafrog.My.piano.app.web.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.omegafrog.My.piano.app.external.elasticsearch.ElasticSearchInstance;
import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostIndexRepository;
import com.omegafrog.My.piano.app.external.elasticsearch.exception.ElasticSearchException;
import com.omegafrog.My.piano.app.cache.SingleFlightCoordinator;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.FileStorageExecutor;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCountRepository;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostListDto;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import com.omegafrog.My.piano.app.web.enums.SheetPostSearchBackend;
import com.omegafrog.My.piano.app.web.service.cache.SheetPostCacheCoordinator;
import com.omegafrog.My.piano.app.web.service.outbox.SheetPostOutboxService;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class SheetPostApplicationServiceSearchFallbackTest {

	private final SheetPostIndexRepository sheetPostIndexRepository = mock(SheetPostIndexRepository.class);
	private final SheetPostRepository sheetPostRepository = mock(SheetPostRepository.class);
	private final UserRepository userRepository = mock(UserRepository.class);
	private final ElasticSearchInstance elasticSearchInstance = mock(ElasticSearchInstance.class);
	private final FileStorageExecutor fileStorageExecutor = mock(FileStorageExecutor.class);
	private final SheetPostViewCountRepository sheetPostViewCountRepository = mock(SheetPostViewCountRepository.class);
	private final AuthenticationUtil authenticationUtil = mock(AuthenticationUtil.class);
	private final FileUploadService fileUploadService = mock(FileUploadService.class);
	private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
	private final SheetPostOutboxService sheetPostOutboxService = mock(SheetPostOutboxService.class);
	private final ObjectProvider<Executor> executorProvider = mock(ObjectProvider.class);

	private SimpleMeterRegistry registry;
	private SheetPostApplicationService service;

	@BeforeEach
	void setUp() {
		registry = new SimpleMeterRegistry();
		Metrics.addRegistry(registry);
		when(executorProvider.getIfAvailable()).thenReturn(Executors.newSingleThreadExecutor());
		SheetPostCacheCoordinator cacheCoordinator = new SheetPostCacheCoordinator(
				new ConcurrentMapCacheManager("sheetpostList", "sheetpostDetail"),
				new SingleFlightCoordinator(),
				executorProvider);
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
				cacheCoordinator);
	}

	@AfterEach
	void tearDown() {
		Metrics.removeRegistry(registry);
		registry.close();
	}

	@Test
	void getSheetPostsFallsBackToDbWhenElasticsearchFails() {
		Pageable pageable = PageRequest.of(0, 20);
		SheetPostListDto row = new SheetPostListDto(
				1L,
				"title",
				"artist",
				"profile",
				"sheet",
				Difficulty.EASY,
				null,
				Instrument.PIANO_KEY_88,
				java.time.LocalDateTime.now(),
				1000);
		when(elasticSearchInstance.searchSheetPost(eq("bach"), any(), any(), any(), eq(pageable)))
				.thenThrow(new ElasticSearchException("timeout"));
		when(sheetPostRepository.searchSheetPosts(eq("bach"), any(), any(), any(), eq(pageable)))
				.thenReturn(new PageImpl<>(List.of(row), pageable, 1));
		when(sheetPostViewCountRepository.getViewCountsByIds(anyList())).thenReturn(Map.of(1L, 3));

		Page<SheetPostListDto> result = service.getSheetPosts(
				"bach", null, null, null, SheetPostSearchBackend.ES, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getTitle()).isEqualTo("title");
		assertThat(result.getContent().get(0).getViewCount()).isEqualTo(3);
		assertThat(registry.counter(
				"mypiano.elasticsearch.search.fallbacks",
				"reason", "ElasticSearchException").count()).isEqualTo(1.0);
		verify(sheetPostRepository).searchSheetPosts(eq("bach"), any(), any(), any(), eq(pageable));
	}
}
