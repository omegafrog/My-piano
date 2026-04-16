# bootRun 기본 실행 시 Spring Batch 다중 잡 자동 실행 충돌 해소

- task: `bootrun-batch-autostart-20260415-1720`
- domain: `batch`
- harvest: `docs/use-case-harvests/batch/bootrun-batch-autostart-20260415-1720/use-case-harvest.md`
- scope
  - 기본 `bootRun` 경로의 Spring Batch auto-launch 충돌 분석
  - 로컬 기본 기동과 배치 실행 정책 분리 방향 정의
  - 수정 후 기동/회귀 검증 포인트 정리
- status: ready-for-oracle
- current-findings
  - 현재 코드베이스에는 `PersistViewCountJob`, `SheetPostCacheWarmupJob` 두 개의 Spring Batch Job bean이 존재한다.
  - `application.yml`에는 `spring.batch.job.enabled`나 `spring.batch.job.name`이 없어 기본 startup auto-launch 충돌 조건이 충족된다.
  - `ViewCountPersistentJobScheduler`와 `SheetPostCacheWarmupJobScheduler`는 `JobLauncher`로 명시 실행하므로, startup auto-launch를 분리하는 방향이 유력하다.
  - 실제 `bootRun` 로그 재현은 이번 세션에서 Gradle wrapper 다운로드 제한 때문에 확보하지 못했다.
