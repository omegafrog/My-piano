package com.omegafrog.My.piano.app.external.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.dto.dateRange.DateRange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Slf4j
public class ElasticSearchInstance {

    @Autowired
    private ElasticsearchClient esClient;
    @Autowired
    private SheetPostRepository sheetPostRepository;
    @Autowired
    private SheetPostIndexRepository sheetPostIndexRepository;

    private final String SHEET_POST_INDEX_NAME="sheetpost";


    @Async("ThreadPoolTaskExecutor")
    public void invertIndexingSheetPost(SheetPost sheetPost) {
        sheetPostIndexRepository.save(SheetPostIndex.of(sheetPost));

    }

    public List<Long> searchSheetPost(@Nullable String searchSentence,
                                      @Nullable List<String> instruments,
                                      @Nullable List<String> difficulties,
                                      @Nullable List<String> genres,
                                      Pageable pageable) throws IOException {
        List<Query> searchOptions = new ArrayList<>();
        if (instruments!=null && !instruments.isEmpty()) {
            Query instrumentFilter = QueryBuilders.terms(t -> t
                    .field("instrument.keyword")
                    .terms(terms -> terms.value(instruments.stream().map(item -> FieldValue.of(item))
                            .filter(item ->!item.stringValue().isBlank()).toList())));
            searchOptions.add(instrumentFilter);
        }
        if (difficulties!=null && !difficulties.isEmpty()) {
            Query difficultyFilter = QueryBuilders.terms(t -> t
                    .field("difficulty.keyword")
                    .terms(terms -> terms.value(difficulties.stream().map(item -> FieldValue.of(item))
                            .filter(item ->!item.stringValue().isBlank()).toList())));
            searchOptions.add(difficultyFilter);
        }
        if (genres!=null && !genres.isEmpty()) {
            Query genreFilter= QueryBuilders.terms(t -> t
                    .field("genre.keyword")
                    .terms(terms -> terms.value(genres.stream().map(item -> FieldValue.of(item))
                            .filter(item ->!item.stringValue().isBlank()).toList())));
            searchOptions.add(genreFilter);
        }
        SearchResponse<SheetPostIndex> response = esClient.search(
                s -> s.index(SHEET_POST_INDEX_NAME)
                        .from((pageable.getPageNumber()) * pageable.getPageSize())
                        .size(pageable.getPageSize())
                        .sort(SortOptions.of(so -> so.field(FieldSort.of(f -> f.field("created_at")
                                .order(SortOrder.Desc)))))
                        .query(q->
                        {
                            if (searchSentence != null && !searchSentence.isEmpty()) {
                                return q.bool(b -> b
                                        .must(q2 -> q2.queryString(qs -> qs.fields("title", "content").query("*" + searchSentence + "*")))
                                        .must(searchOptions));
                            } else {
                                return q.bool(b -> b.must(searchOptions));
                            }
                        }
                        ),SheetPostIndex.class);

        List<Hit<SheetPostIndex>> hits = response.hits().hits();
        List<Long> sheetPostIds = new ArrayList<>();
        for (Hit<SheetPostIndex> hit : hits) {
            sheetPostIds.add(Long.valueOf(hit.id()));
        }
        return sheetPostIds;
    }

    public List<SheetPostIndex> searchPopularDateRangeSheetPost(DateRange dateRange, String limit) throws IOException, TimeoutException {
        List<Query> searchOptions = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        Query dateRangeQuery = QueryBuilders.range(r -> r
                .field("created_at")
                .gte(JsonData.of(dateRange.getStart().atStartOfDay().atOffset(ZoneOffset.UTC).format(formatter)))
                .lt(JsonData.of(dateRange.getEnd().atStartOfDay().atOffset(ZoneOffset.UTC).format(formatter))));
        searchOptions.add(dateRangeQuery);
        SortOptions sortOptions = SortOptionsBuilders
                .field(f -> f
                        .field("viewCount")
                        .order(SortOrder.Desc));

        SearchResponse<SheetPostIndex> response = esClient.search(fn -> fn
                        .index(SHEET_POST_INDEX_NAME)
                        .query(q -> q
                                .bool(v -> v
                                        .must(searchOptions)))
                        .sort(sortOptions)
                        .size(Integer.valueOf(limit))
                , SheetPostIndex.class);

        if (response.timedOut())
            throw new TimeoutException("Elasticsearch response is time out.");

        log.info("{} hits in {} seconds. ", response.hits().hits().size(), response.took());
        log.info("searchOptions : {}", searchOptions);
        log.info("sortOptions : {}", sortOptions.toString());

        List<Hit<SheetPostIndex>> hits = response.hits().hits();
        return hits.stream().map(Hit::source).toList();
    }
}
