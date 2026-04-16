# Properties
doc_path: docs/use-case-harvests/cache/ehcache-audit-20260415-1336/use-case-harvest.md
owner: Codex
status: ready-for-oracle
title: Ehcache 캐시 검증 및 비판적 분석
domain: cache
task: ehcache-audit-20260415-1336
coverage_gate: N/A
non_uc_scope_status: READY
next_step: wait-for-user-approval
last_updated: 2026-04-15:13:52

# Prompt Interpretation
- User Goal
  - Ehcache 기반 캐시 적용 전/후의 성능 향상을 검증할 수 있는 테스트가 있는지 확인하고, 있으면 실행 결과를 확인한다.
  - 없으면 새 테스트 또는 벤치마크를 추가한다.
  - 캐시 적용 코드를 비판적으로 분석해 문제점과 개선점을 찾는다.
- Requested Actions
  - 기존 테스트/벤치마크/문서를 확인한다.
  - 전후 비교가 가능한 검증 수단을 실행하거나 새로 만든다.
  - 캐시 구현을 리뷰하고 리스크를 정리한다.
- Preferred Implementation Stack
  - Java 17
  - Spring Boot
  - Gradle
  - JUnit 5 / Spring Boot Test
- Constraints
  - 기존 더러운 워킹트리를 되돌리지 않는다.
  - 관련 없는 사용자 수정은 건드리지 않는다.
  - 프로젝트 규칙상 코드 변경 후 `./gradlew build`를 기준 검증으로 본다.
- Expected Outcome
  - 현재 존재하는 ehcache 검증 수단 목록
  - 실제 실행 결과
  - 부족한 성능 검증 공백을 메우는 신규 테스트/벤치마크
  - 코드 레벨 개선 포인트
- Explicit Non-goals
  - 새로운 사용자 기능 설계
  - 오케스트레이션 워크플로우 실행

# Work Item Classification
## UC
- 없음. 이번 요청은 기능 사용 흐름 추가가 아니라 검증/품질 분석 작업이다.

## UI
- 없음.

## TECH
- EHCache + SWR + single-flight + warm-up 적용 코드 분석
- 캐시 키, TTL, 무효화, 메트릭스, 동시성 처리의 리스크 검토

## TEST
- 기존 단위/통합/성능 검증 수단 확인
- 관련 테스트 실행
- 캐시 적용 전/후 비교 공백이 있으면 신규 검증 추가

## DOC
- 하베스트 문서 작성
- 결과 요약 및 리스크 문서화

# Candidate Use Cases
- 없음

# Confirmed Use Cases
- 없음

# Non-Use-Case Changes
## UI Changes
- 없음

## Technical Changes
- TECH-1: SheetPost EHCache 적용 코드의 구조/리스크 분석

## Test / Quality Changes
- TEST-1: 기존 EHCache 관련 단위/통합 테스트와 벤치마크 스크립트 조사
- TEST-2: 기존 검증 수단 실행 및 결과 확인
- TEST-3: 전/후 성능 검증 공백이 확인되면 신규 테스트 또는 벤치마크 추가

## Documentation Changes
- DOC-1: 하베스트 및 work-unit 문서 작성
- DOC-2: 최종 결과와 개선 포인트 정리

# Coverage Mapping
- "캐싱을 진행하기 전/후로 정성적 성능 향상을 위한 테스트가 있는지 확인" -> TEST-1
- "있으면 실행해서 결과를 확인" -> TEST-2
- "없으면 새로 생성" -> TEST-3
- "캐시를 적용한 코드를 비판적으로 분석" -> TECH-1
- Coverage Gaps
  - 없음. 요청은 모두 Non-UC 범위로 매핑 가능하다.

# Coverage Gate
- Ready for Event Storming: N/A
- Why
  - 사용자 기능 유스케이스가 아니라 품질/분석 작업이다.
- Blocking Conditions
  - 없음

# Non-UC Scope Gate
- Ready for Design/Planning: READY
- Why
  - 대상 구현과 테스트 위치가 확인되었고, 실행/보강 범위가 구체적이다.
- Blocking Conditions
  - 없음

# Stack Profile Readiness
- stack_profile_path: .codex/stack-profile.yaml
- stack_profile_status: READY
- stack_profile_source: existing
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
- 실제 런타임 성능 비교를 위한 로컬 데이터셋/인프라가 현재 준비되어 있는지는 아직 미확인
- `k6`는 설치되어 있지만 벤치마크 대상 서버(`localhost:8080`)는 현재 미기동

# Needs Review
- 자동화된 검증이 "정합성" 중심인지, 실제 "성능 향상" 비교까지 포함하는지
- 캐시 무효화 범위와 개인화 필드 처리 적절성
- ES 검색 결과 순서가 캐시 적재 과정에서 보존되는지
- count 캐시가 재시작/eviction 상황에서도 안전한지

# Execution Notes
- 기존 검증 수단 확인
  - 단위/통합: `EhcacheSmokeTest`, `EhcacheTtlContractTest`, `JCacheCacheManagerOverrideIntegrationTest`, `SheetPostSWRSingleFlightServiceTest`, `SheetPostCacheWarmupServiceTest`, `SheetPostCacheWarmupJobIntegrationTest`
  - 수동 벤치마크: `scripts/search-benchmark.js`, `docs/performance/search-benchmark.md`
- 공백 확인
  - 기존 자동화 테스트는 캐시 정합성/SWR/워밍업 중심이었고, 동일 요청의 cold/warm 전후 체감 차이를 직접 보여주는 테스트는 없었다.
- 신규 보강
  - `src/test/java/com/omegafrog/My/piano/app/cache/SheetPostCachePerformanceCharacterizationTest.java` 추가
  - 동일 목록 요청 2회에서 warm cache가 backend 호출을 재사용하고 응답 시간을 줄이는지 특성화한다.
- 실행 결과
  - `./gradlew --no-daemon test --tests com.omegafrog.My.piano.app.cache.SheetPostCachePerformanceCharacterizationTest --console=plain` 성공
  - `./gradlew --no-daemon test --tests com.omegafrog.My.piano.app.cache.EhcacheSmokeTest --tests com.omegafrog.My.piano.app.cache.EhcacheTtlContractTest --tests com.omegafrog.My.piano.app.cache.SheetPostSWRSingleFlightServiceTest --console=plain` 성공
  - `k6` 바이너리는 설치되어 있으나, `localhost:8080` 서버가 떠 있지 않아 수동 벤치마크는 이번 세션에서 실행하지 못했다.
- 코드 리뷰 주요 메모
  - ES 결과 순서와 `findByIds()` 반환 순서 불일치 가능성
  - 상세 DTO의 `likePost`가 사용자 상태를 다시 계산하지 않고 `false`로 고정됨
  - count 캐시는 eviction/restart 시 마지막 배치 이전 증분 손실 가능성이 큼
  - `EhcacheMetricsBinder`는 대용량 캐시 전체를 주기적으로 순회해 비용이 크다

# Rejected Use Cases
- 없음

# Missing-but-Plausible Use Cases
- 없음

# Next Revision Focus
- 기존 테스트를 정합성 검증과 성능 검증으로 분리 정리
- 실제 성능 전후 비교용 자동화가 없으면 최소 재현 가능한 회귀 테스트 또는 문서화된 벤치마크 추가

# Oracle Handoff
- Allowed To Proceed: NO
- Confirmed Use Cases for Oracle
  - 없음
- Non-Use-Case Changes for Oracle
  - TECH-1
  - TEST-1
  - TEST-2
  - TEST-3
  - DOC-1
  - DOC-2
- Assumptions Forbidden for Oracle
  - 실제 벤치마크 결과가 이미 존재한다고 가정하면 안 된다.
  - 현재 구현이 문서 스펙과 일치한다고 가정하면 안 된다.
- User Approval Required Before Orchestration: YES

# Backlinks
- docs/work-units/cache/ehcache-audit-20260415-1336/index.md
