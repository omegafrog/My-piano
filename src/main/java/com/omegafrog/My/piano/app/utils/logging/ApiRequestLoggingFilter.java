package com.omegafrog.My.piano.app.utils.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class ApiRequestLoggingFilter extends OncePerRequestFilter {

    public static final String HANDLED_EXCEPTION_LOGGED_ATTRIBUTE =
        ApiRequestLoggingFilter.class.getName() + ".HANDLED_EXCEPTION_LOGGED";
    public static final String HANDLED_EXCEPTION_STATUS_ATTRIBUTE =
        ApiRequestLoggingFilter.class.getName() + ".HANDLED_EXCEPTION_STATUS";

    private static final Logger log = LoggerFactory.getLogger(ApiRequestLoggingFilter.class);
    private static final Pattern SAFE_CORRELATION_VALUE = Pattern.compile("[A-Za-z0-9._:-]{1,128}");
    private static final String UNKNOWN = "unknown";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        String requestId = correlationValue(request.getHeader("X-Request-Id"));
        String traceId = correlationValue(request.getHeader("X-Trace-Id"));
        if (!StringUtils.hasText(traceId)) {
            traceId = correlationValue(request.getHeader("X-Correlation-Id"));
        }
        if (!StringUtils.hasText(traceId)) {
            traceId = traceIdFromTraceparent(request.getHeader("traceparent"));
        }
        if (!StringUtils.hasText(requestId)) {
            requestId = UUID.randomUUID().toString();
        }
        if (!StringUtils.hasText(traceId)) {
            traceId = requestId;
        }

        long startedAt = System.nanoTime();
        MDC.put("request_id", requestId);
        MDC.put("trace_id", traceId);
        MDC.put("method", request.getMethod());
        wrappedResponse.setHeader("X-Request-Id", requestId);

        Throwable thrown = null;
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (Throwable ex) {
            thrown = ex;
            throw ex;
        } finally {
            try {
                int status = statusForLog(wrappedResponse, wrappedRequest);
                long elapsedMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
                putCompletionMdc(wrappedRequest, status);

                if (thrown != null && !Boolean.TRUE.equals(
                    wrappedRequest.getAttribute(HANDLED_EXCEPTION_LOGGED_ATTRIBUTE))) {
                    log.error("http request failed {}", StructuredArguments.entries(
                        commonLogFields(wrappedRequest, status, thrown)), thrown);
                }

                Map<String, Object> fields = commonLogFields(wrappedRequest, status, thrown);
                fields.put("event", "http_request_completed");
                fields.put("elapsed_ms", elapsedMs);
                fields.put("request_summary", requestSummary(wrappedRequest));
                fields.put("response_summary", responseSummary(wrappedResponse));
                log.info("http request completed {}", StructuredArguments.entries(fields));
            } finally {
                MDC.clear();
                wrappedResponse.copyBodyToResponse();
            }
        }
    }

    static Map<String, Object> commonLogFields(
        HttpServletRequest request,
        int status,
        Throwable throwable
    ) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("event", throwable == null ? "http_request" : "http_request_exception");
        fields.put("request_id", safeMdc("request_id"));
        fields.put("trace_id", safeMdc("trace_id"));
        fields.put("method", request == null ? UNKNOWN : request.getMethod());
        fields.put("route", route(request));
        fields.put("controller", controller(request));
        fields.put("status", status);
        fields.put("status_class", statusClass(status));
        fields.put("client_ip_hash", hashValue(clientIp(request)));
        if (throwable != null) {
            fields.put("exception", throwable.getClass().getSimpleName());
            fields.put("error_message", throwable.getMessage());
        }
        return fields;
    }

    private static Map<String, Object> requestSummary(ContentCachingRequestWrapper request) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("content_type", contentType(request.getContentType()));
        summary.put("content_length", request.getContentLengthLong());
        summary.put("has_query", StringUtils.hasText(request.getQueryString()));

        Map<String, Object> headers = new LinkedHashMap<>();
        putMaskedHeader(headers, request, HttpHeaders.AUTHORIZATION);
        putMaskedHeader(headers, request, HttpHeaders.COOKIE);
        if (!headers.isEmpty()) {
            summary.put("headers", headers);
        }

        summary.put("body", ApiBodySummary.summarize(
            request.getContentAsByteArray(),
            request.getContentType()).asLogFields());
        return summary;
    }

    private static Map<String, Object> responseSummary(ContentCachingResponseWrapper response) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("content_type", contentType(response.getContentType()));
        summary.put("body", ApiBodySummary.summarize(
            response.getContentAsByteArray(),
            response.getContentType()).asLogFields());
        return summary;
    }

    private static void putMaskedHeader(
        Map<String, Object> headers,
        HttpServletRequest request,
        String headerName
    ) {
        String value = request.getHeader(headerName);
        if (value != null) {
            headers.put(headerName.toLowerCase(), ApiBodySummary.maskHeaderValue(headerName, value));
        }
    }

    private static void putCompletionMdc(HttpServletRequest request, int status) {
        MDC.put("route", route(request));
        MDC.put("controller", controller(request));
        MDC.put("status_class", statusClass(status));
    }

    private static int statusForLog(HttpServletResponse response, HttpServletRequest request) {
        Object handledStatus = request.getAttribute(HANDLED_EXCEPTION_STATUS_ATTRIBUTE);
        if (handledStatus instanceof Integer status) {
            return status;
        }
        return response.getStatus();
    }

    private static String route(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        Object route = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return route == null ? UNKNOWN : String.valueOf(route);
    }

    private static String controller(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (handler == null) {
            return UNKNOWN;
        }
        String value = String.valueOf(handler);
        int hashIndex = value.indexOf('#');
        if (hashIndex > 0) {
            int dotIndex = value.lastIndexOf('.', hashIndex);
            return value.substring(dotIndex + 1, hashIndex);
        }
        return value;
    }

    private static String statusClass(int status) {
        return (status / 100) + "xx";
    }

    private static String contentType(String contentType) {
        return StringUtils.hasText(contentType) ? contentType : UNKNOWN;
    }

    private static String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            int comma = forwardedFor.indexOf(',');
            return comma < 0 ? forwardedFor.trim() : forwardedFor.substring(0, comma).trim();
        }
        return request.getRemoteAddr();
    }

    private static String hashValue(String value) {
        if (!StringUtils.hasText(value)) {
            return UNKNOWN;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException ex) {
            return UNKNOWN;
        }
    }

    private static String safeMdc(String key) {
        String value = MDC.get(key);
        return StringUtils.hasText(value) ? value : UNKNOWN;
    }

    private static String correlationValue(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return SAFE_CORRELATION_VALUE.matcher(trimmed).matches() ? trimmed : null;
    }

    private static String traceIdFromTraceparent(String traceparent) {
        if (!StringUtils.hasText(traceparent)) {
            return null;
        }
        String[] parts = traceparent.split("-");
        if (parts.length >= 2 && SAFE_CORRELATION_VALUE.matcher(parts[1]).matches()) {
            return parts[1];
        }
        return null;
    }
}
