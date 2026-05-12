# Properties
doc_path: docs/use-case-harvests/observability/api-observability-logs-metrics-20260416-1742/use-case-harvest.md
owner: Codex
status: ready-for-oracle
title: 컨트롤러 기준 전체 API 디버그 로그 및 메트릭 수집
domain: observability
task: api-observability-logs-metrics-20260416-1742
coverage_gate: N/A
non_uc_scope_status: READY
next_step: wait-for-user-approval
last_updated: 2026-04-16:17:47

# Prompt Interpretation
- User Goal
  - 모든 REST 컨트롤러 API를 기준으로 디버그용 로그와 메트릭을 수집한다.
  - 사용자와 Codex 에이전트가 장애, 에러, 비기능 요구사항 대응을 위해 태그와 생성일 기준으로 로그/메트릭을 REST API로 조회할 수 있게 한다.
- Requested Actions
  - SLF4J 기반 에러/디버그/인포 로그 수집 방식을 설계한다.
  - 클라이언트 요청/응답 로그를 모든 API에서 남긴다.
  - 예외와 에러 발생 시 재현과 트레이싱이 가능하도록 상세 정보를 남긴다.
  - Prometheus 기반 메트릭 수집 방식을 설계한다.
  - Prometheus보다 가벼운 메트릭 스택 후보가 있으면 추천한다.
  - 로그 수집 스택을 추천한다.
  - 태그와 생성일 기준 조회는 애플리케이션 자체 조회 API를 새로 만들지 않고, 로그/메트릭 스택이 제공하는 REST API를 우선 사용한다.
- Preferred Implementation Stack
  - Application logging: SLF4J facade + Logback implementation
  - Log format: structured JSON logs with MDC correlation fields
  - Log collection recommendation: Grafana Alloy -> Grafana Loki
  - Metrics library: Micrometer + Spring Boot Actuator
  - Metrics backend: Prometheus
  - Lightweight metrics backend alternative reviewed: VictoriaMetrics single-node, not selected for this work unit
  - Query access: Loki HTTP API and Prometheus HTTP API
- Constraints
  - 현재 단계는 `use-case-harvester`이므로 구현, 테스트 실행, Gradle 수정, `src/**` 수정은 하지 않는다.
  - 조회 API는 애플리케이션에 직접 작성하지 않는다.
  - 요청/응답 로깅은 필수지만, 인증 토큰, 쿠키, 결제 키, 비밀번호, OAuth 값, 파일 본문, 대용량 multipart payload 등 민감하거나 과도한 데이터는 마스킹/생략/길이 제한 정책이 필요하다.
  - Loki/Prometheus 계열 태그는 cardinality 폭증을 피해야 하므로 requestId, traceId, userId, 원본 URL path 변수값은 라벨보다 JSON 필드 또는 exemplar/trace 연계 대상으로 다루는 것이 안전하다.
  - 프로젝트 규칙상 실제 코드 변경 이후 기본 검증 게이트는 `./gradlew build`다.
- Expected Outcome
  - 전체 컨트롤러 API 요청/응답/예외 로그 수집 기준
  - 전체 컨트롤러 API 메트릭 수집 기준
  - 태그와 생성일 범위로 조회 가능한 로그/메트릭 스택 API 사용 방식
  - 경량 운영을 우선한 로그/메트릭 스택 추천안
- Explicit Non-goals
  - 이번 단계에서 구현을 수행하지 않는다.
  - 이번 단계에서 애플리케이션 내부 로그/메트릭 조회 REST API를 만들지 않는다.
  - 이번 단계에서 운영 인프라를 배포하거나 Docker Compose를 수정하지 않는다.
  - 이번 단계에서 oracle 또는 executor 하위 단계로 진행하지 않는다.

# Current Repository Signals
- `build.gradle`에는 `spring-boot-starter-actuator`가 이미 있다.
- `build.gradle`에는 `micrometer-registry-prometheus`가 아직 보이지 않는다.
- `src/main/resources/application.yml`의 Actuator 노출 범위는 `health,info,metrics,caches`이며 `prometheus`는 아직 포함되어 있지 않다.
- `src/main/java/com/omegafrog/My/piano/app/utils/logging/PerformanceLoggingAspect.java`는 일부 컨트롤러/서비스 메서드만 실행 시간을 로깅한다.
- `src/main/java/com/omegafrog/My/piano/app/web/controller/ExceptionAdvisor.java`는 일부 예외를 로깅하지만, 모든 예외가 stack trace와 correlation context를 일관되게 남긴다고 볼 수 없다.
- `src/main/java/com/omegafrog/My/piano/app/web/controller/`에는 다수의 `@RestController`가 있어 컨트롤러별 개별 로깅보다 공통 필터/인터셉터/관측 설정이 적합하다.
- 사용자 후속 결정으로 메트릭 backend는 Prometheus로 확정되었다. VictoriaMetrics는 경량 대안으로 검토했지만 이번 작업에서는 선택하지 않는다.

# Work Item Classification
## UC
- UC-1: 운영자 또는 Codex 에이전트가 기간과 태그로 API 에러 로그를 조회한다.
- UC-2: 운영자 또는 Codex 에이전트가 기간과 태그로 API 요청/응답 로그를 조회한다.
- UC-3: 운영자 또는 Codex 에이전트가 기간과 태그로 API 메트릭을 조회해 성능 저하와 에러율을 분석한다.

## UI
- 없음. Grafana 대시보드는 선택적 운영 편의사항이며, 이번 요청의 필수 조회 방식은 REST API다.

## TECH
- 모든 컨트롤러 API에 공통 요청/응답 로그 수집 지점 추가
- 모든 컨트롤러 API에 공통 메트릭 수집 지점 추가
- 예외/에러 로깅에 correlation id, route, controller, status, exception, elapsed time 등 재현 가능한 필드 포함
- JSON 구조화 로그와 MDC 필드 설계
- Loki/Prometheus 조회 태그 설계
- 로그 수집 스택과 메트릭 수집 스택 선택
- 애플리케이션 자체 조회 API 대신 관측 스택 REST API 조회 방식 문서화

## TEST
- 요청/응답 로그가 정상/예외 경로에서 남는지 검증
- 민감정보 마스킹과 body size 제한 검증
- 컨트롤러별 메트릭 태그와 count/timer가 기록되는지 검증
- Actuator Prometheus endpoint 노출 검증
- Loki/Prometheus query API 예시 검증
- 기존 `./gradlew build` 회귀 검증

## DOC
- 하베스트 문서 작성
- 스택 추천과 REST 조회 방법 문서화
- 운영 태그/필드 표준과 민감정보 로깅 금지 정책 문서화

# Candidate Use Cases
## UC-1: 기간과 태그로 API 에러 로그 조회
- Actor: 운영자, 개발자, Codex 에이전트
- Goal: 특정 생성일 범위와 태그 조건으로 에러 로그를 조회해 장애 원인을 파악한다.
- Trigger: API 5xx 증가, 특정 컨트롤러 장애, 사용자 문의, 배치/이벤트 처리 연계 장애
- Query Provider: Loki HTTP API
- Required Query Dimensions
  - time range: `start`, `end`
  - labels/tags: `app`, `env`, `level`, `controller`, `route`, `method`, `status_class`, `exception`
  - structured fields: `request_id`, `trace_id`, `user_id_hash`, `client_ip_hash`, `elapsed_ms`, `error_message`

## UC-2: 기간과 태그로 API 요청/응답 로그 조회
- Actor: 운영자, 개발자, Codex 에이전트
- Goal: 특정 요청 흐름의 입력, 응답, 상태, 소요 시간을 조회해 재현 정보를 확보한다.
- Trigger: 특정 API 응답 이상, 데이터 오류, 느린 응답, 클라이언트 요청 재현 필요
- Query Provider: Loki HTTP API
- Required Query Dimensions
  - time range: `start`, `end`
  - labels/tags: `app`, `env`, `level`, `controller`, `route`, `method`, `status_class`
  - structured fields: `request_id`, `trace_id`, `request_summary`, `response_summary`, `elapsed_ms`

## UC-3: 기간과 태그로 API 메트릭 조회
- Actor: 운영자, 개발자, Codex 에이전트
- Goal: 특정 기간의 API 응답 시간, 호출량, 에러율을 조회해 비기능 요구사항과 장애를 분석한다.
- Trigger: 응답 시간 SLA/SLO 위반, 4xx/5xx 증가, 캐시/DB/외부 연동 병목 의심
- Query Provider: Prometheus HTTP API
- Required Query Dimensions
  - time range: `start`, `end`, `step`
  - metric labels/tags: `application`, `env`, `controller`, `uri`, `method`, `status`, `outcome`, `exception`

# Confirmed Use Cases
- UC-1
- UC-2
- UC-3

# Non-Use-Case Changes
## UI Changes
- 없음

## Technical Changes
- TECH-1: 전체 `@RestController` API 요청/응답 로깅 공통 지점 설계
  - 후보: `OncePerRequestFilter` 또는 `HandlerInterceptor` + `ContentCachingRequestWrapper`/`ContentCachingResponseWrapper`
  - 컨트롤러/route 태그 확보를 위해 Spring MVC best matching pattern과 handler metadata를 사용한다.
- TECH-2: 예외/에러 로깅 보강
  - `ExceptionAdvisor`와 필터/인터셉터 간 중복 로깅을 피하면서 stack trace, exception type, message, request context, correlation id를 남긴다.
- TECH-3: 구조화 로그 필드 표준 정의
  - 필수 후보: `timestamp`, `level`, `app`, `env`, `request_id`, `trace_id`, `controller`, `route`, `method`, `status`, `status_class`, `elapsed_ms`, `exception`, `message`
  - request/response body는 민감정보 마스킹, 최대 길이 제한, content-type allowlist, multipart 제외 정책을 적용한다.
- TECH-4: 메트릭 수집 보강
  - `micrometer-registry-prometheus` 추가와 `/actuator/prometheus` 노출이 필요하다.
  - Spring Boot Actuator의 HTTP server metrics를 우선 사용하고, 컨트롤러 태그가 부족하면 custom observation convention 또는 meter filter를 검토한다.
- TECH-5: 로그 수집 스택 추천
  - 기본 추천: SLF4J/Logback JSON -> Grafana Alloy -> Loki -> Grafana optional
  - 이유: Loki는 label/time range 기반 HTTP query API가 있고, ELK/OpenSearch보다 로그 전용 운영 부담이 낮다.
  - Promtail은 신규 도입 대상에서 제외한다. 공식 문서상 Promtail은 LTS/EOL 일정이 있으므로 Alloy를 우선한다.
- TECH-6: 메트릭 수집 스택 추천
  - 최종 선택: Micrometer + Actuator + Prometheus
  - 경량 대안 검토 결과: VictoriaMetrics single-node는 Prometheus-compatible backend로 유효하지만 이번 work unit에서는 선택하지 않는다.
  - 판단: Prometheus는 표준성과 Spring Boot 연동성이 강하고, 사용자가 후속 결정으로 Prometheus 사용을 확정했다.
- TECH-7: 스택 제공 REST 조회 방식
  - 로그: Loki `/loki/api/v1/query_range`, `/loki/api/v1/labels`, `/loki/api/v1/label/{name}/values`
  - 메트릭: Prometheus `/api/v1/query_range`, `/api/v1/query`, `/api/v1/labels`, `/api/v1/label/{name}/values`

## Test / Quality Changes
- TEST-1: 정상 API 요청에서 request/response 로그가 남는지 MockMvc 또는 통합 테스트로 검증
- TEST-2: 예외 발생 API에서 error 로그가 stack trace와 correlation context를 포함하는지 검증
- TEST-3: Authorization, Cookie, password, token, payment secret, OAuth secret 등 민감 필드 마스킹 검증
- TEST-4: 긴 body와 multipart/file upload 요청에서 body logging 제한이 동작하는지 검증
- TEST-5: `/actuator/prometheus` endpoint 노출과 `http.server.requests` 계열 metric/tag 존재 검증
- TEST-6: 로그/메트릭 스택 REST query 예시가 태그와 생성일 범위로 동작하는지 로컬 compose 또는 문서화된 수동 검증으로 확인
- TEST-7: `./gradlew build` 실행

## Documentation Changes
- DOC-1: 하베스트 및 work-unit 문서 작성
- DOC-2: 로그/메트릭 필드 및 태그 표준 문서화
- DOC-3: Loki/Prometheus REST 조회 예시 문서화
- DOC-4: 로그 수집 민감정보 마스킹 정책 문서화
- DOC-5: 운영 스택 선택 기준과 경량 대안 비교 문서화

# Recommended Stack
## Logs
- Recommended
  - Application: SLF4J + Logback JSON encoder
  - Collector: Grafana Alloy
  - Backend: Grafana Loki
  - Optional UI: Grafana
- Why
  - Loki는 로그 push/query/tail용 HTTP API와 LogQL을 제공한다.
  - `query_range`는 `start`, `end`, `limit`, `direction`으로 생성일 범위 조회가 가능하다.
  - `labels`와 `label/{name}/values` API로 태그 탐색이 가능하다.
  - 현재 프로젝트의 Elasticsearch는 도메인 검색에 이미 쓰이고 있으므로 로그 저장까지 같은 ES에 얹으면 운영/인덱스/비용 부담과 장애 영향 범위가 커질 수 있다.
- Query Shape
  - Error logs by controller/date:
    - `GET /loki/api/v1/query_range?query={app="mypiano",env="prod",level="ERROR",controller="SheetPostController"}&start=2026-04-16T00:00:00Z&end=2026-04-16T01:00:00Z&limit=100`
  - Request/response logs by route/status/date:
    - `GET /loki/api/v1/query_range?query={app="mypiano",route="/api/v1/sheet-posts",status_class="5xx"}&start=...&end=...`

## Metrics
- Selected
  - Micrometer + Spring Boot Actuator + Prometheus
- Lightweight Alternative Reviewed
  - VictoriaMetrics single-node as a Prometheus-compatible backend, not selected for this work unit
- Why
  - Prometheus is selected because Spring Boot/Micrometer support is direct, widely understood, and matches the user's follow-up decision.
  - VictoriaMetrics remains a possible future replacement if single-node deployment, lower resource use, or longer retention becomes more important.
- Query Shape
  - Error rate by route/status:
    - `GET /api/v1/query_range?query=sum by (uri,status)(rate(http_server_requests_seconds_count{application="mypiano",status=~"5.."}[5m]))&start=2026-04-16T00:00:00Z&end=2026-04-16T01:00:00Z&step=30s`
  - Latency p95 by route:
    - `GET /api/v1/query_range?query=histogram_quantile(0.95,sum by (le,uri,method)(rate(http_server_requests_seconds_bucket{application="mypiano"}[5m])))&start=...&end=...&step=30s`

# Tag and Timestamp Policy
- Required log labels with controlled cardinality
  - `app`, `env`, `level`, `controller`, `route`, `method`, `status_class`, `exception`
- Required log structured fields
  - `created_at` or event timestamp, `request_id`, `trace_id`, `status`, `elapsed_ms`, `request_summary`, `response_summary`, `error_message`, `stack_trace`
- Required metric labels with controlled cardinality
  - `application`, `env`, `controller`, `uri`, `method`, `status`, `outcome`, `exception`
- Not recommended as Loki/Prometheus labels
  - raw `userId`, raw `requestId`, raw `traceId`, raw client IP, raw URL with path variables, request body values
- 생성일 기준 조회
  - 로그는 Loki event timestamp와 `query_range`의 `start`/`end`를 기준으로 조회한다.
  - 메트릭은 Prometheus range query의 `start`/`end`/`step`을 기준으로 조회한다.

# Coverage Mapping
- "컨트롤러를 기준으로 모든 API에 디버그용 로그 작성" -> TECH-1, TECH-2, TECH-3, TEST-1, TEST-2
- "메트릭을 수집" -> TECH-4, TEST-5
- "로그는 slf4j를 사용" -> TECH-3
- "로그 수집 스택을 추천" -> TECH-5, DOC-5
- "메트릭 수집은 prometheus를 사용할 예정" -> TECH-4, TECH-6
- "더 가벼운 스택이 있으면 추천" -> TECH-6, DOC-5
- "태그와 생성일 기준으로 조회" -> TECH-7, TEST-6
- "조회 방법은 rest API" -> TECH-7, DOC-3
- "직접 조회 api를 작성하지 말고 스택 제공 API 사용" -> Explicit Non-goals, TECH-7
- "사용자와 에이전트가 조회해서 비기능적 요구사항이나 에러 대응" -> UC-1, UC-2, UC-3
- "에러/디버그/인포 로그 수집" -> TECH-3, TECH-5
- "클라이언트 요청/응답은 무조건 로깅" -> TECH-1, TEST-1, TEST-3, TEST-4
- "예외나 에러 발생시 자세한 정보" -> TECH-2, TEST-2
- Coverage Gaps
  - request/response body를 어느 수준까지 저장할지 확정이 필요하다. 보안상 "전체 원문 항상 저장"은 위험하며, 요약/마스킹/길이 제한 정책이 필요하다.
  - 운영 환경에서 Loki/Prometheus API를 누가 어떤 인증 방식으로 조회할지 아직 정해지지 않았다.
  - 로그 보존 기간, 메트릭 보존 기간, 비용/스토리지 제한이 아직 정해지지 않았다.

# Coverage Gate
- Ready for Event Storming: N/A
- Why
  - 이번 요청은 도메인 이벤트 흐름보다 횡단 관심사인 관측성 수집/조회 설계에 가깝다.
- Blocking Conditions
  - 없음

# Non-UC Scope Gate
- Ready for Design/Planning: READY
- Why
  - 현재 프로젝트는 Spring Boot, Actuator, SLF4J/Logback 기반이고 컨트롤러/예외 처리 구조가 확인되어 설계 단계로 진행 가능하다.
  - 조회 API는 애플리케이션 개발이 아니라 Loki/Prometheus 계열 API 사용으로 방향이 명확하다.
- Blocking Conditions
  - 없음

# Stack Profile Readiness
- stack_profile_path: .codex/stack-profile.yaml
- stack_profile_status: READY
- stack_profile_source: existing
- stack_profile_updated: NO
- asked_user_for_stack: NO
- required_fields_present:
  - stack.language
  - stack.framework
  - stack.runtime
  - stack.build_tool
  - testing.commands.unit_integration
  - api.style
- blocking_fields:
  - 없음

# Blocking Unknowns
- 요청/응답 로그에서 body 원문 저장을 허용할지, 아니면 metadata + masked summary로 제한할지 결정해야 한다.
- 민감정보 마스킹 대상 필드와 content-type allowlist를 확정해야 한다.
- Loki/Prometheus API 접근 인증/네트워크 경계가 정해지지 않았다.
- 로그/메트릭 보존 기간과 예상 트래픽/스토리지 예산이 정해지지 않았다.
- 컨트롤러 태그를 metric label로 반드시 요구할지, route/uri/method/status 중심으로 충분한지 판단해야 한다.

# Needs Review
- 기존 `PerformanceLoggingAspect`를 유지/확장할지, 요청/응답 로깅과 중복되므로 공통 필터 기반으로 대체할지
- `ExceptionAdvisor` 로깅과 공통 필터 로깅 간 중복 에러 로그를 어떻게 방지할지
- 현재 `application.yml`의 Actuator exposure에 `prometheus`를 추가할 때 운영 보안 설정이 필요한지
- 로컬/운영 Docker Compose 또는 배포 스크립트에 Alloy/Loki/Prometheus를 어느 범위까지 포함할지

# External References Checked
- Spring Boot Actuator metrics reference: https://docs.spring.io/spring-boot/reference/actuator/metrics.html
- Prometheus HTTP API reference: https://prometheus.io/docs/prometheus/latest/querying/api/
- Grafana Loki HTTP API reference: https://grafana.com/docs/loki/latest/api/
- Grafana Promtail deprecation notice: https://grafana.com/docs/loki/latest/send-data/promtail/stages/docker/
- Grafana Alloy structured logs guide: https://grafana.com/docs/alloy/latest/monitor/monitor-structured-logs/
- VictoriaMetrics single-server documentation: https://docs.victoriametrics.com/victoriametrics/single-server-victoriametrics/

# Rejected Use Cases
- 애플리케이션 내부에 로그/메트릭 조회 API를 새로 작성한다.
  - Reason: 사용자가 명시적으로 스택이 제공하는 REST API를 최대한 사용하라고 요청했다.

# Missing-but-Plausible Use Cases
- 알림/경보 자동화
  - 현재 요청은 조회 가능성과 수집에 집중되어 있으며 alert rule 정의는 명시되지 않았다.
- 분산 트레이싱
  - trace id/log correlation은 필요하지만 OpenTelemetry trace backend 도입은 명시되지 않았다.
- Grafana dashboard 구성
  - 선택적 편의사항이며, 필수 요구는 REST API 조회다.

# Next Revision Focus
- request/response body 로깅 정책을 보안 기준으로 확정한다.
- Prometheus 기준 수집/조회 설정을 구체화한다.
- 로그 label과 JSON field 경계를 cardinality 기준으로 확정한다.
- Loki/Prometheus API 접근 인증과 운영 노출 방식을 확정한다.

# Oracle Handoff
- Allowed To Proceed: NO
- Confirmed Use Cases for Oracle
  - UC-1
  - UC-2
  - UC-3
- Non-Use-Case Changes for Oracle
  - TECH-1
  - TECH-2
  - TECH-3
  - TECH-4
  - TECH-5
  - TECH-6
  - TECH-7
  - TEST-1
  - TEST-2
  - TEST-3
  - TEST-4
  - TEST-5
  - TEST-6
  - TEST-7
  - DOC-1
  - DOC-2
  - DOC-3
  - DOC-4
  - DOC-5
- Assumptions Forbidden for Oracle
  - 요청/응답 body 원문 전체를 항상 저장해도 된다고 단정하면 안 된다.
  - requestId, traceId, userId 같은 고카디널리티 값을 Loki/Prometheus label로 올려도 된다고 단정하면 안 된다.
  - VictoriaMetrics를 이번 work unit의 최종 메트릭 backend로 설계하면 안 된다. 사용자는 후속 결정으로 Prometheus 사용을 확정했다.
  - 애플리케이션 내부 조회 API를 만드는 방향으로 설계하면 안 된다.
  - 기존 `PerformanceLoggingAspect`만 확장하면 전체 API 요청/응답/예외 로깅 요구를 충족한다고 단정하면 안 된다.
- User Approval Required Before Orchestration: YES

# Backlinks
- docs/work-units/observability/api-observability-logs-metrics-20260416-1742/index.md
