package com.omegafrog.My.piano.app.batch;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
class ViewCountPagingItemReaderTest {

    @Mock
    private CacheManager cacheManager;

    private ViewCountPagingItemReader<SheetPostViewCount> reader;

    @BeforeEach
    void setUp() {
        reader = new ViewCountPagingItemReader<SheetPostViewCount>(SheetPostViewCount.class, cacheManager);
        reader.setPageSize(10);
    }

    @Test
    void shouldCreateReaderWithCorrectType() {
        // Given & When
        ViewCountPagingItemReader<SheetPostViewCount> reader = 
            new ViewCountPagingItemReader<SheetPostViewCount>(SheetPostViewCount.class, cacheManager);

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
    void shouldAllowNullCacheManagerAtConstruction() {
        // Given & When & Then
        assertThatCode(() -> new ViewCountPagingItemReader<SheetPostViewCount>(SheetPostViewCount.class, null))
                .doesNotThrowAnyException();
    }
}
