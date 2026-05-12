# Properties
doc_path: docs/design-docs/observability/api-observability-logs-metrics-20260416-1742/aggregate-design.md
owner: Codex
status: completed
title: 컨트롤러 기준 전체 API 디버그 로그 및 메트릭 수집
domain: observability
task: api-observability-logs-metrics-20260416-1742
last_updated: 2026-04-16:17:52

# Aggregate Design

## Application-Owned Domain Aggregate
- Responsibility:
  - N/A. 이 work unit은 product domain aggregate를 추가하거나 기존 aggregate transaction boundary를 바꾸지 않는다.
- Commands Handled:
  - N/A. 로그와 메트릭 조회 command는 Loki/Prometheus HTTP API가 처리한다.
- Events Produced:
  - N/A for product domain events. 애플리케이션은 observability telemetry를 출력하지만 domain event를 새로 발행하지 않는다.
- Included Entities / Value Objects:
  - N/A.
- Invariants:
  - 도메인 상태 변경과 observability 출력은 분리한다.
  - 로그/메트릭 수집 실패가 API domain transaction 성공/실패를 바꾸지 않아야 한다.
- Transaction Boundary Rationale:
  - 로그 출력과 metric recording은 cross-cutting side effect이며 MySQL/Redis/Elasticsearch domain persistence transaction에 포함하지 않는다.

## External Record Model: API Log Event
- Responsibility:
  - UC-1과 UC-2에서 Loki가 조회할 수 있는 request, response, error telemetry record를 표현한다.
- Commands Handled:
  - Loki `/loki/api/v1/query_range`
  - Loki `/loki/api/v1/labels`
  - Loki `/loki/api/v1/label/{name}/values`
- Events Produced:
  - `api.request.logged`
  - `api.response.logged`
  - `api.error.logged`
- Included Entities / Value Objects:
  - Log labels: `app`, `env`, `level`, `controller`, `route`, `method`, `status_class`, `exception`
  - Structured fields: `created_at`, `request_id`, `trace_id`, `status`, `elapsed_ms`, `request_summary`, `response_summary`, `error_message`, `stack_trace`, `user_id_hash`, `client_ip_hash`
- Invariants:
  - labels must be low-cardinality and safe for Loki indexing.
  - body summaries must follow masking, size limit, content-type allowlist, and multipart/file exclusion.
  - raw credentials, payment secrets, OAuth values, raw user id, raw IP, request body values, requestId, and traceId are not labels.
- Transaction Boundary Rationale:
  - Log event persistence is owned by Loki after Alloy collection, outside application database transactions.

## External Record Model: API Metric Time Series
- Responsibility:
  - UC-3에서 Prometheus가 조회할 수 있는 API call count, latency, outcome, status metric series를 표현한다.
- Commands Handled:
  - Prometheus `/api/v1/query_range`
  - Prometheus `/api/v1/query`
  - Prometheus `/api/v1/labels`
  - Prometheus `/api/v1/label/{name}/values`
- Events Produced:
  - `api.metric.sample.scraped`
  - `api.metric.query.returned`
- Included Entities / Value Objects:
  - Metric names: Actuator/Micrometer HTTP server metrics such as `http_server_requests_seconds_count`, `http_server_requests_seconds_sum`, and histogram buckets when enabled.
  - Metric labels: `application`, `env`, `controller`, `uri`, `method`, `status`, `outcome`, `exception`
- Invariants:
  - Prometheus is the selected backend.
  - URI labels must use stable templates, not raw values.
  - requestId, traceId, raw user id, raw IP, body values, and raw path variable values are not labels.
- Transaction Boundary Rationale:
  - Metric samples are scrape/query data owned by Prometheus and must not be stored or queried through application-owned domain repositories.

# Backlinks
- docs/work-units/observability/api-observability-logs-metrics-20260416-1742/index.md

# Discovery Hints (grep)
- grep -n "^## Application-Owned Domain Aggregate" docs/design-docs/observability/api-observability-logs-metrics-20260416-1742/aggregate-design.md
