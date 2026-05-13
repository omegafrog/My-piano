package com.omegafrog.My.piano.app.web.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import com.omegafrog.My.piano.app.external.elasticsearch.ElasticSearchInstance;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.FileStorageExecutor;
import com.omegafrog.My.piano.app.web.domain.article.LikeCountRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCountRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.sheetPost.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.web.service.cache.SheetPostCacheCoordinator;
import com.omegafrog.My.piano.app.web.service.outbox.SheetPostOutboxService;

@ExtendWith(MockitoExtension.class)
class SheetPostApplicationServiceOutboxTest {

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
    private SheetPostCacheCoordinator sheetPostCacheCoordinator;
    @Mock
    private LikeCountRepository likeCountRepository;

    private SheetPostApplicationService service;

    @BeforeEach
    void setUp() {
        service = new SheetPostApplicationService(
                sheetPostRepository,
                userRepository,
                elasticSearchInstance,
                fileStorageExecutor,
                sheetPostViewCountRepository,
                authenticationUtil,
                fileUploadService,
                eventPublisher,
                sheetPostOutboxService,
                sheetPostCacheCoordinator);
        ReflectionTestUtils.setField(service, "likeCountRepository", likeCountRepository);
    }

    @Test
    @DisplayName("SheetPost 수정은 직접 ES를 건드리지 않고 outbox 업데이트 이벤트를 적재한다")
    void updateEnqueuesOutboxInsteadOfDirectElasticMutation() throws IOException {
        Long sheetPostId = 10L;
        User loggedInUser = mock(User.class);
        SheetPost sheetPost = mock(SheetPost.class);
        UpdateSheetPostDto dto = mock(UpdateSheetPostDto.class);

        when(authenticationUtil.getLoggedInUser()).thenReturn(loggedInUser);
        when(sheetPostRepository.findById(sheetPostId)).thenReturn(Optional.of(sheetPost));
        when(sheetPost.getAuthor()).thenReturn(loggedInUser);
        when(sheetPost.getId()).thenReturn(sheetPostId);
        when(dto.getUploadId()).thenReturn(null);
        when(sheetPost.update(dto)).thenReturn(sheetPost);

        service.update(sheetPostId, dto);

        verify(sheetPostOutboxService).enqueueUpdated(sheetPostId);
        verify(elasticSearchInstance, never()).invertIndexingSheetPost(sheetPost);
        verify(sheetPostCacheCoordinator).evictSheetPostListCache();
        verify(sheetPostCacheCoordinator).evictSheetPostDetailCache(sheetPostId);
    }

    @Test
    @DisplayName("SheetPost 삭제는 권한 확인 후 DB 삭제와 outbox 삭제 이벤트 적재를 수행한다")
    void deleteChecksAuthorizationBeforeDeleteAndEnqueuesOutbox() {
        Long sheetPostId = 20L;
        User loggedInUser = mock(User.class);
        SheetPost sheetPost = mock(SheetPost.class);

        when(authenticationUtil.getLoggedInUser()).thenReturn(loggedInUser);
        when(sheetPostRepository.findById(sheetPostId)).thenReturn(Optional.of(sheetPost));
        when(sheetPost.getAuthor()).thenReturn(loggedInUser);
        when(sheetPost.getId()).thenReturn(sheetPostId);

        service.delete(sheetPostId);

        verify(fileStorageExecutor).removeSheetPost(sheetPost);
        verify(sheetPostRepository).deleteById(sheetPostId);
        verify(sheetPostOutboxService).enqueueDeleted(sheetPostId);
        verify(elasticSearchInstance, never()).deleteSheetPostIndex(sheetPostId);
    }

    @Test
    @DisplayName("권한 없는 삭제 요청은 파일 삭제와 outbox 적재를 수행하지 않는다")
    void deleteRejectsUnauthorizedRequestBeforeSideEffects() {
        Long sheetPostId = 30L;
        User loggedInUser = mock(User.class);
        User author = mock(User.class);
        SheetPost sheetPost = mock(SheetPost.class);

        when(authenticationUtil.getLoggedInUser()).thenReturn(loggedInUser);
        when(sheetPostRepository.findById(sheetPostId)).thenReturn(Optional.of(sheetPost));
        when(sheetPost.getAuthor()).thenReturn(author);

        org.junit.jupiter.api.Assertions.assertThrows(AccessDeniedException.class, () -> service.delete(sheetPostId));

        verify(fileStorageExecutor, never()).removeSheetPost(sheetPost);
        verify(sheetPostRepository, never()).deleteById(sheetPostId);
        verify(sheetPostOutboxService, never()).enqueueDeleted(sheetPostId);
    }
}
