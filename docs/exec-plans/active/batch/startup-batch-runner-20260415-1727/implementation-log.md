# Properties
doc_path: docs/exec-plans/active/batch/startup-batch-runner-20260415-1727/implementation-log.md
status: completed
domain: batch
task: startup-batch-runner-20260415-1727
last_updated: 2026-04-15:18:30

# Summary
- Boot 기본 batch auto-launch를 비활성화하고, non-test startup `ApplicationRunner`에서 `persistViewCountJob`과 `sheetPostCacheWarmupJob`을 명시 launch하도록 전환했다.
- startup runner는 job별 고유 `JobParameters`(`trigger`, `startupJob`, `requestedAt`)를 생성하고, 한 job launch 실패가 다음 job launch와 서버 기동을 막지 않도록 failure isolation을 적용했다.
- runner 단위 테스트를 추가해 두 job launch 및 첫 launch 실패 후 후속 launch 지속 동작을 검증했다.

# Implemented Scope
- `spring.batch.job.enabled=false` 설정 추가로 Boot의 기본 `JobLauncherApplicationRunner` 경로를 끊었다.
- `BatchStartupJobRunner`를 추가해 non-test 프로필에서 startup 시 두 batch job을 순차 launch하도록 구성했다.
- startup runner 동작을 검증하는 focused unit test를 추가했다.

# File Changes
## Created
- `src/main/java/com/omegafrog/My/piano/app/batch/BatchStartupJobRunner.java`
- `src/test/java/com/omegafrog/My/piano/app/batch/BatchStartupJobRunnerTest.java`
## Modified
- `src/main/resources/application.yml`
- `docs/exec-plans/active/batch/startup-batch-runner-20260415-1727/implementation-log.md`
## Deleted / Renamed
- N/A

# Code-to-Plan Mapping
- `spring.batch.job.enabled=false` 설정 반영: `src/main/resources/application.yml`
- startup runner 추가 및 두 job launch wiring: `src/main/java/com/omegafrog/My/piano/app/batch/BatchStartupJobRunner.java`
- job별 failure isolation 및 startup 로그 정책 반영: `src/main/java/com/omegafrog/My/piano/app/batch/BatchStartupJobRunner.java`
- startup runner 단위 테스트 추가: `src/test/java/com/omegafrog/My/piano/app/batch/BatchStartupJobRunnerTest.java`
- 첫 번째 launch 실패 시 두 번째 launch 지속 테스트 추가: `src/test/java/com/omegafrog/My/piano/app/batch/BatchStartupJobRunnerTest.java`

# External Contract Changes
- N/A

# Policy / Domain Rule Changes
- N/A

# Architectural Impact
- Spring Boot 기본 batch auto-launch 대신 애플리케이션 코드가 startup batch launch 책임을 가진다.
- 기존 `@Scheduled` 기반 job launch는 유지되며, startup 1회 실행과 주기 실행이 분리된다.

# Documentation Updates
- executor 실제 구현 결과를 반영하도록 이 문서를 갱신했다.

# Validation Summary
- `./gradlew test --tests com.omegafrog.My.piano.app.batch.BatchStartupJobRunnerTest` 실행 결과 `BUILD SUCCESSFUL`로 focused unit test를 통과했다.
- `./gradlew build --console=plain` 실행에서 repository-wide gate가 실패했다. 관측된 failing test에는 `SheetPostCacheWarmupJobIntegrationTest`, 여러 `SecurityControllerTest`, 여러 `CommonUserServiceTest`가 포함됐다.
- `./gradlew bootRun` 런타임 검증은 아직 수행되지 않아, 다중 job auto-launch 예외가 실제로 제거됐는지는 문서상 미검증 상태로 유지한다.

# Remaining Gaps
- repository-wide `./gradlew build --console=plain` gate가 실패했으므로 failing suite의 원인 분류와 후속 조치가 필요하다.
- startup runner의 실제 `bootRun` 경로 동작은 아직 검증되지 않았다.

# Risks & Follow-ups
- startup 시 두 job이 모두 실행되므로, 운영 프로필에서 초기 부하가 허용 범위인지 후속 확인이 필요하다.
- final verification에서 기존 스케줄러와 startup runner의 중복 실행 허용성이 의도대로인지 확인해야 한다.
- 현재 build gate 실패는 focused startup-runner 변경 자체의 회귀 검증과는 별개로 repository-wide suite 안정성 이슈일 수 있으므로, 원인 분리가 필요하다.

# Discovery Hints (grep)
- grep -n "^# File Changes" docs/exec-plans/active/batch/startup-batch-runner-20260415-1727/implementation-log.md
- grep -n "^# Code-to-Plan Mapping" docs/exec-plans/active/batch/startup-batch-runner-20260415-1727/implementation-log.md

# Backlinks
- docs/work-units/batch/startup-batch-runner-20260415-1727/index.md
