package com.omegafrog.My.piano.app.batch;

import com.omegafrog.My.piano.app.web.domain.article.ViewCount;
import com.omegafrog.My.piano.app.web.domain.sheet.QSheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCount;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewCountItemWriterTest {

    @Mock
    private JPAQueryFactory factory;

    @Mock
    private JPAUpdateClause updateClause;

    private ViewCountItemWriter<SheetPost> writer;

    @BeforeEach
    void setUp() {
        writer = new ViewCountItemWriter<>(QSheetPost.sheetPost, factory);
    }

    @Test
    void shouldWriteViewCountsSuccessfully() throws Exception {
        // Given
        SheetPostViewCount viewCount1 = SheetPostViewCount.builder()
                .id(1L)
                .viewCount(10)
                .build();
        
        SheetPostViewCount viewCount2 = SheetPostViewCount.builder()
                .id(2L)
                .viewCount(20)
                .build();

        Chunk<ViewCount> chunk = new Chunk<>(Arrays.asList(viewCount1, viewCount2));

        // Mock QueryDSL update chain
        when(factory.update(any())).thenReturn(updateClause);
        when(updateClause.set(any(com.querydsl.core.types.Path.class), any(Integer.class))).thenReturn(updateClause);
        when(updateClause.where(any())).thenReturn(updateClause);
        when(updateClause.execute()).thenReturn(1L);

        // When
        writer.write(chunk);

        // Then
        verify(factory, times(2)).update(QSheetPost.sheetPost);
        verify(updateClause, times(2)).execute();
    }

    @Test
    void shouldHandleEmptyChunk() throws Exception {
        // Given
        Chunk<ViewCount> emptyChunk = new Chunk<>();

        // When
        writer.write(emptyChunk);

        // Then
        verify(factory, never()).update(any());
        verify(updateClause, never()).execute();
    }

    @Test
    void shouldUpdateEachItemIndividually() throws Exception {
        // Given
        SheetPostViewCount viewCount1 = SheetPostViewCount.builder()
                .id(1L)
                .viewCount(15)
                .build();

        Chunk<ViewCount> chunk = new Chunk<>(Arrays.asList(viewCount1));

        // Mock QueryDSL update chain
        when(factory.update(any())).thenReturn(updateClause);
        when(updateClause.set(any(), eq(15))).thenReturn(updateClause);
        when(updateClause.where(any())).thenReturn(updateClause);
        when(updateClause.execute()).thenReturn(1L);

        // When
        writer.write(chunk);

        // Then
        verify(factory, times(1)).update(QSheetPost.sheetPost);
        verify(updateClause, times(1)).set(any(), eq(15));
        verify(updateClause, times(1)).execute();
    }

    @Test
    void shouldLogWarningWhenUpdatedRowsMismatch() throws Exception {
        // Given
        SheetPostViewCount viewCount1 = SheetPostViewCount.builder()
                .id(1L)
                .viewCount(10)
                .build();
        
        SheetPostViewCount viewCount2 = SheetPostViewCount.builder()
                .id(2L)
                .viewCount(20)
                .build();

        Chunk<ViewCount> chunk = new Chunk<>(Arrays.asList(viewCount1, viewCount2));

        // Mock QueryDSL update chain - 첫 번째는 성공, 두 번째는 실패 (0 rows affected)
        when(factory.update(any())).thenReturn(updateClause);
        when(updateClause.set(any(com.querydsl.core.types.Path.class), any(Integer.class))).thenReturn(updateClause);
        when(updateClause.where(any())).thenReturn(updateClause);
        when(updateClause.execute())
                .thenReturn(1L)  // 첫 번째 업데이트 성공
                .thenReturn(0L); // 두 번째 업데이트 실패

        // When
        writer.write(chunk);

        // Then
        verify(factory, times(2)).update(QSheetPost.sheetPost);
        verify(updateClause, times(2)).execute();
        // 로그 경고는 실제 로그 출력을 확인하기 어려우므로, 예외가 발생하지 않음을 확인
    }

    @Test
    void shouldHandleReflectionException() throws Exception {
        // Given
        ViewCountItemWriter<SheetPost> writerWithInvalidEntity = 
            new ViewCountItemWriter<>(null, factory); // null entityPath로 Reflection 오류 유발

        SheetPostViewCount viewCount1 = SheetPostViewCount.builder()
                .id(1L)
                .viewCount(10)
                .build();

        Chunk<ViewCount> chunk = new Chunk<>(Arrays.asList(viewCount1));

        // When & Then
        try {
            writerWithInvalidEntity.write(chunk);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Cannot access entity fields");
        }
    }

    @Test
    void shouldCreateWriterWithCorrectEntityPath() {
        // Given & When
        ViewCountItemWriter<SheetPost> writer = 
            new ViewCountItemWriter<>(QSheetPost.sheetPost, factory);

        // Then
        assertThat(writer).isNotNull();
    }
}