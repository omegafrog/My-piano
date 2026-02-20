package com.omegafrog.My.piano.app.web.service.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.omegafrog.My.piano.app.external.elasticsearch.ElasticSearchInstance;
import com.omegafrog.My.piano.app.web.domain.outbox.PostIndexVersion;
import com.omegafrog.My.piano.app.web.domain.outbox.PostIndexVersionRepository;
import com.omegafrog.My.piano.app.web.domain.outbox.PostOutboxEvent;
import com.omegafrog.My.piano.app.web.domain.outbox.PostOutboxEventRepository;
import com.omegafrog.My.piano.app.web.domain.outbox.PostOutboxEventStatus;
import com.omegafrog.My.piano.app.web.domain.outbox.PostOutboxEventType;
import com.omegafrog.My.piano.app.web.domain.outbox.ProcessedPostEventRepository;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;

@ExtendWith(MockitoExtension.class)
class PostOutboxProcessorTest {

    @Mock
    private PostOutboxEventRepository outboxRepository;
    @Mock
    private ProcessedPostEventRepository processedPostEventRepository;
    @Mock
    private PostIndexVersionRepository postIndexVersionRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private ElasticSearchInstance elasticSearchInstance;

    private PostOutboxProcessor processor;

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-02-12T00:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        processor = new PostOutboxProcessor(
                outboxRepository,
                processedPostEventRepository,
                postIndexVersionRepository,
                postRepository,
                elasticSearchInstance,
                fixedClock,
                100,
                Duration.ofMinutes(1));
    }

    @Test
    @DisplayName("이미 처리된 eventId는 중복 처리하지 않고 완료 상태로 마킹한다")
    void skipAlreadyProcessedEvent() {
        PostOutboxEvent event = PostOutboxEvent.pending("evt-1", 1L, 1L, PostOutboxEventType.POST_CREATED);
        when(outboxRepository.findProcessable(any(LocalDateTime.class), anyInt())).thenReturn(List.of(event));
        when(processedPostEventRepository.existsByEventId("evt-1")).thenReturn(true);

        processor.processPendingEvents();

        assertThat(event.getStatus()).isEqualTo(PostOutboxEventStatus.COMPLETED);
        verifyNoInteractions(postRepository, postIndexVersionRepository, elasticSearchInstance);
    }

    @Test
    @DisplayName("더 낮은 버전 이벤트는 stale로 판단해 건너뛴다")
    void skipStaleVersionEvent() {
        PostOutboxEvent event = PostOutboxEvent.pending("evt-2", 7L, 2L, PostOutboxEventType.POST_UPDATED);
        when(outboxRepository.findProcessable(any(LocalDateTime.class), anyInt())).thenReturn(List.of(event));
        when(processedPostEventRepository.existsByEventId("evt-2")).thenReturn(false);
        when(postIndexVersionRepository.findByPostId(7L))
                .thenReturn(Optional.of(new PostIndexVersion(7L, 3L, "evt-prev", LocalDateTime.now(fixedClock))));

        processor.processPendingEvents();

        assertThat(event.getStatus()).isEqualTo(PostOutboxEventStatus.COMPLETED);
        verifyNoInteractions(postRepository, elasticSearchInstance);
        verify(processedPostEventRepository).save(any());
        verify(postIndexVersionRepository, never()).save(any());
    }

    @Test
    @DisplayName("신규 생성 이벤트는 Elasticsearch 반영 후 버전/처리이력을 갱신한다")
    void processCreateEventSuccessfully() throws Exception {
        PostOutboxEvent event = PostOutboxEvent.pending("evt-3", 10L, 0L, PostOutboxEventType.POST_CREATED);
        Post post = org.mockito.Mockito.mock(Post.class);

        when(outboxRepository.findProcessable(any(LocalDateTime.class), anyInt())).thenReturn(List.of(event));
        when(processedPostEventRepository.existsByEventId("evt-3")).thenReturn(false);
        when(postIndexVersionRepository.findByPostId(10L)).thenReturn(Optional.empty());
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        processor.processPendingEvents();

        assertThat(event.getStatus()).isEqualTo(PostOutboxEventStatus.COMPLETED);
        verify(elasticSearchInstance).savePostIndex(post);
        verify(processedPostEventRepository).save(any());
        verify(postIndexVersionRepository).save(any(PostIndexVersion.class));
    }

    @Test
    @DisplayName("처리 중 예외가 발생하면 실패 상태로 전환하고 재시도 시간을 설정한다")
    void markFailedWhenProcessingThrows() throws Exception {
        PostOutboxEvent event = PostOutboxEvent.pending("evt-4", 20L, 5L, PostOutboxEventType.POST_UPDATED);
        Post post = org.mockito.Mockito.mock(Post.class);

        when(outboxRepository.findProcessable(any(LocalDateTime.class), anyInt())).thenReturn(List.of(event));
        when(processedPostEventRepository.existsByEventId("evt-4")).thenReturn(false);
        when(postIndexVersionRepository.findByPostId(20L)).thenReturn(Optional.empty());
        when(postRepository.findById(20L)).thenReturn(Optional.of(post));
        doThrow(new RuntimeException("es timeout")).when(elasticSearchInstance).updatePostIndex(post);

        processor.processPendingEvents();

        assertThat(event.getStatus()).isEqualTo(PostOutboxEventStatus.FAILED);
        assertThat(event.getRetryCount()).isEqualTo(1);
        assertThat(event.getNextAttemptAt()).isEqualTo(LocalDateTime.ofInstant(fixedClock.instant(), ZoneOffset.UTC).plusMinutes(1));
        verify(processedPostEventRepository, never()).save(any());
        verify(postIndexVersionRepository, never()).save(any());
    }
}
