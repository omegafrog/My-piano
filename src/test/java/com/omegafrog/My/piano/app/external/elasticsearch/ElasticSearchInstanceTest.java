package com.omegafrog.My.piano.app.external.elasticsearch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;

class ElasticSearchInstanceTest {

	private final ElasticsearchClient client = mock(ElasticsearchClient.class);
	private final ElasticSearchInstance elasticSearchInstance = new ElasticSearchInstance(client);

	@Test
	void searchSheetPostUsesFilterClausesForFacetInputs() throws IOException {
		when(client.search(org.mockito.ArgumentMatchers.any(SearchRequest.class), eq(SheetPostIndex.class)))
				.thenReturn(emptyResponse());

		elasticSearchInstance.searchSheetPost(
				"mozart",
				List.of("PIANO"),
				List.of("EASY"),
				List.of("CLASSIC"),
				PageRequest.of(0, 20));

		BoolQuery boolQuery = capturedSearchRequest().query().functionScore().query().bool();

		assertThat(boolQuery.must()).hasSize(1);
		assertThat(boolQuery.filter()).hasSize(3);
		assertThat(boolQuery.should()).isEmpty();
	}

	@Test
	void searchSheetPostUsesMatchAllForBlankSearchSentence() throws IOException {
		when(client.search(org.mockito.ArgumentMatchers.any(SearchRequest.class), eq(SheetPostIndex.class)))
				.thenReturn(emptyResponse());

		elasticSearchInstance.searchSheetPost(null, null, null, null, PageRequest.of(0, 20));

		Query baseQuery = capturedSearchRequest().query().functionScore().query().bool().must().get(0);

		assertThat(baseQuery.isMatchAll()).isTrue();
	}

	private SearchRequest capturedSearchRequest() throws IOException {
		ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
		verify(client).search(captor.capture(), eq(SheetPostIndex.class));
		return captor.getValue();
	}

	private SearchResponse<SheetPostIndex> emptyResponse() {
		return SearchResponse.of(response -> response
				.took(1)
				.timedOut(false)
				.shards(shards -> shards.total(1).successful(1).failed(0))
				.hits(hits -> hits
						.total(total -> total.value(0).relation(TotalHitsRelation.Eq))
						.hits(List.of())));
	}
}
