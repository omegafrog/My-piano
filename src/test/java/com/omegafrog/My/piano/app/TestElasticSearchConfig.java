package com.omegafrog.My.piano.app;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.omegafrog.My.piano.app.external.elasticsearch.ElasticSearchInstance;
import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostIndexRepository;
import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostSearchIndexRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

@Configuration
@Profile("test")
public class TestElasticSearchConfig {

    @Bean
    @Primary
    public ElasticsearchClient elasticsearchClient() {
        return mock(ElasticsearchClient.class);
    }

    @Bean
    @Primary
    public SheetPostIndexRepository sheetPostIndexRepository() {
        return mock(SheetPostIndexRepository.class);
    }

    @Bean
    @Primary
    public SheetPostSearchIndexRepository sheetPostSearchIndexRepository() {
        return mock(SheetPostSearchIndexRepository.class);
    }

    @Bean
    @Primary
    public ElasticSearchInstance elasticSearchInstance(SheetPostRepository sheetPostRepository) {
        ElasticSearchInstance instance = Mockito.mock(ElasticSearchInstance.class);

        when(instance.searchSheetPost(
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(Pageable.class)))
                .thenAnswer(invocation -> {
                    Pageable pageable = invocation.getArgument(4);
                    Page<SheetPost> page = sheetPostRepository.findAll(pageable);
                    Page<Long> ids = page.map(SheetPost::getId);
                    Object searchSentence = invocation.getArgument(0);
                    String rawQuery = String.valueOf(searchSentence);
                    return Pair.of(ids, rawQuery);
                });

        when(instance.getSearchSheetPostAutoComplete(
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any()))
                .thenReturn(java.util.List.of());

        return instance;
    }
}
