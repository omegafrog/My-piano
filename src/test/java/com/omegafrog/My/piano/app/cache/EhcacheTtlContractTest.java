package com.omegafrog.My.piano.app.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EhcacheTtlContractTest {

    @Test
    @DisplayName("ehcache ttl covers SWR hard ttl contracts")
    void ehcacheTtlShouldCoverSwrHardTtl() throws IOException {
        var resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("ehcache.xml");
        assertNotNull(resource, "ehcache.xml must exist on classpath");

        String xml = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
        long listTtlSec = ttlSeconds(xml, "sheetpostList");
        long detailTtlSec = ttlSeconds(xml, "sheetpostDetail");

        assertTrue(listTtlSec >= 120, "sheetpostList ttl must be >= 120s");
        assertTrue(detailTtlSec >= 300, "sheetpostDetail ttl must be >= 300s");
    }

    private long ttlSeconds(String xml, String alias) {
        Pattern blockPattern = Pattern.compile("<cache alias=\"" + Pattern.quote(alias) + "\">(.*?)</cache>", Pattern.DOTALL);
        Matcher block = blockPattern.matcher(xml);
        assertTrue(block.find(), "Cache alias not found: " + alias);

        Matcher ttl = Pattern.compile("<ttl unit=\"(seconds|minutes|hours)\">(\\d+)</ttl>").matcher(block.group(1));
        assertTrue(ttl.find(), "TTL not found for cache: " + alias);

        String unit = ttl.group(1);
        long value = Long.parseLong(ttl.group(2));
        return switch (unit) {
            case "seconds" -> value;
            case "minutes" -> value * 60;
            case "hours" -> value * 3600;
            default -> throw new IllegalStateException("Unknown ttl unit: " + unit);
        };
    }
}
