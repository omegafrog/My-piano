# Properties
doc_path: docs/use-case-harvests/cache/cache-performance-quantification-20260416-0933/use-case-harvest.md
owner: Codex
status: ready-for-oracle
title: 캐시 적용 전후 성능 향상 정량 비교 테스트
domain: cache
task: cache-performance-quantification-20260416-0933
coverage_gate: N/A
non_uc_scope_status: READY
next_step: wait-for-user-approval
last_updated: 2026-04-16:09:33

# Prompt Interpretation
- User Goal
  - 캐시 적용으로 얻은 성능 향상을 정량적으로 확인할 수 있는 테스트를 작성하고 실행해서 전후 이득을 비교한다.
- Requested Actions
  - 캐시 성능 향상을 수치로 보여줄 수 있는 테스트 또는 벤치마크를 준비한다.
  - 해당 검증을 실제로 실행한다.
  - 캐시 적용 전/후의 이득을 비교 가능한 형태로 정리한다.
- Preferred Implementation Stack
  - Java 17
  - Spring Boot
  - Gradle
  - JUnit 5 / Spring Boot Test
  - 필요 시 기존 `k6` 스크립트 기반 벤치마크
- Constraints
  - 현재 단계는 `use-case-harvester`이므로 구현, 테스트 실행, 결과 생성은 하지 않는다.
  - 하베스터는 하베스트 문서와 work-unit 인덱스만 수정한다.
  - 프로젝트 규칙상 실제 코드 변경 이후 기본 검증 게이트는 `./gradlew build`다.
- Expected Outcome
  - 재현 가능한 캐시 성능 비교 전략
  - 작성/보강 대상 테스트 범위
  - 실행 시 확보해야 할 비교 지표와 결과 정리 방식
- Explicit Non-goals
  - 이번 단계에서 캐시 구현 자체를 리팩터링하거나 정책을 바꾸지 않는다.
  - 이번 단계에서 오케스트레이션 하위 단계를 시작하지 않는다.

# Work Item Classification
## UC
- 없음. 사용자 기능 흐름 추가가 아니라 내부 성능 검증과 품질 측정 작업이다.

## UI
- 없음.

## TECH
- 캐시 on/off 또는 cold/warm 비교를 동일 조건에서 재현할 수 있는 측정 제어점 정의
- 기존 캐시 자산과 서비스 경로를 기준으로 비교 기준선 결정

## TEST
- 캐시 전/후 성능 향상을 수치화하는 자동화 테스트 또는 벤치마크 작성/보강
- 선택한 검증 수단 실행
- 전/후 결과 수집 및 비교 기준 정리

## DOC
- 하베스트 문서 작성
- 성능 비교 결과 보고 형식 정리

# Candidate Use Cases
- 없음

# Confirmed Use Cases
- 없음

# Non-Use-Case Changes
## UI Changes
- 없음

## Technical Changes
- TECH-1: 동일 요청 기준으로 cache enabled/disabled 또는 cold/warm 상태를 통제할 수 있는 비교 전략 정의
- TECH-2: 기존 캐시 성능 검증 자산과 실제 서비스 경로 간 연결 지점 점검

## Test / Quality Changes
- TEST-1: 기존 `SheetPostCachePerformanceCharacterizationTest`와 `scripts/search-benchmark.js`를 기준으로 정량 비교가 가능한 테스트 범위 확정
- TEST-2: 캐시 전/후 이득을 재현 가능하게 보여주는 자동화 테스트 또는 벤치마크 작성/보강
- TEST-3: 선택한 검증 수단 실행 및 전/후 지표 수집
- TEST-4: 응답시간 또는 비율 기반으로 before/after 이득 비교 결과 정리

## Documentation Changes
- DOC-1: 하베스트 및 work-unit 문서 작성
- DOC-2: 실행 결과를 기록할 비교 보고 형식 정리

# Coverage Mapping
- "캐시를 적용해서 얻은 성능 향상을 정량적으로 평가" -> TECH-1, TEST-1, TEST-4
- "테스트를 작성" -> TEST-2
- "실행해서" -> TEST-3
- "전후 이득을 비교" -> TEST-4, DOC-2
- Coverage Gaps
  - 현재 저장소에는 `src/test/java/com/omegafrog/My/piano/app/cache/SheetPostCachePerformanceCharacterizationTest.java`와 `scripts/search-benchmark.js`가 이미 존재하므로, 새 작업은 완전 신규 작성이 아니라 비교 기준 정제와 실행 자동화 보강이 될 가능성이 높다.
  - 현 시점 문서만으로는 "전/후"의 기준이 cold/warm 비교인지, cache disabled/enabled 비교인지, 혹은 캐시 도입 이전 커밋 비교인지 확정되지 않았다.

# Coverage Gate
- Ready for Event Storming: N/A
- Why
  - 이번 요청은 기능 유스케이스가 아니라 성능 측정과 검증 자동화 작업이다.
- Blocking Conditions
  - 없음

# Non-UC Scope Gate
- Ready for Design/Planning: READY
- Why
  - 관련 테스트, 수동 벤치마크 스크립트, 캐시 스펙 문서가 이미 존재해 비교 전략과 실행 계획을 구체화할 수 있다.
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
- 정량 비교의 기준선을 cold/warm으로 둘지, cache disabled/enabled로 둘지, 혹은 과거 커밋 대비로 둘지 확정되지 않았다.
- API 수준 벤치마크를 위해 필요한 로컬 서버/Redis/ES/Kafka 기동 범위를 어느 수준까지 요구할지 아직 결정되지 않았다.
- 최종 산출물을 단위 특성화 테스트 중심으로 둘지, `k6` 기반 실경로 벤치마크까지 포함할지 선택이 필요하다.

# Needs Review
- 기존 `SheetPostCachePerformanceCharacterizationTest`가 mocked latency 기반 특성화 테스트에 머물러 있어, 실제 실행 결과 보고에 충분한지
- `docs/performance/search-benchmark.md`의 cold/warm 가이드가 현재 캐시 구현과 동일한 전후 비교 정의로 쓰일 수 있는지
- 전/후 이득 판단 기준을 절대 시간(ms)인지 개선 비율(배수/퍼센트)인지로 통일할지

# Rejected Use Cases
- 없음

# Missing-but-Plausible Use Cases
- 없음

# Next Revision Focus
- "전/후" 기준을 하나로 고정하고 자동화/수동 벤치마크 역할을 분리한다.
- 기존 캐시 특성화 테스트 재사용 여부와 추가 실행 환경 요구사항을 명확히 적는다.

# Oracle Handoff
- Allowed To Proceed: NO
- Confirmed Use Cases for Oracle
  - 없음
- Non-Use-Case Changes for Oracle
  - TECH-1
  - TECH-2
  - TEST-1
  - TEST-2
  - TEST-3
  - TEST-4
  - DOC-1
  - DOC-2
- Assumptions Forbidden for Oracle
  - `SheetPostCachePerformanceCharacterizationTest`만으로 사용자가 원하는 전/후 비교 보고가 충분하다고 단정하면 안 된다.
  - "전/후"를 cold/warm, enabled/disabled, 과거 커밋 대비 중 하나로 임의 선택해 진행하면 안 된다.
  - 로컬 벤치마크 실행에 필요한 인프라가 항상 떠 있다고 가정하면 안 된다.
- User Approval Required Before Orchestration: YES

# Backlinks
- docs/work-units/cache/cache-performance-quantification-20260416-0933/index.md
