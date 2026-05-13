package com.omegafrog.My.piano.app.external.elasticsearch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import com.omegafrog.My.piano.app.external.elasticsearch.exception.ElasticSearchException;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class ElasticSearchInstanceResilienceTest {

	private SimpleMeterRegistry registry;

	@BeforeEach
	void setUp() {
		registry = new SimpleMeterRegistry();
		Metrics.addRegistry(registry);
	}

	@AfterEach
	void tearDown() {
		Metrics.removeRegistry(registry);
		registry.close();
	}

	@Test
	void searchSheetPostRecordsTimeoutAndThrowsWhenElasticsearchTimesOut() throws IOException {
		ElasticsearchClient client = mock(ElasticsearchClient.class);
		ElasticSearchInstance elasticSearchInstance = new ElasticSearchInstance(client);
		when(client.search(any(SearchRequest.class), eq(SheetPostIndex.class)))
				.thenReturn(searchResponse(true));

		assertThatThrownBy(() -> elasticSearchInstance.searchSheetPost(
				"bach", null, null, null, PageRequest.of(0, 20)))
				.isInstanceOf(ElasticSearchException.class)
				.hasMessageContaining("timed out");

		assertThat(registry.counter(
				"mypiano.elasticsearch.search.requests",
				"operation", "sheet_post_search",
				"outcome", "timeout").count()).isEqualTo(1.0);
	}

	private SearchResponse<SheetPostIndex> searchResponse(boolean timedOut) {
		return SearchResponse.of(response -> response
				.took(1000)
				.timedOut(timedOut)
				.shards(shards -> shards.total(1).successful(1).failed(0))
				.hits(hits -> hits
						.total(total -> total.value(0).relation(TotalHitsRelation.Eq))
						.hits(List.of())));
	}
}
