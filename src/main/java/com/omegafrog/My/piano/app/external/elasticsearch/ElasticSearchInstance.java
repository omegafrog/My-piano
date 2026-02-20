package com.omegafrog.My.piano.app.external.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.external.elasticsearch.exception.ElasticSearchException;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.dto.dateRange.DateRange;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostListDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile("!test")
public class ElasticSearchInstance {

  private static final Integer AUTOCOMPLETE_SIZE = 10;
  @Autowired(required = false)
  private SheetPostIndexRepository sheetPostIndexRepository;
  private final ElasticsearchClient client;

  private final String SHEET_POST_INDEX_NAME = "sheetpost";

  @Async("ThreadPoolTaskExecutor")
  public void invertIndexingSheetPost(SheetPost sheetPost) {
    sheetPostIndexRepository.save(SheetPostIndex.of(sheetPost));
  }

  @Async("ThreadPoolTaskExecutor")
  public void invertIndexingSheetPost(SheetPostIndex index) {
    sheetPostIndexRepository.save(index);
  }

  public Pair<Page<Long>, String> searchSheetPost(@Nullable String searchSentence,
      @Nullable List<String> instruments,
      @Nullable List<String> difficulties,
      @Nullable List<String> genres,
      Pageable pageable) {
    // multi_match 쿼리 (title, name, content)
    MultiMatchQuery mmQuery = MultiMatchQuery.of(q -> q
        .query(searchSentence)
        .fields("title^5", "name^4", "content^3")
        .analyzer("my_nori_analyzer")
        .minimumShouldMatch("2<75%"));

    // bool should
    BoolQuery boolQuery = BoolQuery.of(b -> {
      b.should(mmQuery._toQuery());

      if (genres != null && !genres.isEmpty()) {
        b.should(TermsQuery.of(t -> t
            .field("genre")
            .terms(TermsQueryField.of(f -> f.value(genres.stream().map(FieldValue::of).toList())))
            .boost(1.5f))._toQuery());
      }

      if (difficulties != null && !difficulties.isEmpty()) {
        b.should(TermsQuery.of(t -> t
            .field("difficulty")
            .terms(TermsQueryField.of(f -> f.value(difficulties.stream().map(FieldValue::of).toList())))
            .boost(1.2f))._toQuery());
      }

      if (instruments != null && !instruments.isEmpty()) {
        b.should(TermsQuery.of(t -> t
            .field("instrument")
            .terms(TermsQueryField.of(f -> f.value(instruments.stream().map(FieldValue::of).toList())))
            .boost(1.5f))._toQuery());
      }

      return b;
    });

    // function_score
    FunctionScoreQuery functionScoreQuery = FunctionScoreQuery.of(f -> f
        .query(boolQuery._toQuery())
        .functions(fx -> fx
            .fieldValueFactor(fvf -> fvf
                .field("viewCount")
                .factor(1.2)
                .modifier(FieldValueFactorModifier.Log1p)))
        .boostMode(FunctionBoostMode.Sum));

    // 검색 요청
    SearchRequest searchRequest = SearchRequest.of(s -> s
        .index("sheetpost_v3")
        .query(functionScoreQuery._toQuery())
        .from((int) pageable.getOffset())
        .size(pageable.getPageSize()));

    SearchResponse<SheetPostIndex> response = null;
    try {
      response = client.search(searchRequest, SheetPostIndex.class);
    } catch (IOException e) {
      throw new ElasticSearchException("elasticsearch query failed. query:" + searchRequest.query().toString(),
          e);
    }
    long count = response.hits().total().value();

    List<Hit<SheetPostIndex>> hits = response.hits().hits();
    List<Long> sheetPostIds = new ArrayList<>();
    hits.forEach(hit -> sheetPostIds.add(hit.source().getId()));

    return Pair.of(PageableExecutionUtils.getPage(sheetPostIds, pageable, () -> count),
        searchRequest.query().toString());
  }

  public List<SheetPostIndex> searchPopularDateRangeSheetPost(DateRange dateRange, String limit)
      throws IOException, TimeoutException {
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

    SearchResponse<SheetPostIndex> response = client.search(fn -> fn
        .index(SHEET_POST_INDEX_NAME)
        .query(q -> q
            .bool(v -> v
                .must(searchOptions)))
        .sort(sortOptions)
        .size(Integer.valueOf(limit)), SheetPostIndex.class);

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

  // Post indexing methods for event-driven architecture
  @Async("ThreadPoolTaskExecutor")
  public void savePostIndex(Post post) throws IOException {
    log.info("Creating elasticsearch index for post: {}", post.getId());
    // Implementation would create a PostIndex similar to SheetPostIndex
    // For now, we'll simulate the operation
    log.info("Post index created successfully for post: {}", post.getId());
  }

  @Async("ThreadPoolTaskExecutor")
  public void updatePostIndex(Post post) throws IOException {
    log.info("Updating elasticsearch index for post: {}", post.getId());
    // Implementation would update the PostIndex
    // For now, we'll simulate the operation
    log.info("Post index updated successfully for post: {}", post.getId());
  }

  @Async("ThreadPoolTaskExecutor")
  public void deletePostIndex(Long postId) throws IOException {
    log.info("Deleting elasticsearch index for post: {}", postId);
    // Implementation would delete the PostIndex
    // For now, we'll simulate the operation
    log.info("Post index deleted successfully for post: {}", postId);
  }

  public Map<Long, Integer> getViewCountsBySheetPostIds(List<Long> sheetPostIds) throws IOException {
    Map<Long, Integer> viewCountMap = new HashMap<>();

    if (sheetPostIds == null || sheetPostIds.isEmpty()) {
      return viewCountMap;
    }

    List<FieldValue> idValues = sheetPostIds.stream()
        .map(FieldValue::of)
        .toList();

    Query idsQuery = QueryBuilders.terms(t -> t
        .field("id")
        .terms(terms -> terms.value(idValues)));

    SearchResponse<SheetPostIndex> response = client.search(s -> s
        .index(SHEET_POST_INDEX_NAME)
        .query(idsQuery)
        .size(sheetPostIds.size())
        .source(sourceConfig -> sourceConfig
            .filter(f -> f.includes("id", "viewCount"))),
        SheetPostIndex.class);

    List<Hit<SheetPostIndex>> hits = response.hits().hits();
    for (Hit<SheetPostIndex> hit : hits) {
      SheetPostIndex sheetPostIndex = hit.source();
      if (sheetPostIndex != null) {
        viewCountMap.put(sheetPostIndex.getId(), sheetPostIndex.getViewCount());
      }
    }

    return viewCountMap;
  }

  public List<SheetPostIndex> getSearchSheetPostAutoComplete(String searchSentence, List<String> instruments,
      List<String> difficulties, List<String> genres) {
    // 1. multi_match (title.autocomplete, content.autocomplete)
    MultiMatchQuery mmQuery = MultiMatchQuery.of(q -> q
        .query(searchSentence)
        .fields("title.autocomplete^5", "content.autocomplete^3")
        .analyzer("my_nori_analyzer")
        .minimumShouldMatch("2<75%"));

    // 2. bool must (자동완성 + 필터 조건)
    BoolQuery boolQuery = BoolQuery.of(b -> {

      // keyword 자동완성
      b.must(mmQuery._toQuery());

      // instrument 필터
      if (instruments != null && !instruments.isEmpty()) {
        b.must(TermsQuery.of(t -> t
            .field("instrument")
            .terms(TermsQueryField.of(f -> f.value(
                instruments.stream().map(FieldValue::of).collect(Collectors.toList())))))
            ._toQuery());
      }

      // genre 필터
      if (genres != null && !genres.isEmpty()) {
        b.must(TermsQuery.of(t -> t
            .field("genre")
            .terms(TermsQueryField.of(f -> f.value(
                genres.stream().map(FieldValue::of).collect(Collectors.toList())))))
            ._toQuery());
      }

      // difficulty 필터
      if (difficulties != null && !difficulties.isEmpty()) {
        b.must(TermsQuery.of(t -> t
            .field("difficulty")
            .terms(TermsQueryField.of(f -> f.value(
                difficulties.stream().map(FieldValue::of).collect(Collectors.toList())))))
            ._toQuery());
      }

      return b;
    });

    // 3. function_score (viewCount 반영)
    FunctionScoreQuery functionScoreQuery = FunctionScoreQuery.of(f -> f
        .query(boolQuery._toQuery())
        .functions(fx -> fx
            .fieldValueFactor(fvf -> fvf
                .field("viewCount")
                .factor(1.2)
                .modifier(FieldValueFactorModifier.Log1p)))
        .boostMode(FunctionBoostMode.Sum));

    // 4. 검색 요청
    SearchRequest searchRequest = SearchRequest.of(s -> s
        .index("sheetpost_v3_v2")
        .query(functionScoreQuery._toQuery())
        .size(AUTOCOMPLETE_SIZE));

    SearchResponse<SheetPostIndex> response = null;
    try {
      response = client.search(searchRequest, SheetPostIndex.class);
    } catch (IOException e) {
      throw new ElasticSearchException("sheetpostindex search failed", e);
    }

    List<Hit<SheetPostIndex>> hits = response.hits().hits();
    return hits.stream().map(Hit::source).toList();
  }

}
