package com.omegafrog.My.piano.app.web.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

final class SheetPostSearchRequestGuard {

	static final int MAX_SEARCH_SENTENCE_LENGTH = 100;
	static final int MAX_PAGE_SIZE = 50;
	static final int MAX_PAGE_NUMBER = 100;
	static final int MAX_FILTER_VALUES = 10;
	static final int MIN_AUTOCOMPLETE_SEARCH_LENGTH = 2;

	private SheetPostSearchRequestGuard() {
	}

	static void validateListRequest(
			String searchSentence,
			List<String> instrument,
			List<String> difficulty,
			List<String> genre,
			Pageable pageable) {
		validateSearchSentence(searchSentence);
		validatePageable(pageable);
		validateFilterValues("instrument", instrument);
		validateFilterValues("difficulty", difficulty);
		validateFilterValues("genre", genre);
	}

	static void validateAutocompleteRequest(
			String searchSentence,
			List<String> instrument,
			List<String> difficulty,
			List<String> genre) {
		validateSearchSentence(searchSentence);
		validateFilterValues("instrument", instrument);
		validateFilterValues("difficulty", difficulty);
		validateFilterValues("genre", genre);
	}

	static boolean isAutocompleteQueryAllowed(String searchSentence) {
		return searchSentence != null && searchSentence.length() >= MIN_AUTOCOMPLETE_SEARCH_LENGTH;
	}

	static String normalizeSearchSentence(String searchSentence) {
		if (searchSentence == null) {
			return null;
		}
		String trimmed = searchSentence.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	static List<String> normalizeFilterValues(List<String> values) {
		if (values == null) {
			return null;
		}
		List<String> normalized = values.stream()
				.filter(value -> value != null && !value.isBlank())
				.map(String::trim)
				.distinct()
				.toList();
		return normalized.isEmpty() ? null : normalized;
	}

	private static void validateSearchSentence(String searchSentence) {
		if (searchSentence != null && searchSentence.length() > MAX_SEARCH_SENTENCE_LENGTH) {
			throw new IllegalArgumentException("searchSentence must be " + MAX_SEARCH_SENTENCE_LENGTH + " characters or less.");
		}
	}

	private static void validatePageable(Pageable pageable) {
		if (pageable.getPageSize() > MAX_PAGE_SIZE) {
			throw new IllegalArgumentException("page size must be " + MAX_PAGE_SIZE + " or less.");
		}
		if (pageable.getPageNumber() > MAX_PAGE_NUMBER) {
			throw new IllegalArgumentException("page number must be " + MAX_PAGE_NUMBER + " or less.");
		}
	}

	private static void validateFilterValues(String fieldName, List<String> values) {
		if (values != null && values.size() > MAX_FILTER_VALUES) {
			throw new IllegalArgumentException(fieldName + " filter count must be " + MAX_FILTER_VALUES + " or less.");
		}
	}
}
