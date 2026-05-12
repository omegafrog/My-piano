package com.omegafrog.My.piano.app.utils.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.persistence.PersistenceException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class ApiRequestLoggingFilterTest {

    private final ListAppender<ILoggingEvent> requestLogAppender = new ListAppender<>();
    private final ListAppender<ILoggingEvent> exceptionLogAppender = new ListAppender<>();

    private MockMvc mockMvc;
    private ch.qos.logback.classic.Logger requestLogger;
    private ch.qos.logback.classic.Logger exceptionLogger;

    @BeforeEach
    void setUp() {
        requestLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ApiRequestLoggingFilter.class);
        exceptionLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ApiExceptionLogger.class);
        requestLogAppender.start();
        exceptionLogAppender.start();
        requestLogger.addAppender(requestLogAppender);
        exceptionLogger.addAppender(exceptionLogAppender);
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new com.omegafrog.My.piano.app.web.controller.ExceptionAdvisor(
                new ApiExceptionLogger()))
            .addFilters(new ApiRequestLoggingFilter())
            .build();
    }

    @AfterEach
    void tearDown() {
        requestLogger.detachAppender(requestLogAppender);
        exceptionLogger.detachAppender(exceptionLogAppender);
        requestLogAppender.list.clear();
        exceptionLogAppender.list.clear();
    }

    @Test
    void logsApiRequestAndResponseWithCorrelationAndMaskedSummary() throws Exception {
        mockMvc.perform(post("/api/v1/log-test/42")
                .header("X-Request-Id", "request-123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer raw-token")
                .header(HttpHeaders.COOKIE, "SESSION=raw-cookie")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "password": "plain-password",
                      "token": "raw-token",
                      "email": "person@example.com"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Request-Id", "request-123"));

        String completed = completedLog();
        assertThat(completed)
            .contains("request-123")
            .contains("/api/v1/log-test/{id}")
            .contains("TestController")
            .contains("2xx")
            .contains("authorization=***")
            .contains("cookie=***")
            .doesNotContain("/api/v1/log-test/42")
            .doesNotContain("plain-password")
            .doesNotContain("raw-token")
            .doesNotContain("person@example.com");
    }

    @Test
    void omitsMultipartRequestBodyFromLogs() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "sheet.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "file bytes that must not appear".getBytes());

        mockMvc.perform(multipart("/api/v1/log-test/upload").file(file))
            .andExpect(status().isOk());

        String completed = completedLog();
        assertThat(completed)
            .contains("multipart_excluded")
            .doesNotContain("file bytes that must not appear");
    }

    @Test
    void handledExceptionLogsStackTraceOnceWithCorrelationContext() throws Exception {
        mockMvc.perform(get("/api/v1/log-test/fail")
                .header("X-Request-Id", "request-500"))
            .andExpect(status().isInternalServerError());

        List<ILoggingEvent> exceptionEvents = exceptionLogAppender.list;
        assertThat(exceptionEvents).hasSize(1);
        assertThat(exceptionEvents.get(0).getLevel()).isEqualTo(Level.ERROR);
        assertThat(exceptionEvents.get(0).getThrowableProxy().getClassName())
            .isEqualTo(PersistenceException.class.getName());
        assertThat(exceptionEvents.get(0).getFormattedMessage())
            .contains("request-500")
            .contains("status=500")
            .contains("trace_id=request-500");

        long throwableLogCount = requestLogAppender.list.stream()
            .filter(event -> event.getThrowableProxy() != null)
            .count();
        assertThat(throwableLogCount).isZero();
        assertThat(completedLog()).contains("status=500").contains("5xx");
    }

    private String completedLog() {
        return requestLogAppender.list.stream()
            .map(ILoggingEvent::getFormattedMessage)
            .filter(message -> message.contains("http request completed"))
            .findFirst()
            .orElseThrow();
    }

    @RestController
    static class TestController {

        @PostMapping("/api/v1/log-test/{id}")
        Map<String, Object> echo(@PathVariable Long id, @RequestBody Map<String, Object> body) {
            return Map.of("id", id, "accepted", true);
        }

        @PostMapping(path = "/api/v1/log-test/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        Map<String, Object> upload() {
            return Map.of("accepted", true);
        }

        @GetMapping("/api/v1/log-test/fail")
        Map<String, Object> fail() {
            throw new PersistenceException("database failed");
        }
    }
}
