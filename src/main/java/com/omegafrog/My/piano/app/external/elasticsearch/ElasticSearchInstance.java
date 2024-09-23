package com.omegafrog.My.piano.app.external.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.ObjectBuilder;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.dto.dateRange.DateRange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
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

    private final String SHEET_POST_INDEX_NAME = "sheetpost";


    @Async("ThreadPoolTaskExecutor")
    public void invertIndexingSheetPost(SheetPost sheetPost) {
        sheetPostIndexRepository.save(SheetPostIndex.of(sheetPost));

    }

    public Page<Long> searchSheetPost(@Nullable String searchSentence,
                                      @Nullable List<String> instruments,
                                      @Nullable List<String> difficulties,
                                      @Nullable List<String> genres,
                                      Pageable pageable) throws IOException {
        List<Query> searchOptions = new ArrayList<>();
        if (instruments != null && !instruments.isEmpty()) {
            Query instrumentFilter = getQuery("instrument.keyword", instruments);
            searchOptions.add(instrumentFilter);
        }
        if (difficulties != null && !difficulties.isEmpty()) {
            Query difficultyFilter = getQuery("difficulty.keyword", difficulties);
            searchOptions.add(difficultyFilter);
        }
        if (genres != null && !genres.isEmpty()) {
            Query genreFilter = getQuery("genre.keyword", genres);
            searchOptions.add(genreFilter);
        }
        SearchResponse<SheetPostIndex> response = esClient.search(
                s -> getRequest(searchSentence, s, searchOptions, pageable), SheetPostIndex.class);


        long count = esClient.count(builder ->
                        builder.index(SHEET_POST_INDEX_NAME)
                                .query(q -> getSearchTermQueryBuilder(searchSentence, searchOptions, q)))
                .count();

        List<Hit<SheetPostIndex>> hits = response.hits().hits();
        List<Long> sheetPostIds = new ArrayList<>();
        hits.forEach(hit -> sheetPostIds.add(hit.source().getId()));
        return PageableExecutionUtils.getPage(sheetPostIds, pageable, () -> count);
    }

    private static Query getQuery(String value, List<String> instruments) {
        return QueryBuilders.terms(t -> t
                .field(value)
                .terms(terms -> terms.value(instruments.stream().map(item -> FieldValue.of(item))
                        .filter(item -> !item.stringValue().isBlank()).toList())));
    }

    private SearchRequest.Builder getRequest(@Nullable String searchSentence, SearchRequest.Builder s,
                                             List<Query> searchOptions, Pageable pageable) {
        return s.index(SHEET_POST_INDEX_NAME)
                .from((pageable.getPageNumber()) * pageable.getPageSize())
                .size(pageable.getPageSize())
                .sort(SortOptions.of(so -> so.field(FieldSort.of(f -> f.field("created_at")
                        .order(SortOrder.Desc)))))
                .query(q -> getSearchTermQueryBuilder(searchSentence, searchOptions, q));
    }

    private static ObjectBuilder<Query> getSearchTermQueryBuilder(@Nullable String searchSentence, List<Query> searchOptions, Query.Builder q) {
        if (searchSentence != null && !searchSentence.isEmpty()) {
            return q.bool(b -> b
                    .must(q2 -> q2.queryString(qs -> qs.fields("title", "content").query("*" + searchSentence + "*")))
                    .must(searchOptions));
        } else {
            return q.bool(b -> b.must(searchOptions));
        }
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

    public void updateSheetPostIndex() {

    }
}
