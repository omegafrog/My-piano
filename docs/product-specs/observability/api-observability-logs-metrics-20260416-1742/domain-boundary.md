# Properties
doc_path: docs/product-specs/observability/api-observability-logs-metrics-20260416-1742/domain-boundary.md
owner: Codex
status: completed
title: 컨트롤러 기준 전체 API 디버그 로그 및 메트릭 수집
domain: observability
task: api-observability-logs-metrics-20260416-1742
last_updated: 2026-04-16:17:52

# Scope
- 모든 `@RestController` API 요청, 응답, 예외 흐름에 대해 운영자가 재현 가능한 구조화 로그를 확보할 수 있게 한다.
- Micrometer와 Spring Boot Actuator 기반 HTTP API 메트릭을 Prometheus로 수집할 수 있게 한다.
- 사용자와 Codex 에이전트는 애플리케이션 내부 조회 API가 아니라 Loki HTTP API와 Prometheus HTTP API로 기간 및 태그 조건 조회를 수행한다.
- 로그 수집 권장 경로는 `SLF4J/Logback JSON -> Grafana Alloy -> Loki`로 확정한다.

# External Boundaries
- Application logging boundary
  - 애플리케이션은 SLF4J facade와 Logback implementation으로 구조화 로그를 stdout/file 등 운영 수집 대상에 남긴다.
  - Logback JSON encoder 도입은 허용하지만, 로그 조회 저장소를 애플리케이션 DB나 Elasticsearch 도메인 검색 인덱스로 겸용하지 않는다.
- Log collection/query boundary
  - Grafana Alloy가 애플리케이션 로그를 수집해 Loki로 전송한다.
  - 로그 조회는 Loki `/loki/api/v1/query_range`, `/loki/api/v1/labels`, `/loki/api/v1/label/{name}/values`를 사용한다.
- Metrics collection/query boundary
  - 애플리케이션은 Micrometer와 Actuator를 통해 Prometheus scrape endpoint를 노출한다.
  - 메트릭 backend는 Prometheus이며, VictoriaMetrics는 이번 work unit의 선택지가 아니다.
  - 메트릭 조회는 Prometheus `/api/v1/query_range`, `/api/v1/query`, `/api/v1/labels`, `/api/v1/label/{name}/values`를 사용한다.
- API controller boundary
  - 컨트롤러별 개별 코드보다 공통 filter/interceptor/observation 설정으로 전체 `/api/v1/**` 계열 REST API를 다룬다.
  - 기존 `ExceptionAdvisor`와 중복 에러 로그를 만들지 않도록 책임 분리를 설계한다.

# In Scope
- 전체 컨트롤러 API request/response 로그 필드 표준 정의
- 정상, 4xx, 5xx, exception 흐름의 correlation context 정의
- body logging 보안 정책 정의
  - masking
  - maximum captured size
  - content-type allowlist
  - multipart/file payload exclusion
- Loki label과 JSON field 경계 정의
- Prometheus metric label 경계 정의
- Prometheus endpoint 노출과 `http.server.requests` 계열 metric 수집 설계
- Loki/Prometheus REST query 예시와 기간/태그 조회 기준 정의
- `./gradlew build`를 포함한 구현 후 검증 기대사항 정의

# Out of Scope
- 애플리케이션 소유 로그/메트릭 조회 REST API 신규 작성
- VictoriaMetrics를 최종 메트릭 backend로 설계하거나 운영 기본값으로 채택
- Promtail 신규 도입
- Grafana dashboard 구현
- alert rule, on-call 정책, SLO 문서화
- OpenTelemetry tracing backend 도입
- 운영 인프라 배포, Docker Compose, Jenkins, CodeDeploy 스크립트 변경의 구체 구현
- 요청/응답 body 원문 전체를 무조건 저장하는 정책

# Key Constraints
- requestId, traceId, raw userId, raw client IP, raw URL path variable, request/response body value는 Loki/Prometheus label로 사용하지 않는다.
- Loki labels는 `app`, `env`, `level`, `controller`, `route`, `method`, `status_class`, 제한된 `exception`처럼 cardinality가 통제되는 값으로 제한한다.
- Prometheus labels는 `application`, `env`, `controller`, `uri`, `method`, `status`, `outcome`, 제한된 `exception`처럼 Micrometer 권장 범위에 맞춘다.
- route는 실제 raw path가 아니라 Spring MVC best matching pattern 또는 안정적인 URI template을 사용한다.
- body logging은 JSON 등 허용된 content type에만 적용하고 multipart/file upload는 body를 기록하지 않는다.
- Authorization, Cookie, password, token, payment secret, OAuth secret, 개인식별성 높은 값은 마스킹하거나 해시 처리한다.
- repository-level implementation verification은 `./gradlew build`를 포함해야 한다.

# Backlinks
- docs/work-units/observability/api-observability-logs-metrics-20260416-1742/index.md

# Discovery Hints (grep)
- grep -n "^# Scope" docs/product-specs/observability/api-observability-logs-metrics-20260416-1742/domain-boundary.md
