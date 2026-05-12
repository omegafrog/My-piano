# Properties
doc_path: docs/design-docs/observability/api-observability-logs-metrics-20260416-1742/bounded-context.md
owner: Codex
status: completed
title: 컨트롤러 기준 전체 API 디버그 로그 및 메트릭 수집
domain: observability
task: api-observability-logs-metrics-20260416-1742
last_updated: 2026-04-16:17:52

# Bounded Context Finalization

## Context: API Observability
- Responsibility:
  - 전체 REST controller API의 request, response, exception, latency, status, outcome telemetry를 일관된 형식으로 만든다.
  - Loki와 Prometheus가 기간 및 태그 조건으로 조회할 수 있는 저카디널리티 label 체계를 유지한다.
  - 애플리케이션 내부 조회 API를 소유하지 않고 external observability stack query API를 사용하게 한다.
- Included Aggregates:
  - Application-owned aggregate: N/A.
  - External API log event record model for UC-1 and UC-2.
  - External API metric time-series model for UC-3.
- Key Terms:
  - `request_id`: 요청 단위 correlation field. label이 아니라 structured field다.
  - `trace_id`: trace/log correlation field. label이 아니라 structured field다.
  - `route` / `uri`: Spring MVC template 기반 path pattern. raw path가 아니다.
  - `status_class`: `2xx`, `4xx`, `5xx`처럼 제한된 log label.
  - `request_summary` / `response_summary`: masking, size limit, content-type allowlist가 적용된 body 요약.
  - `omission_reason`: body logging이 제외된 이유. 예: multipart, unsupported content type, over size limit.
- Why This Boundary Exists:
  - Observability는 SheetPost, Post, Order, User 등 product domain을 관통하지만 어떤 product aggregate도 소유하지 않는다.
  - 로그와 메트릭은 장애 대응 및 비기능 분석을 위한 operational data이며 application domain repository에 저장하지 않는다.
  - Loki와 Prometheus가 이미 query boundary를 제공하므로 애플리케이션 API surface를 늘리지 않는 것이 요구사항에 맞다.
- Relation With Other Contexts:
  - `app/web/controller`: telemetry source. controller, route, method, status, exception context를 제공한다.
  - `app/web/controller/ExceptionAdvisor`: exception response mapping source. 공통 로깅과 중복 error log를 피해야 한다.
  - `app/security`: auth, token, OAuth 관련 민감정보 masking 정책의 주요 source.
  - `app/cache`: 기존 Micrometer 기반 cache metric binder와 같은 MeterRegistry를 공유할 수 있다.
  - Grafana Alloy/Loki: log collection, storage, query context.
  - Prometheus: metric scrape, storage, query context.

# Backlinks
- docs/work-units/observability/api-observability-logs-metrics-20260416-1742/index.md

# Discovery Hints (grep)
- grep -n "^## Context:" docs/design-docs/observability/api-observability-logs-metrics-20260416-1742/bounded-context.md
