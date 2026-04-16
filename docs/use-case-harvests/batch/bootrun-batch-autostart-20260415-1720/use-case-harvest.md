# Properties
doc_path: docs/use-case-harvests/batch/bootrun-batch-autostart-20260415-1720/use-case-harvest.md
owner: Codex
status: ready-for-oracle
title: bootRun 기본 실행 시 Spring Batch 다중 잡 자동 실행 충돌 해소
domain: batch
task: bootrun-batch-autostart-20260415-1720
coverage_gate: N/A
non_uc_scope_status: READY
next_step: wait-for-user-approval
last_updated: 2026-04-15:17:20

# Prompt Interpretation
- User Goal
  - 로컬에서 `./gradlew bootRun`을 기본값으로 실행했을 때 Spring Batch 잡이 여러 개 등록되어 바로 종료되는 문제를 없앤다.
  - 개발자가 로컬 검증을 반복할 때 배치 자동 실행 충돌에 걸리지 않도록 기본 실행 경험을 안정화한다.
- Requested Actions
  - 현재 `bootRun` 실패 원인을 정리한다.
  - 로컬 기본 실행 경로에서 배치 자동 실행 충돌을 막는 해결 방향을 정의한다.
  - 이후 구현 계획이 가능한 수준으로 범위를 하베스트한다.
- Preferred Implementation Stack
  - Java 17
  - Spring Boot 3.2.x
  - Spring Batch
  - Gradle
- Constraints
  - 기존 사용자 변경사항은 되돌리지 않는다.
  - 오케스트레이션 전 단계인 하베스트만 수행한다.
  - 프로젝트 규칙상 실제 코드 변경 후에는 `./gradlew build` 검증이 필요하다.
- Expected Outcome
  - 문제 원인과 영향 범위가 문서화된다.
  - 해결 대상 파일과 검증 포인트가 정리된다.
  - oracle로 넘길 수 있는 Non-UC 작업 목록이 준비된다.
- Explicit Non-goals
  - 새로운 사용자 기능 추가
  - 이번 턴에서 oracle/executor 실행
  - 이번 턴에서 코드 수정 착수

# Work Item Classification
## UC
- 없음. 이번 요청은 사용자 플로우 추가가 아니라 로컬 실행 운영성 개선이다.

## UI
- 없음.

## TECH
- `bootRun` 기본 경로에서 Spring Batch 다중 Job bean 자동 실행 충돌 원인 분석
- 로컬 기본 실행 시 배치 auto-launch를 비활성화하거나 실행 조건을 분리하는 설계
- 기존 스케줄러 기반 배치 실행과 수동 배치 실행 경로가 유지되는지 영향 범위 확인

## TEST
- 현재 실패 재현 조건 확인
- 수정 후 `bootRun` 기본 실행이 애플리케이션 기동까지 이어지는지 검증
- 배치 스케줄러/명시적 job 실행 경로가 회귀되지 않는지 확인

## DOC
- 하베스트 및 work-unit 문서 작성
- 필요 시 로컬 실행 가이드(`README.md`, `CLAUDE.md`) 보완 범위 식별

# Candidate Use Cases
- 없음

# Confirmed Use Cases
- 없음

# Non-Use-Case Changes
## UI Changes
- 없음

## Technical Changes
- TECH-1: 기본 `bootRun` 경로에서 Spring Batch auto-launch 충돌 제거
- TECH-2: 배치 실행 정책을 로컬 기본 기동과 스케줄/수동 실행으로 분리

## Test / Quality Changes
- TEST-1: 다중 Job bean 환경에서 `bootRun` 기본 기동 실패 재현 근거 정리
- TEST-2: 수정 후 애플리케이션 정상 기동 검증
- TEST-3: 기존 배치 스케줄러 또는 명시적 job 실행 경로 회귀 여부 확인

## Documentation Changes
- DOC-1: 하베스트 및 work-unit 문서 작성
- DOC-2: 로컬 실행 가이드 반영 필요 여부 판단

# Coverage Mapping
- "기본 bootRun은 Spring Batch 잡이 여러 개라서 Job name must be specified ... 로 바로 죽음" -> TECH-1, TEST-1
- "로컬 검증 시 반복해서 걸리는 운영성 문제" -> TECH-2, TEST-2, TEST-3
- "이 문제를 해결" -> TECH-1, TECH-2, TEST-2, DOC-2
- Coverage Gaps
  - 없음. 요청은 모두 Non-UC 범위로 매핑 가능하다.

# Coverage Gate
- Ready for Event Storming: N/A
- Why
  - 기능 유스케이스가 아니라 개발/운영성 개선 작업이다.
- Blocking Conditions
  - 없음

# Non-UC Scope Gate
- Ready for Design/Planning: READY
- Why
  - 충돌 원인이 되는 배치 Job bean 두 개(`PersistViewCountJob`, `SheetPostCacheWarmupJob`)와 기본 실행 경로(`application.yml`, `bootRun` 가이드)가 확인됐다.
  - 해결 방향이 "기본 기동에서 배치 auto-launch 분리"로 충분히 구체화됐다.
- Blocking Conditions
  - 운영/CI에서 startup 시점 auto-launch가 실제로 필요한지 최종 구현 단계에서 확인이 필요하다.

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
- `spring.batch.job.enabled=false`를 전체 기본값으로 둘지, `dev` 또는 `local` 실행 범위로만 제한할지는 구현 시 결정이 필요하다.
- 프로덕션/운영 배포가 `bootRun`에 의존하지는 않아 보여도, startup auto-launch 의존 코드가 있는지 추가 확인이 필요하다.
- 현재 세션에서는 Gradle wrapper 다운로드와 샌드박스 제약 때문에 실제 `bootRun` 재현 로그를 수집하지 못했다.

# Needs Review
- 배치 auto-launch를 끄더라도 `@Scheduled` 기반 실행은 그대로 유지되는지
- warmup job은 스케줄러가 property 기반(`cache.warmup.enabled`)이라 기본값에서 영향이 없는지
- 로컬 실행 문서에 "기본 `bootRun`은 배치 자동 실행 없이 서버 기동"이라는 기대 동작을 명시할지

# Execution Notes
- 코드상 확인된 배치 Job bean
  - `src/main/java/com/omegafrog/My/piano/app/batch/ViewCountPersistentJobConfig.java`
    - `PersistViewCountJob`
  - `src/main/java/com/omegafrog/My/piano/app/batch/SheetPostCacheWarmupJobConfig.java`
    - `SheetPostCacheWarmupJob`
- 현재 설정 상태
  - `src/main/resources/application.yml`에 `spring.batch.job.enabled` 또는 `spring.batch.job.name`이 없다.
  - Spring Boot 기본 동작상 Job bean이 여러 개면 startup auto-launch 시 job name 지정이 필요하다.
  - 따라서 기본 `bootRun`은 배치 auto-launch 단계에서 종료될 가능성이 높다.
- 스케줄러 상태
  - `ViewCountPersistentJobScheduler`는 `@Scheduled`로 직접 `persistViewCountJob`을 실행한다.
  - `SheetPostCacheWarmupJobScheduler`는 `cache.warmup.enabled=true`일 때만 활성화된다.
  - 즉 startup auto-launch를 끄더라도 스케줄러/명시적 launcher 경로는 별도로 유지될 수 있다.
- 재현 시도 메모
  - 샌드박스 기본 Gradle home은 읽기 전용이라 실패했다.
  - 작업공간 내부 `GRADLE_USER_HOME`으로 우회 시도했지만, wrapper 다운로드 네트워크가 막혀 실제 기동 로그 확보는 실패했다.

# Rejected Use Cases
- 없음

# Missing-but-Plausible Use Cases
- 없음

# Next Revision Focus
- 기본 기동에서 적용할 배치 실행 정책을 한 가지로 확정
- 검증 명령과 기대 로그를 명문화
- 문서 변경 범위가 필요한지 확정

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
  - DOC-1
  - DOC-2
- Assumptions Forbidden for Oracle
  - 운영 환경에서도 startup auto-launch가 불필요하다고 단정하면 안 된다.
  - 단순히 `spring.batch.job.name` 하나를 박아 넣으면 문제가 해결된다고 가정하면 안 된다.
  - 스케줄러 경로가 startup auto-launch와 동일하게 동작한다고 가정하면 안 된다.
- User Approval Required Before Orchestration: YES

# Backlinks
- docs/work-units/batch/bootrun-batch-autostart-20260415-1720/index.md
