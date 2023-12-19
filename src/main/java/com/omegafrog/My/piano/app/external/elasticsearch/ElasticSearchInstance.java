package com.omegafrog.My.piano.app.external.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ElasticSearchInstance {

    @Autowired
    private ElasticsearchClient esClient;
    @Autowired
    private SheetPostRepository sheetPostRepository;

    private final Integer MAX_PAGE_SIZE = 20;


    @Async("ThreadPoolTaskExecutor")
    public void invertIndexingSheetPost(SheetPost sheetPost) throws IOException {
//        curl -XPUT "https://172.18.0.2:9200/sheets/_doc/1" -H "kbn-xsrf: reporting" -H "Content-Type: application/json" -d'
//        {
//            "name":"동해물과 백두산이"
//        }'
        esClient.index(i -> i
                .index("sheets")
                .id(sheetPost.getId().toString())
                .document(
                        SheetPostIndex.of(sheetPost)
                ));
    }

    public List<Long> searchingSheetPost(Integer page, List<String> instruments, List<String> difficulties, List<String> genres) throws IOException {
        List<Query> searchOptions = new ArrayList<>();
        if(!instruments.isEmpty()){
            Query instrumentFilter = QueryBuilders.terms(t -> t
                    .field("instrument")
                    .terms(terms -> terms.value(instruments.stream().map(item -> FieldValue.of(item)).toList())));
            searchOptions.add(instrumentFilter);
        }
        if(!difficulties.isEmpty()){
            Query instrumentFilter = QueryBuilders.terms(t -> t
                    .field("difficulty")
                    .terms(terms -> terms.value(difficulties.stream().map(item -> FieldValue.of(item)).toList())));
            searchOptions.add(instrumentFilter);
        }
        if(!genres.isEmpty()){
            Query instrumentFilter = QueryBuilders.terms(t -> t
                    .field("genre")
                    .terms(terms -> terms.value(genres.stream().map(item -> FieldValue.of(item)).toList())));
            searchOptions.add(instrumentFilter);
        }
        SearchResponse<SheetPostIndex> response = esClient.search(
                s -> s.index("sheets")
                        .from(page*MAX_PAGE_SIZE)
                        .size(MAX_PAGE_SIZE)
                        .query(q -> q
                                .bool(b -> b.
                                        must(searchOptions))), SheetPostIndex.class
        );
        List<Hit<SheetPostIndex>> hits = response.hits().hits();
        List<Long> sheetPostIds = new ArrayList<>();
        for(Hit<SheetPostIndex> hit: hits){
            sheetPostIds.add(Long.valueOf(hit.id()));
        }
        return sheetPostIds;
    }
}
