package com.omegafrog.My.piano.app.external.elasticsearch;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

public final class ElasticSearchOperationMetrics {

	private static final String SEARCH_LATENCY = "mypiano.elasticsearch.search.latency";
	private static final String SEARCH_REQUESTS = "mypiano.elasticsearch.search.requests";
	private static final String SEARCH_EMPTY_HITS = "mypiano.elasticsearch.search.empty_hits";
	private static final String SEARCH_FALLBACKS = "mypiano.elasticsearch.search.fallbacks";
	private static final String INDEXING_REQUESTS = "mypiano.elasticsearch.indexing.requests";

	private ElasticSearchOperationMetrics() {
	}

	static Timer.Sample startSearchTimer() {
		return Timer.start(Metrics.globalRegistry);
	}

	static void recordSearchSuccess(String operation, Timer.Sample sample, boolean emptyHits) {
		recordSearch(operation, sample, "success");
		if (emptyHits) {
			counter(SEARCH_EMPTY_HITS, "operation", operation).increment();
		}
	}

	static void recordSearchTimeout(String operation, Timer.Sample sample) {
		recordSearch(operation, sample, "timeout");
	}

	static void recordSearchError(String operation, Timer.Sample sample) {
		recordSearch(operation, sample, "error");
	}

	public static void recordSearchFallback(Throwable cause) {
		counter(SEARCH_FALLBACKS, "reason", cause.getClass().getSimpleName()).increment();
	}

	static void recordIndexingSuccess(String operation) {
		counter(INDEXING_REQUESTS, "operation", operation, "outcome", "success").increment();
	}

	static void recordIndexingError(String operation) {
		counter(INDEXING_REQUESTS, "operation", operation, "outcome", "error").increment();
	}

	private static void recordSearch(String operation, Timer.Sample sample, String outcome) {
		sample.stop(Timer.builder(SEARCH_LATENCY)
				.tag("operation", operation)
				.tag("outcome", outcome)
				.register(Metrics.globalRegistry));
		counter(SEARCH_REQUESTS, "operation", operation, "outcome", outcome).increment();
	}

	private static Counter counter(String name, String... tags) {
		return Counter.builder(name)
				.tags(tags)
				.register(Metrics.globalRegistry);
	}
}
