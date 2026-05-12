# Properties
doc_path: docs/design-docs/observability/api-observability-logs-metrics-20260416-1742/detailed-design.md
owner: Codex
status: completed
title: 컨트롤러 기준 전체 API 디버그 로그 및 메트릭 수집
domain: observability
task: api-observability-logs-metrics-20260416-1742
last_updated: 2026-04-16:17:52

# Detailed Design

# Functional Behavior Design
## UC-1 API error log lookup
- Actor는 Loki HTTP API로 기간 및 label 조건을 전달한다.
- 애플리케이션은 error logs를 Loki에 직접 저장하지 않고 SLF4J/Logback으로 출력한다.
- Loki query example:
  - `GET /loki/api/v1/query_range?query={app="mypiano",env="prod",level="ERROR",controller="SheetPostController"}&start=2026-04-16T00:00:00Z&end=2026-04-16T01:00:00Z&limit=100`
- error log는 `request_id`, `trace_id`, `route`, `method`, `status`, `elapsed_ms`, `exception`, `error_message`, `stack_trace`를 포함해야 한다.

## UC-2 API request/response log lookup
- Actor는 Loki HTTP API로 기간, controller, route, method, status_class 조건을 전달한다.
- request/response body는 원문 전체 저장 대상이 아니다.
- request/response summary는 허용 content type, size limit, masking 정책을 통과한 경우에만 포함한다.
- Loki query example:
  - `GET /loki/api/v1/query_range?query={app="mypiano",env="prod",route="/api/v1/sheet-posts",status_class="5xx"}&start=...&end=...`

## UC-3 API metric lookup
- Actor는 Prometheus HTTP API로 기간, route/uri, method, status, outcome 조건을 전달한다.
- backend는 Prometheus로 확정하며 VictoriaMetrics는 이번 설계의 selected backend가 아니다.
- Prometheus query examples:
  - `GET /api/v1/query_range?query=sum by (uri,status)(rate(http_server_requests_seconds_count{application="mypiano",status=~"5.."}[5m]))&start=2026-04-16T00:00:00Z&end=2026-04-16T01:00:00Z&step=30s`
  - `GET /api/v1/query_range?query=histogram_quantile(0.95,sum by (le,uri,method)(rate(http_server_requests_seconds_bucket{application="mypiano"}[5m])))&start=...&end=...&step=30s`

# UI/UX Design Impact
- N/A for application UI.
- Grafana dashboard는 선택적 운영 편의사항이며 이번 work unit의 필수 조회 방식은 Loki/Prometheus REST API다.

# Technical / Refactoring Design Impact
## 1. Request/response logging entrypoint
- 전체 controller API coverage를 위해 controller별 logging code를 추가하지 않는다.
- 후보 구현은 `OncePerRequestFilter` 또는 `HandlerInterceptor`와 `ContentCachingRequestWrapper` / `ContentCachingResponseWrapper` 조합이다.
- route label은 Spring MVC best matching pattern 또는 handler metadata에서 얻는다.
- 기존 `PerformanceLoggingAspect`는 일부 controller/service method만 대상으로 하므로 전체 request/response logging 요구의 주 구현 지점으로 보지 않는다.

## 2. Correlation context
- request start 시 `request_id`가 없으면 생성하고, incoming header가 있으면 안전한 형식만 수용한다.
- MDC에 `request_id`, `trace_id`, `controller`, `route`, `method`, `status_class` 등 logging context를 넣고 request 완료 시 반드시 정리한다.
- `request_id`와 `trace_id`는 JSON field로 출력하되 Loki/Prometheus label로 사용하지 않는다.

## 3. Body logging policy
- Allowlisted content types:
  - `application/json`
  - `application/problem+json`
  - 필요한 경우 `application/x-www-form-urlencoded`는 masking이 충분할 때만 허용한다.
- Excluded content:
  - multipart requests
  - file upload/download payloads
  - binary content
  - unsupported content type
- Required controls:
  - maximum capture size in bytes
  - truncation marker for over-limit body
  - omission reason when body is skipped
  - masking for Authorization, Cookie, password, token, secret, payment key, OAuth value, phone/email-like sensitive values where applicable
- File upload controller paths must log metadata only, not file bytes.

## 4. Structured log shape
- Recommended log labels:
  - `app`, `env`, `level`, `controller`, `route`, `method`, `status_class`, `exception`
- Recommended JSON fields:
  - `created_at`, `request_id`, `trace_id`, `status`, `elapsed_ms`, `request_summary`, `response_summary`, `error_message`, `stack_trace`, `user_id_hash`, `client_ip_hash`, `omission_reason`
- Forbidden labels:
  - requestId, traceId, raw userId, raw client IP, raw path values, body values, query values, raw exception message
- Logback JSON encoder can be introduced in implementation, but the final log query design remains Loki-based.

## 5. Exception logging responsibility
- `ExceptionAdvisor` currently logs some exceptions but not all handlers consistently include stack trace and correlation context.
- Design target:
  - common request logging emits one completion record per request.
  - exception logging emits one detailed error record with stack trace and correlation context.
  - duplicate error logs from filter and advice should be avoided through explicit responsibility split or marker attribute on the request.

## 6. Metrics design
- Add Prometheus registry support if missing from implementation dependencies.
- Expose `/actuator/prometheus` under appropriate management endpoint configuration.
- Prefer Spring Boot Actuator HTTP server metrics as the baseline.
- If controller dimension is required and not available by default, add a safe custom observation convention or meter filter that derives controller names from bounded handler metadata.
- Do not add raw request values, requestId, traceId, raw user id, raw IP, or body values as metric labels.

## 7. Collection stack
- Logs:
  - Application: SLF4J + Logback structured JSON
  - Collector: Grafana Alloy
  - Backend/query: Loki HTTP API
- Metrics:
  - Application: Micrometer + Spring Boot Actuator
  - Backend/query: Prometheus HTTP API
- Promtail is not the recommended new collector.
- VictoriaMetrics single-node remains a reviewed lightweight alternative but is not selected.

## 8. Security and access boundary
- Loki/Prometheus API authentication and network exposure are operational concerns that must be solved outside the application API.
- Do not create application-owned proxy/query endpoints to hide Loki/Prometheus access unless a future work unit explicitly changes this requirement.

# Test / Quality Design Impact
- Request/response log tests:
  - normal controller path emits request and response records with controller, route, method, status, elapsed time, request_id.
  - exception path emits error record with stack trace and correlation context.
- Masking tests:
  - Authorization, Cookie, password, token, secret, payment/OAuth fields are not logged in raw form.
  - over-limit bodies are truncated with a marker.
  - unsupported content type and multipart/file payloads are omitted with omission reason.
- Metrics tests:
  - `/actuator/prometheus` is exposed in the relevant profile/config.
  - HTTP server metric series includes expected safe labels.
  - route/uri labels use templates, not raw IDs or path variables.
- Query verification:
  - Loki query examples can retrieve logs by time range and low-cardinality labels in a local or documented manual environment.
  - Prometheus query examples can retrieve call count, error rate, and latency by time range and labels in a local or documented manual environment.
- Required repository gate:
  - `./gradlew build`
  - If the build gate fails or is blocked by repository infrastructure, record the failing command, stage, and whether it is related to the observability change.

# Documentation Impact
- Keep canonical planning docs under this work unit.
- Implementation should update execution log and verification reports after code changes.
- If operational deployment files are deferred, document the remaining Alloy/Loki/Prometheus setup as a follow-up instead of implying it was implemented.

# Assumptions Required For Execution
- Log query access will be provided by Loki, not an application endpoint.
- Metric query access will be provided by Prometheus, not an application endpoint.
- Body logging is summary-only under strict masking and size controls.
- Existing controller thinness and `ExceptionAdvisor` centralization should be preserved.

# Backlinks
- docs/work-units/observability/api-observability-logs-metrics-20260416-1742/index.md

# Discovery Hints (grep)
- grep -n "^# Functional Behavior Design" docs/design-docs/observability/api-observability-logs-metrics-20260416-1742/detailed-design.md
