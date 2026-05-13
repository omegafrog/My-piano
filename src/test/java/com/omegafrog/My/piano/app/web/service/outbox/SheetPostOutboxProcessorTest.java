package com.omegafrog.My.piano.app.web.service.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import com.omegafrog.My.piano.app.web.domain.outbox.SheetPostOutboxEvent;
import com.omegafrog.My.piano.app.web.domain.outbox.SheetPostOutboxEventRepository;
import com.omegafrog.My.piano.app.web.domain.outbox.SheetPostOutboxEventStatus;
import com.omegafrog.My.piano.app.web.domain.outbox.SheetPostOutboxEventType;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;

@ExtendWith(MockitoExtension.class)
class SheetPostOutboxProcessorTest {

    @Mock
    private SheetPostOutboxEventRepository outboxRepository;
    @Mock
    private SheetPostRepository sheetPostRepository;
    @Mock
    private ElasticSearchInstance elasticSearchInstance;

    private SheetPostOutboxProcessor processor;

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-02-12T00:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        processor = new SheetPostOutboxProcessor(
                outboxRepository,
                sheetPostRepository,
                elasticSearchInstance,
                fixedClock,
                100,
                Duration.ofMinutes(1));
    }

    @Test
    @DisplayName("생성 이벤트는 Elasticsearch 저장 성공 후에만 완료 처리한다")
    void completeCreatedEventAfterSuccessfulIndexing() {
        SheetPostOutboxEvent event = SheetPostOutboxEvent.pending("evt-1", 10L, SheetPostOutboxEventType.SHEET_POST_CREATED);
        SheetPost sheetPost = org.mockito.Mockito.mock(SheetPost.class);

        when(outboxRepository.findProcessable(any(LocalDateTime.class), anyInt())).thenReturn(List.of(event));
        when(sheetPostRepository.findById(10L)).thenReturn(Optional.of(sheetPost));

        processor.processPendingEvents();

        assertThat(event.getStatus()).isEqualTo(SheetPostOutboxEventStatus.COMPLETED);
        verify(elasticSearchInstance).saveSheetPostIndex(sheetPost);
    }

    @Test
    @DisplayName("수정 이벤트도 Elasticsearch upsert 성공 후 완료 처리한다")
    void completeUpdatedEventAfterSuccessfulIndexing() {
        SheetPostOutboxEvent event = SheetPostOutboxEvent.pending("evt-2", 11L, SheetPostOutboxEventType.SHEET_POST_UPDATED);
        SheetPost sheetPost = org.mockito.Mockito.mock(SheetPost.class);

        when(outboxRepository.findProcessable(any(LocalDateTime.class), anyInt())).thenReturn(List.of(event));
        when(sheetPostRepository.findById(11L)).thenReturn(Optional.of(sheetPost));

        processor.processPendingEvents();

        assertThat(event.getStatus()).isEqualTo(SheetPostOutboxEventStatus.COMPLETED);
        verify(elasticSearchInstance).saveSheetPostIndex(sheetPost);
    }

    @Test
    @DisplayName("삭제 이벤트는 엔티티 재조회 없이 Elasticsearch delete 성공 후 완료 처리한다")
    void completeDeletedEventAfterSuccessfulDelete() {
        SheetPostOutboxEvent event = SheetPostOutboxEvent.pending("evt-3", 12L, SheetPostOutboxEventType.SHEET_POST_DELETED);

        when(outboxRepository.findProcessable(any(LocalDateTime.class), anyInt())).thenReturn(List.of(event));

        processor.processPendingEvents();

        assertThat(event.getStatus()).isEqualTo(SheetPostOutboxEventStatus.COMPLETED);
        verify(elasticSearchInstance).deleteSheetPostIndex(12L);
        verify(sheetPostRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Elasticsearch 저장 실패 시 completed 처리하지 않고 재시도 가능 상태로 남긴다")
    void markFailedWhenIndexingThrows() {
        SheetPostOutboxEvent event = SheetPostOutboxEvent.pending("evt-4", 20L, SheetPostOutboxEventType.SHEET_POST_CREATED);
        SheetPost sheetPost = org.mockito.Mockito.mock(SheetPost.class);

        when(outboxRepository.findProcessable(any(LocalDateTime.class), anyInt())).thenReturn(List.of(event));
        when(sheetPostRepository.findById(20L)).thenReturn(Optional.of(sheetPost));
        doThrow(new RuntimeException("es timeout")).when(elasticSearchInstance).saveSheetPostIndex(sheetPost);

        processor.processPendingEvents();

        assertThat(event.getStatus()).isEqualTo(SheetPostOutboxEventStatus.FAILED);
        assertThat(event.getRetryCount()).isEqualTo(1);
        assertThat(event.getProcessedAt()).isNull();
        assertThat(event.getNextAttemptAt())
                .isEqualTo(LocalDateTime.ofInstant(fixedClock.instant(), ZoneOffset.UTC).plusMinutes(1));
        verify(elasticSearchInstance).saveSheetPostIndex(sheetPost);
        verify(outboxRepository, never()).save(any());
    }
}
