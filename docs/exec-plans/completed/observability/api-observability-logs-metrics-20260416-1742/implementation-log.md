# Properties
doc_path: docs/exec-plans/completed/observability/api-observability-logs-metrics-20260416-1742/implementation-log.md
status: completed
domain: observability
task: api-observability-logs-metrics-20260416-1742
last_updated: 2026-04-16:19:11

# Summary
- Spring MVC `/api/**` requests now pass through a common observability filter that emits one structured completion log per request.
- Handled controller exceptions are logged once through `ApiExceptionLogger` with stack trace, request/trace correlation fields, route, controller, status class, and exception metadata.
- Request/response body logging is summary-only. It applies content-type allowlisting, multipart exclusion, max capture size, truncation marker, and masking for sensitive keys plus email/phone-like values.
- Prometheus registry support was added and actuator exposure now includes `/actuator/prometheus`.
- Final test gate passed after rerun with sufficient timeout: `./gradlew build` completed successfully in `16m 6s`, and `./gradlew test` completed successfully in `26s` with `:test UP-TO-DATE` after the build.
- No application-owned log or metric query API was added.

# Implemented Scope
- Added `ApiRequestLoggingFilter` as a `OncePerRequestFilter` under `app/utils/logging`.
- Added MDC fields:
  - `request_id`
  - `trace_id`
  - `method`
  - `route`
  - `controller`
  - `status_class`
- Added structured log fields through Logstash structured arguments:
  - `event`
  - `request_id`
  - `trace_id`
  - `method`
  - `route`
  - `controller`
  - `status`
  - `status_class`
  - `client_ip_hash`
  - `elapsed_ms`
  - `request_summary`
  - `response_summary`
  - `exception`
  - `error_message`
- Added request correlation behavior:
  - accepts safe `X-Request-Id`
  - accepts safe `X-Trace-Id`, `X-Correlation-Id`, or W3C `traceparent` trace id
  - generates a UUID request id when absent
  - returns `X-Request-Id` on the response
  - clears MDC in `finally`
- Added body logging policy:
  - allowed: `application/json`, `application/problem+json`, `application/x-www-form-urlencoded`
  - excluded: multipart bodies and unsupported/binary content types
  - max capture: 4096 bytes
  - over-limit marker: `...[truncated]`
  - omission reasons include `empty_body`, `multipart_excluded`, `unsupported_content_type`
  - sensitive key masking covers authorization/cookie/password/token/secret/payment key/OAuth/JWT-like keys
  - scalar masking covers email-like and Korean mobile-phone-like values
- Added Logback JSON stdout config with `LogstashEncoder`, MDC, key-value pairs, structured arguments, and static `app`/`env` fields.
- Added `.gitignore` exception so `src/main/resources/logback-spring.xml` is visible to normal git status/commit workflows despite the repository-wide `*.xml` ignore rule.
- Added `micrometer-registry-prometheus` and exposed `prometheus` in actuator endpoint config.
- Added `application=mypiano` as a safe common metrics tag.

# File Changes
## Created
- `src/main/java/com/omegafrog/My/piano/app/utils/logging/ApiBodySummary.java`
- `src/main/java/com/omegafrog/My/piano/app/utils/logging/ApiExceptionLogger.java`
- `src/main/java/com/omegafrog/My/piano/app/utils/logging/ApiRequestLoggingFilter.java`
- `src/main/resources/logback-spring.xml`
- `src/test/java/com/omegafrog/My/piano/app/utils/logging/ApiBodySummaryTest.java`
- `src/test/java/com/omegafrog/My/piano/app/utils/logging/ApiRequestLoggingFilterTest.java`
- `src/test/java/com/omegafrog/My/piano/app/utils/logging/PrometheusObservabilityConfigTest.java`

## Modified
- `.gitignore`
- `build.gradle`
- `src/main/java/com/omegafrog/My/piano/app/web/controller/ExceptionAdvisor.java`
- `src/main/resources/application.yml`
- `src/main/resources/application-prod.yml`
- `src/test/resources/application-test.yml`
- `docs/exec-plans/completed/observability/api-observability-logs-metrics-20260416-1742/plan.md`
- `docs/exec-plans/completed/observability/api-observability-logs-metrics-20260416-1742/implementation-log.md`
- `docs/work-units/observability/api-observability-logs-metrics-20260416-1742/index.md`

## Deleted / Renamed
- Moved execution docs from `docs/exec-plans/active/observability/api-observability-logs-metrics-20260416-1742/` to `docs/exec-plans/completed/observability/api-observability-logs-metrics-20260416-1742/` during closer stage.

# Code-to-Plan Mapping
- Common controller/API request-response logging:
  - `ApiRequestLoggingFilter`
- MDC/correlation context:
  - `ApiRequestLoggingFilter`
  - `ApiExceptionLogger`
- Summary-only body logging controls:
  - `ApiBodySummary`
  - `ApiRequestLoggingFilter`
- Duplicate exception logging reduction:
  - `ExceptionAdvisor`
  - `ApiExceptionLogger`
  - handled exception request attributes consumed by `ApiRequestLoggingFilter`
- Structured JSON output:
  - `logback-spring.xml`
  - `logstash-logback-encoder` dependency
- Prometheus support:
  - `micrometer-registry-prometheus` dependency
  - `management.endpoints.web.exposure.include=...,prometheus`
  - `management.metrics.tags.application=mypiano`
- Cardinality controls:
  - route uses Spring MVC best matching pattern
  - client IP is hashed
  - raw request id and trace id are log fields only, not metric labels
  - tests assert raw path values do not appear as metric/log route labels

# External Contract Changes
- New response header:
  - `X-Request-Id`
- New actuator scrape path enabled by configuration:
  - `/actuator/prometheus`
- No new application-owned Loki/Prometheus query/proxy API.

# Policy / Domain Rule Changes
- API body observability is intentionally summary-only.
- Multipart and unsupported/binary bodies are excluded from body content logging.
- Request ids, trace ids, raw user/client values, raw path variable values, query values, and body values must not be used as Prometheus labels or Loki labels.

# Architectural Impact
- Observability remains cross-cutting under `app/utils/logging`.
- Controllers remain thin; no controller-specific logging code was added.
- `ExceptionAdvisor` remains the central exception response boundary but delegates log emission to `ApiExceptionLogger`.
- The existing `PerformanceLoggingAspect` was left intact for its narrower performance tracing scope.

# Documentation Updates
- Updated this implementation log with actual executor output and final test_gate evidence.
- Updated completed plan progress and change log.
- Updated the work-unit hub status/findings so it reflects implemented scope and final gate results.
- Recorded actual Loki/Prometheus backend REST query execution as an operational follow-up because collector/backend access is outside this repository.
- Recorded `.gitignore` traceability fix for `logback-spring.xml` after post-execution doc verification found the file was ignored by `*.xml`.

# Validation Summary
- Passed:
  - `./gradlew compileJava`
  - `./gradlew test --tests 'com.omegafrog.My.piano.app.utils.logging.*'`
  - `timeout 1200s ./gradlew --no-daemon build --stacktrace --console=plain`
  - `timeout 1200s ./gradlew --no-daemon test --stacktrace --console=plain`
- Final test gate evidence:
  - `./gradlew build`: `BUILD SUCCESSFUL in 16m 6s`
  - `./gradlew test`: `BUILD SUCCESSFUL in 26s`, with `:test UP-TO-DATE` after the successful build
- Earlier blocked gate:
  - caused by insufficient wait time and sandbox Gradle socket/IP restrictions
  - not caused by an implementation test failure
- Focused test coverage:
  - normal request/response completion log contains route template, controller, status class, request id, masked request summary, and response summary
  - handled 500 exception logs stack trace and correlation context once
  - sensitive JSON/body/header values are masked
  - over-limit body summary is truncated with marker
  - multipart request body content is omitted with `multipart_excluded`
  - Prometheus registry scrape uses safe URI-template labels in test data
  - application config exposes `prometheus` in actuator endpoint include list

# Remaining Gaps
- Actual Loki `/loki/api/v1/query_range` and Prometheus `/api/v1/query_range` calls were not executed because collector/backend services and access details are outside this repository stage.
- Grafana Alloy, Loki, and Prometheus deployment/scrape configuration remains an operations follow-up outside application code.

# Risks & Follow-ups
- Log volume can increase on high-throughput APIs; collector retention and sampling policy should be handled operationally.
- `application-prod.yml` is tracked but production deployment may use external `application-prod.properties`; deployment config should mirror the actuator/logging settings.
- If operations promote structured fields to Loki labels, they must keep only low-cardinality fields such as `app`, `env`, `level`, `controller`, `route`, `method`, `status_class`, and `exception`.
- The current implementation logs request/response summaries for `/api/**`; actuator scrape and other non-API endpoints are intentionally outside body logging.
- `.gitignore` now explicitly unignores `src/main/resources/logback-spring.xml`; future XML runtime configs should be unignored deliberately if they are meant to be versioned.

# Backlinks
- docs/work-units/observability/api-observability-logs-metrics-20260416-1742/index.md
