package com.omegafrog.My.piano.app.web.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

class SheetPostSearchRequestGuardTest {

	@Test
	void normalizesBlankSearchSentenceToNull() {
		assertThat(SheetPostSearchRequestGuard.normalizeSearchSentence("   ")).isNull();
	}

	@Test
	void normalizesBlankFilterValuesToNull() {
		assertThat(SheetPostSearchRequestGuard.normalizeFilterValues(List.of(" ", "\t"))).isNull();
	}

	@Test
	void rejectsOversizedSearchSentence() {
		String tooLong = "a".repeat(SheetPostSearchRequestGuard.MAX_SEARCH_SENTENCE_LENGTH + 1);

		assertThatThrownBy(() -> SheetPostSearchRequestGuard.validateListRequest(
				tooLong, null, null, null, PageRequest.of(0, 20)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("searchSentence");
	}

	@Test
	void rejectsOversizedPageRequest() {
		assertThatThrownBy(() -> SheetPostSearchRequestGuard.validateListRequest(
				null, null, null, null, PageRequest.of(0, SheetPostSearchRequestGuard.MAX_PAGE_SIZE + 1)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("page size");
	}

	@Test
	void rejectsDeepPageRequest() {
		assertThatThrownBy(() -> SheetPostSearchRequestGuard.validateListRequest(
				null, null, null, null, PageRequest.of(SheetPostSearchRequestGuard.MAX_PAGE_NUMBER + 1, 20)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("page number");
	}

	@Test
	void rejectsTooManyFilterValues() {
		List<String> filters = java.util.stream.IntStream.rangeClosed(1, SheetPostSearchRequestGuard.MAX_FILTER_VALUES + 1)
				.mapToObj(index -> "filter-" + index)
				.toList();

		assertThatThrownBy(() -> SheetPostSearchRequestGuard.validateAutocompleteRequest("aa", filters, null, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("instrument filter count");
	}

	@Test
	void autocompleteRequiresMinimumSearchLength() {
		assertThat(SheetPostSearchRequestGuard.isAutocompleteQueryAllowed("a")).isFalse();
		assertThat(SheetPostSearchRequestGuard.isAutocompleteQueryAllowed("aa")).isTrue();
	}
}
