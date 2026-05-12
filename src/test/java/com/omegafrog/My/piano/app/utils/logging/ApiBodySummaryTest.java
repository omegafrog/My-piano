package com.omegafrog.My.piano.app.utils.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ApiBodySummaryTest {

    @Test
    void masksSensitiveJsonValuesAndPersonalData() {
        String body = """
            {
              "username": "tester",
              "password": "plain-password",
              "accessToken": "raw-token",
              "email": "person@example.com",
              "phone": "010-1234-5678"
            }
            """;

        ApiBodySummary summary = ApiBodySummary.summarize(
            body.getBytes(StandardCharsets.UTF_8),
            MediaType.APPLICATION_JSON_VALUE);

        assertThat(summary.included()).isTrue();
        assertThat(summary.body())
            .contains("\"password\":\"***\"")
            .contains("\"accessToken\":\"***\"")
            .contains("***@***")
            .contains("***-****-****")
            .doesNotContain("plain-password")
            .doesNotContain("raw-token")
            .doesNotContain("person@example.com")
            .doesNotContain("010-1234-5678");
    }

    @Test
    void truncatesLargeAllowedBodyWithMarker() {
        String body = "{\"content\":\"" + "a".repeat(ApiBodySummary.MAX_CAPTURE_BYTES + 100) + "\"}";

        ApiBodySummary summary = ApiBodySummary.summarize(
            body.getBytes(StandardCharsets.UTF_8),
            MediaType.APPLICATION_JSON_VALUE);

        assertThat(summary.included()).isTrue();
        assertThat(summary.truncated()).isTrue();
        assertThat(summary.body()).endsWith("...[truncated]");
    }

    @Test
    void omitsMultipartBodies() {
        ApiBodySummary summary = ApiBodySummary.summarize(
            "file bytes".getBytes(StandardCharsets.UTF_8),
            MediaType.MULTIPART_FORM_DATA_VALUE + "; boundary=test");

        assertThat(summary.included()).isFalse();
        assertThat(summary.omissionReason()).isEqualTo("multipart_excluded");
    }
}
