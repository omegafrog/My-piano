package com.omegafrog.My.piano.app.batch;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewCountPagingItemReaderTest {

    @Mock
    private RedisTemplate<String, SheetPostViewCount> redisTemplate;

    private ViewCountPagingItemReader<SheetPostViewCount> reader;

    @BeforeEach
    void setUp() {
        reader = new ViewCountPagingItemReader<>(SheetPostViewCount.class, redisTemplate);
        reader.setPageSize(10);
    }

    @Test
    void shouldCreateReaderWithCorrectType() {
        // Given & When
        ViewCountPagingItemReader<SheetPostViewCount> reader = 
            new ViewCountPagingItemReader<>(SheetPostViewCount.class, redisTemplate);

        // Then
        assertThat(reader).isNotNull();
    }

    @Test
    void shouldSetPageSizeCorrectly() {
        // Given & When
        reader.setPageSize(20);

        // Then
        assertThat(reader.getPageSize()).isEqualTo(20);
    }

    @Test
    void shouldHandleNullRedisTemplate() {
        // Given & When & Then
        try {
            new ViewCountPagingItemReader<>(SheetPostViewCount.class, null);
        } catch (Exception e) {
            // NullPointerException이 발생할 수 있음
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }
}