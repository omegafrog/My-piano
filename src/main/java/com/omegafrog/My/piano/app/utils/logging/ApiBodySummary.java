package com.omegafrog.My.piano.app.utils.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.http.MediaType;

public final class ApiBodySummary {

    static final int MAX_CAPTURE_BYTES = 4096;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern SENSITIVE_KEY = Pattern.compile(
        "(?i).*(authorization|cookie|password|passwd|pwd|token|secret|paymentKey|payment_key|oauth|accessToken|refreshToken|jwt).*");
    private static final Pattern EMAIL = Pattern.compile(
        "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE = Pattern.compile(
        "(?<!\\d)(?:\\+?82[- ]?)?0?1[016789][- ]?\\d{3,4}[- ]?\\d{4}(?!\\d)");

    private final boolean included;
    private final String contentType;
    private final int sizeBytes;
    private final String body;
    private final String omissionReason;
    private final boolean truncated;

    private ApiBodySummary(
        boolean included,
        String contentType,
        int sizeBytes,
        String body,
        String omissionReason,
        boolean truncated
    ) {
        this.included = included;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.body = body;
        this.omissionReason = omissionReason;
        this.truncated = truncated;
    }

    public static ApiBodySummary summarize(byte[] bytes, String contentType) {
        int sizeBytes = bytes == null ? 0 : bytes.length;
        String normalizedContentType = normalizeContentType(contentType);
        if (isMultipart(normalizedContentType)) {
            return omitted(normalizedContentType, sizeBytes, "multipart_excluded");
        }
        if (sizeBytes == 0) {
            return omitted(normalizedContentType, sizeBytes, "empty_body");
        }
        if (!isAllowlisted(normalizedContentType)) {
            return omitted(normalizedContentType, sizeBytes, "unsupported_content_type");
        }

        boolean truncated = sizeBytes > MAX_CAPTURE_BYTES;
        byte[] captured = bytes;
        if (truncated) {
            captured = new byte[MAX_CAPTURE_BYTES];
            System.arraycopy(bytes, 0, captured, 0, MAX_CAPTURE_BYTES);
        }

        Charset charset = charset(contentType);
        String text = new String(captured, charset);
        String masked = maskBody(text, normalizedContentType);
        if (truncated) {
            masked = masked + "...[truncated]";
        }
        return included(normalizedContentType, sizeBytes, masked, truncated);
    }

    public static ApiBodySummary omitted(String contentType, int sizeBytes, String omissionReason) {
        return new ApiBodySummary(false, contentType, sizeBytes, null, omissionReason, false);
    }

    public static String maskHeaderValue(String headerName, String value) {
        if (value == null) {
            return null;
        }
        if (headerName != null && SENSITIVE_KEY.matcher(headerName).matches()) {
            return "***";
        }
        return maskScalar(value);
    }

    public Map<String, Object> asLogFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("included", included);
        fields.put("content_type", contentType);
        fields.put("size_bytes", sizeBytes);
        fields.put("truncated", truncated);
        if (body != null) {
            fields.put("body", body);
        }
        if (omissionReason != null) {
            fields.put("omission_reason", omissionReason);
        }
        return fields;
    }

    public boolean included() {
        return included;
    }

    public String body() {
        return body;
    }

    public String omissionReason() {
        return omissionReason;
    }

    public boolean truncated() {
        return truncated;
    }

    private static ApiBodySummary included(
        String contentType,
        int sizeBytes,
        String body,
        boolean truncated
    ) {
        return new ApiBodySummary(true, contentType, sizeBytes, body, null, truncated);
    }

    private static String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "unknown";
        }
        try {
            return MediaType.parseMediaType(contentType).removeQualityValue().toString().toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException ex) {
            return contentType.toLowerCase(Locale.ROOT);
        }
    }

    private static boolean isAllowlisted(String normalizedContentType) {
        return normalizedContentType.startsWith(MediaType.APPLICATION_JSON_VALUE)
            || normalizedContentType.startsWith("application/problem+json")
            || normalizedContentType.startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }

    private static boolean isMultipart(String normalizedContentType) {
        return normalizedContentType.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE);
    }

    private static Charset charset(String contentType) {
        if (contentType == null) {
            return StandardCharsets.UTF_8;
        }
        try {
            MediaType mediaType = MediaType.parseMediaType(contentType);
            return mediaType.getCharset() == null ? StandardCharsets.UTF_8 : mediaType.getCharset();
        } catch (IllegalArgumentException ex) {
            return StandardCharsets.UTF_8;
        }
    }

    @SuppressWarnings("unchecked")
    private static String maskBody(String body, String normalizedContentType) {
        if (normalizedContentType.startsWith(MediaType.APPLICATION_JSON_VALUE)
            || normalizedContentType.startsWith("application/problem+json")) {
            try {
                Object parsed = OBJECT_MAPPER.readValue(body, Object.class);
                Object masked = maskJsonValue(null, parsed);
                return OBJECT_MAPPER.writeValueAsString(masked);
            } catch (IOException ex) {
                return maskScalar(maskKnownKeyValues(body));
            }
        }
        if (normalizedContentType.startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
            String[] parts = body.split("&");
            StringBuilder builder = new StringBuilder();
            for (String part : parts) {
                if (!builder.isEmpty()) {
                    builder.append('&');
                }
                int equals = part.indexOf('=');
                if (equals < 0) {
                    builder.append(maskScalar(part));
                    continue;
                }
                String key = part.substring(0, equals);
                String value = part.substring(equals + 1);
                builder.append(key).append('=');
                builder.append(SENSITIVE_KEY.matcher(key).matches() ? "***" : maskScalar(value));
            }
            return builder.toString();
        }
        return maskScalar(body);
    }

    private static Object maskJsonValue(String key, Object value) {
        if (value == null) {
            return null;
        }
        if (key != null && SENSITIVE_KEY.matcher(key).matches()) {
            return "***";
        }
        if (value instanceof Map<?, ?> source) {
            Map<String, Object> masked = new LinkedHashMap<>();
            source.forEach((entryKey, entryValue) -> {
                String childKey = String.valueOf(entryKey);
                masked.put(childKey, maskJsonValue(childKey, entryValue));
            });
            return masked;
        }
        if (value instanceof Iterable<?> iterable) {
            java.util.List<Object> masked = new java.util.ArrayList<>();
            iterable.forEach(item -> masked.add(maskJsonValue(key, item)));
            return masked;
        }
        if (value instanceof String stringValue) {
            return maskScalar(stringValue);
        }
        return value;
    }

    private static String maskKnownKeyValues(String text) {
        return text.replaceAll(
            "(?i)(authorization|cookie|password|passwd|pwd|token|secret|paymentKey|payment_key|oauth|accessToken|refreshToken|jwt)(\\s*[=:]\\s*)[^,\\s}\"&]+",
            "$1$2***");
    }

    private static String maskScalar(String value) {
        String masked = EMAIL.matcher(value).replaceAll("***@***");
        return PHONE.matcher(masked).replaceAll("***-****-****");
    }
}
