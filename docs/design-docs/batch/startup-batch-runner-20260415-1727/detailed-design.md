# Properties
doc_path: docs/design-docs/batch/startup-batch-runner-20260415-1727/detailed-design.md
owner: Codex
status: completed
title: startup runner로 다중 Spring Batch job 명시 실행 전환
domain: batch
task: startup-batch-runner-20260415-1727
last_updated: 2026-04-15:17:55

# Detailed Design

# Functional Behavior Design
- N/A for this work unit. 외부 API나 사용자 기능 플로우는 바뀌지 않는다.

# UI/UX Design Impact
- N/A for this work unit.

# Technical / Refactoring Design Impact
## 1. Boot 기본 batch auto-launch 비활성화
- 적용 대상: `src/main/resources/application.yml` 또는 profile 공통 startup 설정
- 설계:
  - `spring.batch.job.enabled=false`를 기본값으로 둔다.
  - Spring Boot 기본 `JobLauncherApplicationRunner`가 startup 시 `Job` bean을 자동 실행하지 않도록 막는다.
- 기대 효과:
  - `bootRun` 기본 기동이 더 이상 `Job name must be specified in case of multiple jobs` 예외로 종료되지 않는다.

## 2. Startup runner 추가
- 적용 대상: `src/main/java/com/omegafrog/My/piano/app/batch/` 하위 신규 runner 클래스
- 권장 타입:
  - `ApplicationRunner`
- 권장 활성화 조건:
  - `@Profile("!test")`
- 의존성:
  - `JobLauncher`
  - `@Qualifier("persistViewCountJob") Job`
  - `@Qualifier("sheetPostCacheWarmupJob") Job`
- 실행 방식:
  - startup 시 `persistViewCountJob` launch 시도
  - startup 시 `sheetPostCacheWarmupJob` launch 시도
  - 두 launch는 같은 runner 안에서 각각 독립 호출
- parameter 규칙:
  - 각 job마다 고유 `JobParameters` 사용
  - 공통 키 후보: `requestedAt`
  - 충돌 회피를 위해 job마다 다른 timestamp 또는 job 식별 파라미터를 포함

## 3. Failure isolation 정책
- 기본 정책:
  - 첫 번째 launch 실패가 두 번째 launch 시도를 막지 않는다.
  - runner 내부에서 job별 `try/catch`로 예외를 분리 처리한다.
  - startup runner 자체는 예외를 재전파하지 않고 실패 사실을 로그로 남긴다.
- 로그 요구:
  - job 시작 로그
  - job launch 실패 로그
  - 가능하면 job 이름과 parameter 핵심값 포함
- 이유:
  - 이번 작업의 목적은 local bootRun 운영성 개선이며, startup batch 실패가 전체 애플리케이션 기동 실패로 이어지는 정책은 현재 요구와 맞지 않는다.

## 4. 기존 scheduler와의 관계
- `ViewCountPersistentJobScheduler`는 6시간 주기 flush 용도로 유지한다.
- `SheetPostCacheWarmupJobScheduler`는 `cache.warmup.enabled=true`일 때 주기 warm-up을 계속 담당한다.
- 새 startup runner는 "기동 시 1회 실행" 역할만 추가하며, 기존 scheduler를 대체하지 않는다.

## 5. 구현 시 주의점
- `SheetPostCacheWarmupJobConfig`는 현재 `test` 프로필 제외 조건이 없다.
- 새 runner가 테스트에서 실행되면 기존 통합 테스트 bootstrap에 영향을 줄 수 있으므로 runner는 반드시 `!test` 조건을 가져야 한다.
- `JobLauncher` 구현이 비동기일 수 있으므로, 두 job의 완료 순서를 전제로 한 후속 로직은 넣지 않는다.

# Test / Quality Design Impact
## 단위 테스트
- 신규 startup runner 테스트
  - 두 `Job`에 대해 `JobLauncher.run(...)`이 각각 1회 호출되는지 검증
  - 첫 번째 job launch가 예외를 던져도 두 번째 job launch가 여전히 시도되는지 검증
- 기존 warmup scheduler 단위 테스트는 유지

## 통합/회귀 검증
- `./gradlew build`
- 가능하면 로컬에서 `./gradlew bootRun` 또는 동등 startup 경로로 다중 job auto-launch 예외가 제거됐는지 확인
- 환경 의존성 미충족으로 build 또는 bootRun이 막히면 `BLOCKED`로 기록

# Documentation Impact
- work unit hub에 canonical stage docs 링크를 추가한다.
- 필요하면 로컬 실행 시 startup batch 정책을 별도 운영 메모에 반영한다. 이번 범위에서는 필수는 아니다.

# Assumptions Required For Execution
- startup 시 `SheetPostCacheWarmupJob`을 1회 실행하는 정책이 non-test 기본 동작으로 허용되어야 한다.
- `spring.batch.job.enabled=false`를 기본 설정에 둬도 다른 startup 경로가 Boot auto-launch에 의존하지 않아야 한다.
- startup runner는 배치 실패를 로그로 남기고 계속 기동하는 정책을 채택해도 운영상 허용 가능해야 한다.

# Backlinks
- docs/work-units/batch/startup-batch-runner-20260415-1727/index.md

# Discovery Hints (grep)
- grep -n "^# Functional Behavior Design" docs/design-docs/batch/startup-batch-runner-20260415-1727/detailed-design.md
