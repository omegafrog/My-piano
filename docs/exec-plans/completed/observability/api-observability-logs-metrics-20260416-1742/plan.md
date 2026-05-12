# Properties
doc_path: docs/exec-plans/completed/observability/api-observability-logs-metrics-20260416-1742/plan.md
status: completed
title: 컨트롤러 기준 전체 API 디버그 로그 및 메트릭 수집
domain: observability
task: api-observability-logs-metrics-20260416-1742
source_use_case_harvest: docs/use-case-harvests/observability/api-observability-logs-metrics-20260416-1742/use-case-harvest.md
last_updated: 2026-04-16:19:11

# Discovery Hints (grep)
- grep -R "^# Properties" docs/use-case-harvests/observability/api-observability-logs-metrics-20260416-1742/
- grep -R "^# Properties" docs/product-specs/observability/api-observability-logs-metrics-20260416-1742/
- grep -R "^# Properties" docs/design-docs/observability/api-observability-logs-metrics-20260416-1742/
- grep -R "^# Properties" docs/exec-plans/completed/observability/api-observability-logs-metrics-20260416-1742/

# Purpose / Big Picture
- 전체 REST controller API에 공통 request/response/error observability를 적용한다.
- 로그는 SLF4J/Logback 구조화 JSON으로 출력하고 Grafana Alloy를 통해 Loki에서 조회한다.
- 메트릭은 Micrometer/Actuator를 통해 Prometheus에서 조회한다.
- Loki/Prometheus REST API를 사용하며 애플리케이션 내부 로그/메트릭 조회 API는 만들지 않는다.

# Progress
- [x] DOC oracle canonical planning/design docs 작성
- [x] UC Loki API로 기간과 태그 기준 API error log 조회 가능성 확보
- [x] UC Loki API로 기간과 태그 기준 API request/response log 조회 가능성 확보
- [x] UC Prometheus API로 기간과 태그 기준 API metric 조회 가능성 확보
- [x] UI application UI 변경 없음 확인
- [x] TECH 전체 controller API 공통 request/response logging entrypoint 구현
- [x] TECH exception logging correlation context와 duplicate logging 방지 구현
- [x] TECH body masking, size limit, content-type allowlist, multipart/file exclusion 구현
- [x] TECH SLF4J/Logback structured JSON log output 구성
- [x] TECH Grafana Alloy -> Loki 로그 수집 운영 경로 문서화 또는 설정 범위 확정
- [x] TECH Micrometer Prometheus registry와 `/actuator/prometheus` 노출 구성
- [x] TECH Prometheus-safe metric labels와 Loki-safe log labels 적용
- [x] TEST 정상 request/response log coverage 검증
- [x] TEST exception/error log stack trace 및 correlation context 검증
- [x] TEST sensitive value masking, body limit, multipart/file exclusion 검증
- [x] TEST Prometheus endpoint와 HTTP metric label 검증
- [x] TEST Loki/Prometheus REST query 예시 검증 또는 수동 검증 절차 기록
- [x] TEST `./gradlew build` 실행 및 결과 기록
- [x] DOC execution log, test gate, verification reports를 실제 결과로 동기화

# Surprises & Discoveries
- `spring-boot-starter-actuator`는 이미 dependency에 있다.
- executor stage에서 `micrometer-registry-prometheus` dependency와 `/actuator/prometheus` exposure가 추가됐다.
- 기존 `PerformanceLoggingAspect`는 일부 controller/service pointcut만 대상으로 하므로 전체 API request/response logging 요구를 충족하지 않는다.
- `ExceptionAdvisor`는 중앙 예외 응답 경계지만 일부 log call은 stack trace와 correlation context가 일관되지 않다.
- 2026-04-16 executor stage에서 Spring MVC API 요청만 대상으로 하는 `OncePerRequestFilter`를 추가했다. `/actuator/**`는 request body logging 대상이 아니며 Prometheus scrape는 actuator endpoint config로 노출한다.
- `ExceptionAdvisor`가 의도 상태값을 request attribute로 남겨 기존 응답 계약을 크게 바꾸지 않으면서 completion log에는 4xx/5xx status class가 기록되도록 했다.
- Grafana Alloy/Loki/Prometheus 서버 구성은 애플리케이션 repository 변경 범위 밖으로 남겼고, 이번 구현은 JSON stdout log와 `/actuator/prometheus` scrape 가능성까지를 애플리케이션 책임으로 확정했다.
- 최종 test_gate rerun에서 `timeout 1200s ./gradlew --no-daemon build --stacktrace --console=plain`과 `timeout 1200s ./gradlew --no-daemon test --stacktrace --console=plain`이 모두 PASS했다.
- earlier `BLOCKED` gate는 구현 실패가 아니라 짧은 대기 시간과 sandbox Gradle socket/IP 제한으로 발생했다.
- 실제 Loki `/loki/api/v1/query_range` 및 Prometheus `/api/v1/query_range` 호출은 collector/backend 접근 정보가 repository 밖에 있어 운영 후속 검증으로 남겼다.

# Decision Log
- 메트릭 backend는 Prometheus로 확정한다.
- VictoriaMetrics는 경량 대안으로만 기록하고 이번 work unit의 backend로 설계하지 않는다.
- 로그 수집 권장 스택은 `SLF4J/Logback JSON -> Grafana Alloy -> Loki`다.
- Promtail은 신규 도입 권장 대상에서 제외한다.
- 조회는 Loki/Prometheus REST API로 수행하며 애플리케이션 내부 조회 API를 만들지 않는다.
- requestId, traceId, raw userId, raw path value, raw IP, body value는 labels로 쓰지 않는다.

# Context and Orientation
- 관련 application code:
  - `src/main/java/com/omegafrog/My/piano/app/web/controller/`
  - `src/main/java/com/omegafrog/My/piano/app/web/controller/ExceptionAdvisor.java`
  - `src/main/java/com/omegafrog/My/piano/app/utils/logging/PerformanceLoggingAspect.java`
  - `src/main/java/com/omegafrog/My/piano/app/cache/EhcacheMetricsBinder.java`
- 관련 configuration:
  - `build.gradle`
  - `src/main/resources/application.yml`
- 검증 기준:
  - 최종 repository gate는 `./gradlew build`

# Plan of Work
- 공통 request lifecycle instrumentation을 추가해 전체 controller API coverage를 확보한다.
- Logback structured output과 MDC correlation fields를 표준화한다.
- request/response body capture는 보안 제한이 있는 summary-only 정책으로 구현한다.
- Prometheus registry와 actuator endpoint를 구성하고 safe label만 사용한다.
- Loki/Prometheus query examples를 실제 구현 결과에 맞춰 검증한다.

# Concrete Steps
1. `OncePerRequestFilter` 또는 `HandlerInterceptor` 기반 request/response logging component를 추가한다.
2. Spring MVC best matching pattern에서 stable route value를 얻고 MDC에 request context를 설정/정리한다.
3. request/response summary builder에 masking, size limit, content-type allowlist, multipart/file exclusion을 구현한다.
4. `ExceptionAdvisor`와 공통 logging component의 error logging 책임을 정리해 중복 error logs를 방지한다.
5. Logback JSON output dependency/config를 추가한다.
6. Prometheus registry dependency와 `/actuator/prometheus` exposure를 추가한다.
7. HTTP server metrics label이 high-cardinality 값을 포함하지 않는지 확인한다.
8. 정상/예외/masking/multipart/metrics 테스트를 추가한다.
9. Loki/Prometheus REST query 예시를 구현 결과 기준으로 검증하거나 수동 검증 절차로 기록한다.
10. `./gradlew build`를 실행하고 결과를 verification docs에 기록한다.

# Validation and Acceptance
- 모든 controller API가 공통 request/response logging path를 통과한다.
- error logs는 stack trace와 correlation context를 포함한다.
- body logging은 masking, size limit, content-type allowlist, multipart/file exclusion을 지킨다.
- Loki labels와 Prometheus labels에 high-cardinality values가 들어가지 않는다.
- Prometheus backend 기준 `/actuator/prometheus` scrape endpoint가 검증된다.
- Loki/Prometheus REST API query examples가 기간과 태그 기준 조회 요구를 설명하거나 검증한다.
- `./gradlew build` 결과가 기록된다.

# Idempotence and Recovery
- logging and metrics instrumentation은 request 처리 결과를 바꾸지 않아야 한다.
- observability backend 장애 또는 collector 장애가 API transaction 성공/실패를 바꾸지 않아야 한다.
- MDC는 request completion 또는 exception path에서 반드시 clear되어 thread reuse contamination을 막아야 한다.
- body capture 실패 시 request 자체를 실패시키지 않고 omission reason만 기록한다.

# Documentation Impact
- executor 후 `implementation-log.md`에 실제 구현 파일과 설정 변경을 기록한다.
- executor 진입 전 canonical execution-doc placeholders를 유지해 missing-file gap을 없앤다.
- `test-gate.md`에 focused tests, query verification, `./gradlew build` 결과를 기록한다.
- `doc-verify-after-execute.md`와 `closure.md`는 executor/test-gate 이후 실제 결과로 갱신한다.
- 운영 배포 설정이 범위 밖이면 Alloy/Loki/Prometheus deployment remains follow-up으로 명확히 남긴다.

# Change Log
- 2026-04-16 17:52 KST: oracle planning/design docs 작성. Prometheus backend, Grafana Alloy -> Loki log stack, no application-owned query API, strict body logging and label cardinality policies를 확정했다.
- 2026-04-16 17:59 KST: doc_writer stage에서 canonical execution/verification placeholder docs를 추가하고 work-unit hub를 execution-ready 상태로 정렬했다.
- 2026-04-16 18:21 KST: executor stage에서 공통 API request/response/error structured logging, body summary/masking 정책, Logback JSON config, Prometheus registry/exposure config, focused logging/metrics tests를 구현했다. 해당 시점에는 full build/test gate와 실제 Loki/Prometheus REST query 검증을 후속 test_gate 범위로 남겼다.
- 2026-04-16 18:59 KST: execute_writer stage에서 최종 test_gate PASS 결과를 반영했다. `./gradlew build`와 `./gradlew test`는 20분 timeout으로 재실행해 통과했고, Loki/Prometheus backend REST query execution은 repository 외부 운영 접근 범위라 후속 검증으로 남겼다.
- 2026-04-16 19:00 KST: post-execute doc_verify에서 `logback-spring.xml`이 repository-wide `*.xml` ignore rule에 가려지는 traceability gap을 발견했다. `.gitignore`에 `/src/main/resources/logback-spring.xml` 예외를 추가해 정상 git workflow에 포함되도록 보정했다.
- 2026-04-16 19:11 KST: closer stage에서 execution docs를 `docs/exec-plans/completed/observability/api-observability-logs-metrics-20260416-1742/`로 이동하고 closure verdict를 `COMPLETED`로 확정했다.

# Backlinks
- docs/work-units/observability/api-observability-logs-metrics-20260416-1742/index.md
