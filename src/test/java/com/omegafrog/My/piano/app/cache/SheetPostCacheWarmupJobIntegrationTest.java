package com.omegafrog.My.piano.app.cache;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import com.omegafrog.My.piano.app.Cleanup;
import com.omegafrog.My.piano.app.TestUtilConfig;
import com.omegafrog.My.piano.app.external.elasticsearch.ElasticSearchInstance;
import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUser;
import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostListDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Genre;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import com.omegafrog.My.piano.app.web.infra.sheetPost.SimpleJpaSheetPostRepository;
import com.omegafrog.My.piano.app.web.infra.user.SimpleJpaUserRepository;
import com.omegafrog.My.piano.app.web.service.cache.SheetPostCacheCoordinator;
import com.omegafrog.My.piano.app.web.service.cache.SheetPostListCacheKey;
import com.omegafrog.My.piano.app.web.service.cache.SheetPostListCachePayload;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.generate-ddl=true"
})
@ActiveProfiles("test")
@Import({TestUtilConfig.class, JCacheTestConfig.class})
class SheetPostCacheWarmupJobIntegrationTest {

    @Autowired
    private Cleanup cleanup;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("sheetPostCacheWarmupJob")
    private Job sheetPostCacheWarmupJob;

    @Autowired
    private SheetPostCacheCoordinator sheetPostCacheCoordinator;

    @Autowired
    private SimpleJpaUserRepository userRepository;

    @Autowired
    private SimpleJpaSheetPostRepository sheetPostRepository;

    @MockBean
    private ElasticSearchInstance elasticSearchInstance;

    @BeforeEach
    void setUp() {
        cleanup.cleanUp();
        Cache listCache = sheetPostCacheCoordinator.getRequiredCache(
                SheetPostCacheCoordinator.SHEET_POST_LIST_CACHE);
        Cache detailCache = sheetPostCacheCoordinator.getRequiredCache(
                SheetPostCacheCoordinator.SHEET_POST_DETAIL_CACHE);
        assertNotNull(listCache);
        assertNotNull(detailCache);
        listCache.clear();
        detailCache.clear();
    }

    @Test
    @DisplayName("warm-up job populates caches and overwrites when DB changes")
    void warmupJobPopulatesAndOverwritesCaches() throws Exception {
        var user = com.omegafrog.My.piano.app.web.domain.user.User.builder()
                .name("tester")
                .email("tester@example.com")
                .cart(new Cart())
                .loginMethod(LoginMethod.EMAIL)
                .profileSrc("profile")
                .cash(0)
                .build();
        SecurityUser securityUser = SecurityUser.builder()
                .username("tester")
                .password("pw")
                .role(Role.USER)
                .user(user)
                .build();
        user.setSecurityUser(securityUser);
        user = userRepository.save(user);

        var sheet = Sheet.builder()
                .title("sheet")
                .pageNum(1)
                .difficulty(Difficulty.MEDIUM)
                .instrument(Instrument.GUITAR_ACOUSTIC)
                .genres(Genres.builder().genre1(Genre.CLASSIC).genre2(null).build())
                .isSolo(true)
                .lyrics(false)
                .sheetUrl("s")
                .thumbnailUrl("t")
                .user(user)
                .originalFileName("orig.pdf")
                .build();

        SheetPost sheetPost = sheetPostRepository.save(new SheetPost(
                "title-v1",
                "content",
                user,
                sheet,
                1000
        ));
        sheet.setSheetPost(sheetPost);
        sheetPostRepository.flush();

        Mockito.when(elasticSearchInstance.searchSheetPost(
                        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(Pageable.class)))
                .thenAnswer(invocation -> {
                    Pageable pageable = invocation.getArgument(4);
                    Page<Long> ids = new PageImpl<>(List.of(sheetPost.getId()), pageable, 1);
                    return Pair.of(ids, "raw");
                });

        runWarmupJob();

        Cache listCache = sheetPostCacheCoordinator.getRequiredCache(
                SheetPostCacheCoordinator.SHEET_POST_LIST_CACHE);
        Cache detailCache = sheetPostCacheCoordinator.getRequiredCache(
                SheetPostCacheCoordinator.SHEET_POST_DETAIL_CACHE);
        assertNotNull(listCache);
        assertNotNull(detailCache);

        String listKey = SheetPostListCacheKey.of(null, null, null, null, PageRequest.of(0, 20)).asStringKey();
        Cache.ValueWrapper listWrapper = listCache.get(listKey);
        Assertions.assertThat(listWrapper).isNotNull();

        @SuppressWarnings("unchecked")
        SwrCacheValue<SheetPostListCachePayload> listValue = (SwrCacheValue<SheetPostListCachePayload>) listWrapper.get();
        Assertions.assertThat(listValue).isNotNull();
        List<SheetPostListDto> items = listValue.payload().items();
        Assertions.assertThat(items).isNotEmpty();
        Assertions.assertThat(items.get(0).getTitle()).isEqualTo("title-v1");

        Cache.ValueWrapper detailWrapper = detailCache.get(sheetPost.getId());
        Assertions.assertThat(detailWrapper).isNotNull();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(listValue);
            oos.flush();
            Assertions.assertThat(baos.size()).isGreaterThan(0);
        }

        UpdateSheetPostDto updateDto = new UpdateSheetPostDto();
        updateDto.setTitle("title-v2");
        SheetPost latestSheetPost = sheetPostRepository.findById(sheetPost.getId())
                .orElseThrow();
        latestSheetPost.update(updateDto);
        sheetPostRepository.save(latestSheetPost);
        sheetPostRepository.flush();

        runWarmupJob();

        @SuppressWarnings("unchecked")
        SwrCacheValue<SheetPostListCachePayload> updatedListValue =
                (SwrCacheValue<SheetPostListCachePayload>) listCache.get(listKey).get();
        Assertions.assertThat(updatedListValue.payload().items().get(0).getTitle()).isEqualTo("title-v2");
    }

    private void runWarmupJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("requestedAt", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(sheetPostCacheWarmupJob, params);
    }

    @TestConfiguration
    static class JCacheOverrideConfig {

        @Bean(destroyMethod = "close")
        public CachingProvider jCacheProvider() {
            return Caching.getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider");
        }

        @Bean(destroyMethod = "close")
        public javax.cache.CacheManager jCacheManager(CachingProvider cachingProvider) throws Exception {
            URL configUrl = Thread.currentThread().getContextClassLoader().getResource("ehcache.xml");
            if (configUrl == null) {
                throw new IllegalStateException("ehcache.xml must exist on classpath");
            }
            URI configUri = configUrl.toURI();
            return cachingProvider.getCacheManager(configUri, Thread.currentThread().getContextClassLoader());
        }

        @Bean(name = "testCacheManager")
        @Primary
        public CacheManager testCacheManager(javax.cache.CacheManager jCacheManager) {
            return new JCacheCacheManager(jCacheManager);
        }
    }
}
