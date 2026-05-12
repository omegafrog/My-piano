# Properties
doc_path: docs/design-docs/observability/api-observability-logs-metrics-20260416-1742/event-storming.md
owner: Codex
status: completed
title: 컨트롤러 기준 전체 API 디버그 로그 및 메트릭 수집
domain: observability
task: api-observability-logs-metrics-20260416-1742
last_updated: 2026-04-16:17:52

# Event Storming

## UC-1 기간과 태그로 API 에러 로그 조회
- Start Command:
  - Loki query range API로 error log query를 실행한다.
- Actors:
  - 운영자
  - 개발자
  - Codex 에이전트
- External Systems:
  - Grafana Alloy
  - Loki HTTP API
- Events:
  - API request received
  - Controller exception raised
  - Error response completed
  - Structured error log emitted
  - Log collected by Alloy
  - Log indexed by Loki labels and event timestamp
  - Error log query returned
- Policies:
  - `request_id`, `trace_id`, raw user id, raw IP, raw path value, body value는 Loki label로 승격하지 않는다.
  - stack trace는 error log JSON field 또는 log message payload에 포함하되 민감정보가 포함될 수 있는 request data는 masking 정책을 따른다.
  - `ExceptionAdvisor`와 request logging component가 같은 예외를 중복으로 error level 기록하지 않도록 책임을 나눈다.
- Follow-up Commands:
  - 같은 시간 범위와 route 조건으로 request/response 로그를 조회한다.
  - 같은 시간 범위와 URI 조건으로 Prometheus error rate query를 실행한다.
- Sync/Async Boundary:
  - 애플리케이션 request handling은 synchronous HTTP flow다.
  - 로그 수집과 Loki indexing은 application transaction 밖의 asynchronous observability flow다.
- Cross-Context Interaction:
  - API controller/security/service contexts는 telemetry producer다.
  - Loki는 query provider이며 애플리케이션은 log query API를 소유하지 않는다.

## UC-2 기간과 태그로 API 요청/응답 로그 조회
- Start Command:
  - Loki query range API로 request/response log query를 실행한다.
- Actors:
  - 운영자
  - 개발자
  - Codex 에이전트
- External Systems:
  - Grafana Alloy
  - Loki HTTP API
- Events:
  - API request received
  - Request metadata captured
  - Request body summary captured or omitted
  - Controller handler resolved
  - API response completed
  - Response body summary captured or omitted
  - Structured access log emitted
  - Log collected and queryable by Loki
- Policies:
  - body capture is allowed only for configured content types such as JSON and only up to the configured byte limit.
  - multipart and file upload payloads are excluded from body logging.
  - Authorization, Cookie, password, token, payment key, OAuth values, and similar secrets are masked.
  - route label uses Spring MVC best matching pattern, not raw path values.
- Follow-up Commands:
  - 같은 `request_id` 또는 `trace_id` structured field로 error log를 검색한다.
  - Prometheus latency query로 같은 route의 성능 지표를 확인한다.
- Sync/Async Boundary:
  - request/response summary generation occurs in the synchronous request lifecycle.
  - log ingestion and lookup occur asynchronously through Alloy and Loki.
- Cross-Context Interaction:
  - File upload controller paths may emit metadata-only logs because file/multipart bodies are excluded.
  - Security/auth controllers require stricter masking because credentials and OAuth values are likely.

## UC-3 기간과 태그로 API 메트릭 조회
- Start Command:
  - Prometheus query range API로 HTTP API metric query를 실행한다.
- Actors:
  - 운영자
  - 개발자
  - Codex 에이전트
- External Systems:
  - Spring Boot Actuator `/actuator/prometheus`
  - Prometheus HTTP API
- Events:
  - API request observed by Micrometer
  - HTTP server metric sample recorded
  - Prometheus scrapes actuator endpoint
  - Time series stored by Prometheus
  - Metric query returned
- Policies:
  - metric backend is Prometheus, not VictoriaMetrics.
  - labels must remain bounded: `application`, `env`, `controller`, `uri`, `method`, `status`, `outcome`, `exception`.
  - requestId, traceId, raw user id, raw IP, raw path values, body values must not be metric labels.
  - if controller tag cannot be supplied safely, route/method/status/outcome remain the minimum stable metric dimensions.
- Follow-up Commands:
  - Use Loki query range for the same route and time window when metric anomalies need log evidence.
- Sync/Async Boundary:
  - metric recording occurs in the request lifecycle.
  - Prometheus scraping and query are external pull/query operations.
- Cross-Context Interaction:
  - Actuator exposes metrics but Prometheus owns retention and query.
  - 애플리케이션은 metric query API를 새로 제공하지 않는다.

# Backlinks
- docs/work-units/observability/api-observability-logs-metrics-20260416-1742/index.md

# Discovery Hints (grep)
- grep -n "^## UC-" docs/design-docs/observability/api-observability-logs-metrics-20260416-1742/event-storming.md
