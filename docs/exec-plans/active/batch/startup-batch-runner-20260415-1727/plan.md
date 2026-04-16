# Properties
doc_path: docs/exec-plans/active/batch/startup-batch-runner-20260415-1727/plan.md
status: completed
title: startup runner로 다중 Spring Batch job 명시 실행 전환
domain: batch
task: startup-batch-runner-20260415-1727
source_use_case_harvest: docs/use-case-harvests/batch/startup-batch-runner-20260415-1727/use-case-harvest.md
last_updated: 2026-04-15:18:30

# Discovery Hints (grep)
- grep -R "^# Properties" docs/use-case-harvests/batch/startup-batch-runner-20260415-1727/
- grep -R "^# Properties" docs/product-specs/batch/startup-batch-runner-20260415-1727/
- grep -R "^# Properties" docs/design-docs/batch/startup-batch-runner-20260415-1727/
- grep -R "^# Properties" docs/exec-plans/active/batch/startup-batch-runner-20260415-1727/

# Purpose / Big Picture
- Spring Boot 기본 batch auto-launch를 끄고, startup 시점에 필요한 두 batch job을 custom runner에서 명시 실행하도록 전환한다.
- `bootRun` 기본 경로의 즉시 실패를 제거하면서 기존 주기 scheduler는 유지한다.
- oracle output과 pre-execution doc verification을 기반으로 executor 구현이 완료되었고, 후속 verification 사실이 반영되었다.

# Progress
- [x] TECH `spring.batch.job.enabled=false` 설정 반영
- [x] TECH startup runner 추가 및 두 job launch wiring
- [x] TECH job별 failure isolation 및 startup 로그 정책 반영
- [x] TEST startup runner 단위 테스트 추가
- [x] TEST 첫 번째 launch 실패 시 두 번째 launch 지속 테스트 추가
- [x] TEST `./gradlew build --console=plain` 실행 및 failing suite 관측
- [x] DOC work unit / verification 문서와 구현 결과 동기화

# Surprises & Discoveries
- `ViewCountPersistentJobConfig`와 `ViewCountPersistentJobScheduler`는 `!test` 프로필로 보호되지만, `SheetPostCacheWarmupJobConfig`는 그렇지 않다.
- `SheetPostCacheWarmupJobScheduler`는 property 기반 활성화이고, startup runner는 별도 활성화 정책을 명시해야 한다.

# Decision Log
- 기본 Boot auto-launch는 사용하지 않는다.
- startup hook 타입은 `ApplicationRunner`를 우선 채택한다.
- 두 job launch는 같은 runner 안에서 독립 시도하며, 완료 순서/동기화를 추가 요구로 만들지 않는다.
- startup runner는 기존 scheduler를 대체하지 않는다.
- 사용자 승인에 따라 두 job은 startup 시 각각 launch한다.

# Context and Orientation
- 관련 코드:
  - `src/main/java/com/omegafrog/My/piano/app/batch/ViewCountPersistentJobConfig.java`
  - `src/main/java/com/omegafrog/My/piano/app/batch/SheetPostCacheWarmupJobConfig.java`
  - `src/main/java/com/omegafrog/My/piano/app/batch/ViewCountPersistentJobScheduler.java`
  - `src/main/java/com/omegafrog/My/piano/app/batch/SheetPostCacheWarmupJobScheduler.java`
  - `src/main/resources/application.yml`
- 검증 기준:
  - repository rule상 최종 검증 명령은 `./gradlew build`

# Plan of Work
- 설정 변경으로 Boot 기본 batch auto-runner를 비활성화한다.
- startup runner를 batch 패키지에 추가해 두 job을 개별 launch한다.
- failure isolation과 로그를 job 단위로 명시한다.
- runner 단위 테스트와 build gate로 회귀를 확인한다.

# Concrete Steps
1. `application.yml`에서 batch auto-launch 비활성화 설정을 추가한다.
2. batch 패키지에 non-test startup runner를 추가한다.
3. 두 job launch용 `JobParameters` 생성 규칙을 정한다.
4. job별 `try/catch`와 로그를 적용한다.
5. 신규 runner 단위 테스트를 작성한다.
6. `./gradlew build`를 실행하고 결과를 기록한다.

# Validation and Acceptance
- `bootRun` 기본 경로가 다중 job auto-launch 예외로 즉시 종료되지 않는다.
- startup runner가 두 job launch를 각각 시도한다.
- 첫 번째 job launch 실패 시 두 번째 launch 시도가 유지된다.
- `./gradlew build` 결과와 failing suite를 명확히 기록한다.

# Idempotence and Recovery
- startup runner는 애플리케이션 재기동 시 두 job launch를 다시 시도한다.
- 중복 실행 방지는 Spring Batch `JobParameters` 고유성에 의존한다.
- 한 job launch 실패 시 다른 job과 서버 기동은 계속 진행한다.

# Documentation Impact
- 구현 후 `implementation-log.md`와 verification report를 실제 결과로 갱신해야 한다.
- executor 진입 전 canonical execution-doc placeholders를 유지해 missing-file gap을 없앤다.
- 이번 계획 문서는 non-UC-only 작업임을 유지해야 한다.

# Change Log
- 2026-04-15 17:55 KST: 초기 planning/design artifact 작성
- 2026-04-15 17:55 KST: oracle output과 pre-execute doc verification을 반영하고 execution-ready 상태를 명시했다.
- 2026-04-15 18:30 KST: startup runner 구현 완료, focused regression pass, repository-wide build gate fail, `bootRun` 미검증 상태를 반영했다.

# Backlinks
- docs/work-units/batch/startup-batch-runner-20260415-1727/index.md
