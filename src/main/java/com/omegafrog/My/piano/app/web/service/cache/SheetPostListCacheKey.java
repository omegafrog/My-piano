package com.omegafrog.My.piano.app.web.service.cache;

import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record SheetPostListCacheKey(
        String searchSentence,
        List<String> instruments,
        List<String> difficulties,
        List<String> genres,
        int page,
        int size
) implements Serializable {

    public static SheetPostListCacheKey of(
            String searchSentence,
            List<String> instruments,
            List<String> difficulties,
            List<String> genres,
            Pageable pageable
    ) {
        return new SheetPostListCacheKey(
                normalizeText(searchSentence),
                normalizeList(instruments),
                normalizeList(difficulties),
                normalizeList(genres),
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
    }

    public boolean cacheable() {
        return page >= 0 && page <= 2 && size <= 50;
    }

    public String asStringKey() {
        return searchSentence + "|" + String.join(",", instruments)
                + "|" + String.join(",", difficulties)
                + "|" + String.join(",", genres)
                + "|" + page
                + "|" + size;
    }

    private static String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private static List<String> normalizeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                normalized.add(trimmed);
            }
        }
        Collections.sort(normalized);
        return List.copyOf(normalized);
    }
}
