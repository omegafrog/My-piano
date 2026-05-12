# Properties
doc_path: docs/product-specs/observability/api-observability-logs-metrics-20260416-1742/use-cases.md
owner: Codex
status: completed
title: 컨트롤러 기준 전체 API 디버그 로그 및 메트릭 수집
domain: observability
task: api-observability-logs-metrics-20260416-1742
last_updated: 2026-04-16:17:52

# Confirmed Use Cases

## UC-1 기간과 태그로 API 에러 로그 조회
- Primary Actor:
  - 운영자, 개발자, Codex 에이전트
- Goal:
  - 특정 생성일 범위와 태그 조건으로 API 에러 로그를 조회해 장애 원인을 파악한다.
- Preconditions:
  - 애플리케이션이 SLF4J/Logback 구조화 로그를 출력한다.
  - Grafana Alloy가 로그를 수집해 Loki로 전송한다.
  - Loki HTTP API 접근 권한과 네트워크 경로가 준비되어 있다.
- Trigger:
  - 5xx 증가, 특정 controller 장애, 사용자 문의, 외부 연동 장애, 배치/이벤트 처리 연계 장애
- Main Success Flow:
  - Actor가 시간 범위 `start`, `end`를 정한다.
  - Actor가 `app`, `env`, `level=ERROR`, `controller`, `route`, `method`, `status_class`, `exception` 중 필요한 저카디널리티 label 조건을 정한다.
  - Actor가 Loki `/loki/api/v1/query_range`로 LogQL query를 실행한다.
  - Actor가 반환된 로그의 JSON field에서 `request_id`, `trace_id`, `user_id_hash`, `client_ip_hash`, `elapsed_ms`, `error_message`, `stack_trace`를 확인한다.
  - Actor가 request/trace correlation field로 같은 요청의 request/response 로그를 추가 조회한다.
- Alternative / Failure Flow:
  - label 조건이 너무 넓어 결과가 많으면 `controller`, `route`, `status_class`, `exception`을 추가해 범위를 좁힌다.
  - requestId나 traceId는 label이 아니므로 label selector가 아니라 JSON field filtering 또는 로그 본문 검색으로 찾는다.
  - Loki API 접근이 차단되면 애플리케이션 API를 새로 만들지 않고 운영 접근 권한 또는 네트워크 경계를 조정한다.
- Success Outcome:
  - Actor가 장애 재현에 필요한 controller, route, status, exception, elapsed time, masked request summary, stack trace를 확보한다.

## UC-2 기간과 태그로 API 요청/응답 로그 조회
- Primary Actor:
  - 운영자, 개발자, Codex 에이전트
- Goal:
  - 특정 API 요청 흐름의 입력 요약, 응답 요약, 상태, 소요 시간을 조회해 재현 정보를 확보한다.
- Preconditions:
  - 전체 controller API에 공통 request/response logging이 적용되어 있다.
  - body logging은 masking, size limit, content-type allowlist, multipart/file exclusion 정책을 따른다.
  - Loki HTTP API 접근 권한과 네트워크 경로가 준비되어 있다.
- Trigger:
  - 특정 API 응답 이상, 데이터 오류, 느린 응답, 클라이언트 요청 재현 필요
- Main Success Flow:
  - Actor가 시간 범위와 controller/route/method/status_class 조건을 정한다.
  - Actor가 Loki `/loki/api/v1/query_range`로 request 또는 response 로그를 조회한다.
  - Actor가 로그 JSON field에서 `request_id`, `trace_id`, `request_summary`, `response_summary`, `elapsed_ms`, `status`를 확인한다.
  - Actor가 같은 `request_id` 또는 `trace_id` 값을 가진 request, response, error 로그를 연결해 본다.
- Alternative / Failure Flow:
  - body가 multipart/file payload이면 body content는 조회되지 않고 metadata와 omission reason만 확인한다.
  - content type이 allowlist 밖이면 body content는 조회되지 않고 content type과 size metadata만 확인한다.
  - 민감 필드는 원문 대신 masked value 또는 hash value로 확인한다.
- Success Outcome:
  - Actor가 보안 정책을 위반하지 않는 범위에서 API request/response 재현 정보를 확보한다.

## UC-3 기간과 태그로 API 메트릭 조회
- Primary Actor:
  - 운영자, 개발자, Codex 에이전트
- Goal:
  - 특정 기간의 API 응답 시간, 호출량, 에러율을 조회해 성능 저하와 장애를 분석한다.
- Preconditions:
  - 애플리케이션에 Micrometer, Spring Boot Actuator, Prometheus registry가 구성되어 있다.
  - `/actuator/prometheus`가 필요한 운영 보안 정책 아래 노출되어 있다.
  - Prometheus가 애플리케이션 endpoint를 scrape한다.
- Trigger:
  - 응답 시간 SLA/SLO 위반, 4xx/5xx 증가, 특정 route 병목, 캐시/DB/외부 연동 병목 의심
- Main Success Flow:
  - Actor가 시간 범위 `start`, `end`, `step`을 정한다.
  - Actor가 `application`, `env`, `controller`, `uri`, `method`, `status`, `outcome`, `exception` 중 필요한 저카디널리티 label 조건을 정한다.
  - Actor가 Prometheus `/api/v1/query_range`로 호출량, 에러율, latency percentile query를 실행한다.
  - Actor가 route/method/status 기준으로 성능 저하 또는 에러율 증가 구간을 확인한다.
  - Actor가 필요한 경우 같은 시간 범위와 route 조건으로 Loki 로그를 이어서 조회한다.
- Alternative / Failure Flow:
  - `uri` label이 raw path value로 폭증하면 Spring MVC template 기반 URI만 사용하도록 instrumentation을 조정한다.
  - endpoint가 노출되지 않았거나 registry dependency가 없으면 Prometheus scrape 가능성이 없으므로 구현 단계에서 dependency/config를 보강한다.
- Success Outcome:
  - Actor가 Prometheus API로 API별 호출량, 에러율, latency를 조회하고 Loki 로그와 연결해 원인 분석을 진행한다.

# Backlinks
- docs/work-units/observability/api-observability-logs-metrics-20260416-1742/index.md

# Discovery Hints (grep)
- grep -n "^## UC-" docs/product-specs/observability/api-observability-logs-metrics-20260416-1742/use-cases.md
