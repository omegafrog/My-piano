# Properties
doc_path: docs/product-specs/batch/startup-batch-runner-20260415-1727/domain-boundary.md
owner: Codex
status: completed
title: startup runner로 다중 Spring Batch job 명시 실행 전환
domain: batch
task: startup-batch-runner-20260415-1727
last_updated: 2026-04-15:17:55

# Scope
- `bootRun` 기본 기동 시 Spring Boot의 Batch auto-launch가 다중 `Job` bean 때문에 실패하지 않도록 startup 경로를 재구성한다.
- 애플리케이션 startup 시점에 `PersistViewCountJob`과 `SheetPostCacheWarmupJob`을 명시적으로 launch하는 기술 설계를 정의한다.
- 한 job launch 실패가 다른 job launch 또는 전체 서버 기동을 즉시 중단시키지 않도록 failure isolation 정책을 정의한다.

# External Boundaries
- Spring Boot Batch auto-configuration
  - 기본 `JobLauncherApplicationRunner`가 `Job` bean 다중 존재 시 `job name must be specified` 예외를 유발하는 startup 경계
- Spring Batch launcher/runtime
  - `JobLauncher`, `JobParameters`, `JobExecution`을 통한 명시 실행 경계
- Existing scheduler components
  - `ViewCountPersistentJobScheduler`
  - `SheetPostCacheWarmupJobScheduler`
- Local verification environment
  - `./gradlew build`
  - 필요 시 로컬 MySQL/Redis/Elasticsearch 등 외부 의존성

# In Scope
- `spring.batch.job.enabled=false` 적용 위치와 영향 범위 정의
- non-test profile startup runner 설계
- 두 job에 대한 개별 launch parameter 규칙 정의
- job별 예외 처리 및 로그 정책 정의
- 단위 테스트와 build gate 중심 검증 항목 정의

# Out of Scope
- `PersistViewCountJob` 내부 step/reader/writer 로직 변경
- `SheetPostCacheWarmupJob` 내부 warm-up 로직 변경
- 기존 `@Scheduled` 주기, cron, property 이름 변경
- 운영 배포 스크립트 또는 외부 설정 체계 개편

# Key Constraints
- 새 startup runner는 `test` 프로필에서 비활성화되어야 기존 테스트 bootstrap에 부작용을 만들지 않는다.
- 두 job은 모두 startup 시도 대상이지만, 엄격한 순차 완료 보장은 이번 작업 범위가 아니다.
- 각 launch는 서로 다른 `JobParameters`를 사용해야 기존 실행 이력과 충돌하지 않는다.
- 기존 스케줄러는 startup runner와 별도로 유지되어야 한다.

# Backlinks
- docs/work-units/batch/startup-batch-runner-20260415-1727/index.md

# Discovery Hints (grep)
- grep -n "^# Scope" docs/product-specs/batch/startup-batch-runner-20260415-1727/domain-boundary.md
