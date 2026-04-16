# Properties
doc_path: docs/use-case-harvests/batch/startup-batch-runner-20260415-1727/use-case-harvest.md
owner: Codex
status: ready-for-oracle
title: startup runner로 다중 Spring Batch job 명시 실행 전환
domain: batch
task: startup-batch-runner-20260415-1727
coverage_gate: N/A
non_uc_scope_status: READY
next_step: wait-for-user-approval
last_updated: 2026-04-15:17:27

# Prompt Interpretation
- User Goal
  - `bootRun` 기본 실행이 다중 Spring Batch job auto-launch 충돌로 죽지 않게 하면서, 필요한 두 job은 startup 시점에 계속 실행되게 한다.
  - 실행 순서는 엄격한 동기화가 필요하지 않으므로, 하나의 custom startup runner에서 두 job을 명시적으로 띄우는 방향으로 정리한다.
- Requested Actions
  - `use-case-harvester` 기준으로 이 방향을 새 작업 단위로 문서화한다.
  - 구현 시 필요한 기술 변경, 검증 포인트, 리스크를 정리한다.
- Preferred Implementation Stack
  - Java 17
  - Spring Boot 3.2.x
  - Spring Batch
  - Gradle
- Constraints
  - 이번 턴에서는 harvester만 수행한다.
  - 기존 사용자 변경사항은 되돌리지 않는다.
  - 코드 변경 후 최종 검증 기준은 `./gradlew build`다.
- Expected Outcome
  - startup runner 기반 실행 전환 범위가 명확해진 하베스트 문서
  - 구현 시 손대야 할 설정/코드/테스트 지점 정리
  - oracle로 넘길 수 있는 Non-UC 작업 목록
- Explicit Non-goals
  - 새로운 사용자 기능 추가
  - 이번 턴에서 실제 코드 수정
  - 이번 턴에서 oracle/executor 실행

# Work Item Classification
## UC
- 없음. 사용자 기능 플로우가 아니라 애플리케이션 startup 동작과 운영성 개선이다.

## UI
- 없음.

## TECH
- Spring Boot 기본 batch auto-launch 비활성화
- custom `ApplicationRunner` 또는 동등 startup hook에서 두 batch job 명시 실행
- job별 실패 격리와 로그 정책 정의
- 기존 `@Scheduled` 기반 batch 실행과 startup runner 간의 역할 분리

## TEST
- startup runner가 두 job을 모두 launch하는 단위 테스트
- 첫 번째 job 실패 시 두 번째 job 실행 지속 여부 검증
- 기본 `bootRun` 경로가 다중 job auto-launch 예외 없이 기동 가능한지 검증

## DOC
- 하베스트 및 work-unit 문서 작성
- 필요 시 로컬 실행 문서에서 startup batch 정책 설명 보강

# Candidate Use Cases
- 없음

# Confirmed Use Cases
- 없음

# Non-Use-Case Changes
## UI Changes
- 없음

## Technical Changes
- TECH-1: `spring.batch.job.enabled=false`로 기본 auto-launch 비활성화
- TECH-2: startup runner에서 `PersistViewCountJob`, `SheetPostCacheWarmupJob`을 명시 실행
- TECH-3: job별 예외를 분리 처리해 한 job 실패가 전체 서버 기동 또는 다른 job 실행을 막지 않도록 조정

## Test / Quality Changes
- TEST-1: startup runner가 두 job을 launch하는지 검증
- TEST-2: 첫 번째 job 실패 상황에서도 두 번째 job launch가 시도되는지 검증
- TEST-3: 수정 후 기본 `bootRun` 경로의 startup 충돌 제거 확인

## Documentation Changes
- DOC-1: 하베스트 및 work-unit 문서 작성
- DOC-2: startup batch 정책 문서화 필요 여부 판단

# Coverage Mapping
- "두개의 job이 모두 필요한데" -> TECH-2
- "모두 실행시키는 방향" -> TECH-1, TECH-2, TECH-3
- "그렇게 하도록 해봐" -> TECH-1, TECH-2, TEST-1, TEST-2, TEST-3
- Coverage Gaps
  - 없음. 요청은 모두 Non-UC 범위로 매핑 가능하다.

# Coverage Gate
- Ready for Event Storming: N/A
- Why
  - 기능 요구가 아니라 startup orchestration 개선이다.
- Blocking Conditions
  - 없음

# Non-UC Scope Gate
- Ready for Design/Planning: READY
- Why
  - 충돌 원인과 대상 job이 이미 확인됐고, 해결 방향도 "기본 auto-launch off + custom startup runner"로 확정됐다.
  - 테스트 범위도 runner 단위 테스트와 bootRun 검증으로 구체화 가능하다.
- Blocking Conditions
  - startup runner 실패 정책을 "로그만 남기고 계속"으로 둘지, 일부 job 실패를 기동 실패로 볼지 구현 단계에서 확정이 필요하다.

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
- startup runner를 `ApplicationRunner`로 둘지 `ApplicationReadyEvent` 리스너로 둘지는 구현 선택 사항이다.
- 현재 `JobLauncher`가 실질적으로 동기/비동기 어느 형태로 동작하는지에 따라 로그 순서가 달라질 수 있다.
- 샌드박스 제약으로 이번 세션에서 실제 `bootRun` 성공 로그를 확보하지 못했다.

# Needs Review
- startup 시 두 job을 "무조건 둘 다" 실행하는 것이 운영에서도 원하는 정책인지
- 실패 정책을 runner 내부 예외 삼킴으로 둘지, 일부 예외는 재던질지
- `SheetPostCacheWarmupJobScheduler`의 주기 실행과 startup 1회 실행이 중복 허용 가능한지

# Execution Notes
- 현재 코드상 관련 구성
  - `src/main/java/com/omegafrog/My/piano/app/batch/ViewCountPersistentJobConfig.java`
    - `PersistViewCountJob` bean 정의
  - `src/main/java/com/omegafrog/My/piano/app/batch/SheetPostCacheWarmupJobConfig.java`
    - `SheetPostCacheWarmupJob` bean 정의
  - `src/main/java/com/omegafrog/My/piano/app/batch/ViewCountPersistentJobScheduler.java`
    - `JobLauncher`로 주기 실행
  - `src/main/java/com/omegafrog/My/piano/app/batch/SheetPostCacheWarmupJobScheduler.java`
    - property 활성화 시 주기 실행
- 구현 방향 메모
  - `spring.batch.job.enabled=false`로 Boot 기본 `JobLauncherApplicationRunner` auto-launch를 비활성화한다.
  - 새 startup runner에서 두 job을 서로 다른 `JobParameters`로 launch한다.
  - "큰 동기화는 필요 없다"는 사용자 의도를 반영해 두 실행을 엄격히 직렬 동기화하지는 않되, 하나의 runner에서 각각 독립 시도하도록 설계한다.
  - 각 job launch는 개별 `try/catch`로 감싸 장애 격리를 검토한다.

# Rejected Use Cases
- 없음

# Missing-but-Plausible Use Cases
- 없음

# Next Revision Focus
- startup hook 타입과 실패 정책 확정
- runner 단위 테스트 시나리오 구체화
- 문서 업데이트 범위 확정

# Oracle Handoff
- Allowed To Proceed: NO
- Confirmed Use Cases for Oracle
  - 없음
- Non-Use-Case Changes for Oracle
  - TECH-1
  - TECH-2
  - TECH-3
  - TEST-1
  - TEST-2
  - TEST-3
  - DOC-1
  - DOC-2
- Assumptions Forbidden for Oracle
  - 기본 Boot auto-launch가 다중 job을 자동으로 모두 실행해줄 것이라고 가정하면 안 된다.
  - 두 job이 완전한 직렬 동기화가 필요하다고 가정하면 안 된다.
  - startup runner가 기존 스케줄러를 대체한다고 가정하면 안 된다.
- User Approval Required Before Orchestration: YES

# Backlinks
- docs/work-units/batch/startup-batch-runner-20260415-1727/index.md
