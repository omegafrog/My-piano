# Properties
doc_path: docs/use-case-harvests/test/build-regression-fixes-20260415-1820/use-case-harvest.md
owner: Codex
status: ready-for-oracle
title: build 회귀 실패 테스트와 bootRun 미검증 경로 정리
domain: test
task: build-regression-fixes-20260415-1820
coverage_gate: N/A
non_uc_scope_status: READY
next_step: wait-for-user-approval
last_updated: 2026-04-15:18:20

# Prompt Interpretation
- User Goal
  - `./gradlew build --console=plain`에서 관측된 실패 테스트들을 해소하고, 아직 확인하지 못한 `./gradlew bootRun` 기동 경로까지 검증 가능 상태로 만든다.
- Requested Actions
  - `use-case-harvester` 규칙에 맞춰 이번 작업을 새 work unit으로 문서화한다.
  - 실패로 언급된 `SheetPostCacheWarmupJobIntegrationTest`, 여러 `SecurityControllerTest`, 여러 `CommonUserServiceTest`의 원인을 찾고 수정한다.
  - 수정 후 `build`와 가능하면 `bootRun`을 다시 검증한다.
- Preferred Implementation Stack
  - Java 17
  - Spring Boot 3.2.x
  - Gradle
- Constraints
  - 기존 사용자 변경사항은 되돌리지 않는다.
  - 실패 원인이 공통 설정 회귀인지 개별 테스트 문제인지 구분해야 한다.
  - 최종 품질 게이트는 `./gradlew build` 성공이다.
- Expected Outcome
  - 실패 테스트 원인과 수정 범위가 정리된 하베스트 문서
  - Non-UC 범위의 기술/테스트 변경 목록
  - 후속 구현과 검증에 바로 사용할 수 있는 work unit
- Explicit Non-goals
  - 새로운 사용자 기능 추가
  - 이번 턴에서 oracle/orchestrate-plan 시작

# Work Item Classification
## UC
- 없음. 요청은 회귀 수정과 런타임 검증이다.

## UI
- 없음.

## TECH
- 최근 batch startup 변경이 테스트 컨텍스트와 애플리케이션 기동에 미친 영향 분석
- 보안 테스트와 공통 사용자 서비스 테스트 실패의 공통 설정/의존성 회귀 수정
- `bootRun` 경로에서 새 startup 로직이 서버 기동을 막는지 확인

## TEST
- 언급된 실패 테스트 개별 재현
- 수정 후 관련 회귀 테스트 재실행
- 최종 `./gradlew build --console=plain` 검증
- 가능 시 `./gradlew bootRun` 실기동 확인

## DOC
- 하베스트 및 work-unit 문서 작성
- 필요 시 실패 원인과 검증 결과를 work-unit에 반영

# Candidate Use Cases
- 없음

# Confirmed Use Cases
- 없음

# Non-Use-Case Changes
## UI Changes
- 없음

## Technical Changes
- TECH-1: batch startup 변경으로 인한 테스트 컨텍스트/런타임 회귀 원인 분석
- TECH-2: `SecurityControllerTest`, `CommonUserServiceTest` 실패를 일으키는 보안/빈 설정 회귀 수정
- TECH-3: `SheetPostCacheWarmupJobIntegrationTest` 실패 원인 수정
- TECH-4: `bootRun` 기동 경로에서 startup job 실행 정책 검증 및 필요 시 조정

## Test / Quality Changes
- TEST-1: 세 테스트 군을 개별 재현해 공통 원인과 개별 원인 분리
- TEST-2: 수정 후 대상 테스트 재실행
- TEST-3: 최종 `./gradlew build --console=plain` 재검증
- TEST-4: 가능 시 `./gradlew bootRun` 실기동 검증

## Documentation Changes
- DOC-1: 하베스트 및 work-unit 문서 작성
- DOC-2: 실행 중 확인된 실패 원인과 잔여 리스크를 work-unit에 반영

# Coverage Mapping
- "`./gradlew build --console=plain`는 실패" -> TEST-3
- "`SheetPostCacheWarmupJobIntegrationTest`, 여러 `SecurityControllerTest`, 여러 `CommonUserServiceTest`" -> TECH-2, TECH-3, TEST-1, TEST-2
- "`./gradlew bootRun` 실기동 검증은 아직 못 했습니다" -> TECH-4, TEST-4
- "이 문제 해결해줘" -> TECH-1, TECH-2, TECH-3, TECH-4, TEST-1, TEST-2, TEST-3, TEST-4
- Coverage Gaps
  - 없음. 요청은 모두 Non-UC 범위로 매핑 가능하다.

# Coverage Gate
- Ready for Event Storming: N/A
- Why
  - 사용자 기능이 아니라 회귀 수정 및 검증 작업이다.
- Blocking Conditions
  - 없음

# Non-UC Scope Gate
- Ready for Design/Planning: READY
- Why
  - 실패 테스트 목록과 최종 검증 기준이 이미 명시돼 있다.
  - 영향 범위가 batch startup, security test context, common user service로 압축된다.
- Blocking Conditions
  - `bootRun` 검증이 샌드박스/환경 제약으로 막힐 수 있다.

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
- 현재 실패들이 모두 같은 최근 변경에서 유래했는지, 아니면 기존 잠재 실패가 드러난 것인지는 재현 후 확정해야 한다.
- `bootRun` 검증은 외부 인프라/프로퍼티 요구 여부에 따라 별도 제약이 있을 수 있다.

# Needs Review
- startup runner 실패 정책이 테스트 컨텍스트에서도 동일하게 적용돼야 하는지
- `bootRun` 확인 시 필수 외부 의존성 없이 dev profile만으로 기동 가능한지

# Execution Notes
- 기존 work unit 기록상 최근 batch startup 관련 작업 이후 repository-wide build에서 위 세 테스트 군 실패가 관측됐다.
- 이번 작업은 기능 확장이 아니라 회귀 제거와 검증 복구에 초점이 있다.

# Rejected Use Cases
- 없음

# Missing-but-Plausible Use Cases
- 없음

# Next Revision Focus
- 실패 테스트 개별 원인 확정
- 수정 범위 축소 및 검증 결과 반영
- `bootRun` 런타임 검증 상태 업데이트

# Oracle Handoff
- Allowed To Proceed: NO
- Confirmed Use Cases for Oracle
  - 없음
- Non-Use-Case Changes for Oracle
  - TECH-1
  - TECH-2
  - TECH-3
  - TECH-4
  - TEST-1
  - TEST-2
  - TEST-3
  - TEST-4
  - DOC-1
  - DOC-2
- Assumptions Forbidden for Oracle
  - 세 테스트 군이 모두 같은 원인이라고 가정하면 안 된다.
  - `bootRun`이 외부 의존성 없이 항상 성공한다고 가정하면 안 된다.
- User Approval Required Before Orchestration: YES

# Backlinks
- docs/work-units/test/build-regression-fixes-20260415-1820/index.md
