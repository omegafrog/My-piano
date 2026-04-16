# Properties
doc_path: docs/exec-plans/active/cache/cache-performance-quantification-20260416-0933/plan.md
status: completed
title: 캐시 적용 전후 성능 향상 정량 비교 테스트
domain: cache
task: cache-performance-quantification-20260416-0933
source_use_case_harvest: docs/use-case-harvests/cache/cache-performance-quantification-20260416-0933/use-case-harvest.md
last_updated: 2026-04-16:09:40

# Discovery Hints (grep)
- grep -R "^# Properties" docs/use-case-harvests/cache/cache-performance-quantification-20260416-0933/
- grep -R "^# Properties" docs/product-specs/cache/cache-performance-quantification-20260416-0933/
- grep -R "^# Properties" docs/design-docs/cache/cache-performance-quantification-20260416-0933/
- grep -R "^# Properties" docs/exec-plans/active/cache/cache-performance-quantification-20260416-0933/

# Purpose / Big Picture
- 캐시 미적용 baseline과 캐시 적용 후 warm-hit 경로를 같은 서비스 메서드에서 직접 비교하는 자동화 테스트를 만든다.
- 응답시간뿐 아니라 backend 호출 감소까지 함께 기록해 캐시 이득을 정량적으로 설명한다.
- focused test와 repository-wide build gate 결과를 모두 남겨 실제 적용 가능성을 판단한다.

# Progress
- [x] TECH before/after 기준을 `cache disabled` 대 `cache enabled + warm hit`로 고정
- [x] DOC canonical planning/design docs 작성
- [x] TEST 기존 성능 특성화 테스트 확장 또는 보강
- [x] TECH Docker 기반 `ensureTestInfra` gate 추가
- [x] TEST focused 비교 테스트 실행
- [x] TEST `./gradlew build --console=plain` 실행 시도 및 blocker 수집
- [x] DOC execution / verification / closure 문서 동기화

# Surprises & Discoveries
- 저장소에는 이미 `SheetPostCachePerformanceCharacterizationTest`와 `scripts/search-benchmark.js`가 존재한다.
- 기존 자동화는 cold/warm 체감 차이는 보여주지만, cache disabled baseline과의 직접 비교는 없다.
- repository-wide gate의 즉시 실패 원인 중 하나는 로컬 MySQL/Redis/Elasticsearch 미기동이었다.
- infra 자동 기동 이후에는 연결 실패 대신 full-context 테스트 경로가 장시간 멈추는 현상이 남았다.

# Decision Log
- 이번 작업의 자동화 주경로는 JUnit 특성화 테스트로 둔다.
- `k6` 스크립트는 optional manual benchmark asset으로 유지한다.
- 성능 비교는 절대 시간보다 median과 improvement ratio를 주 지표로 사용한다.
- build/test gate는 Docker 인프라를 먼저 올린 뒤 실행하도록 Gradle `test` task에 연결한다.

# Context and Orientation
- 관련 코드:
  - `src/main/java/com/omegafrog/My/piano/app/web/service/SheetPostApplicationService.java`
  - `src/main/java/com/omegafrog/My/piano/app/web/service/cache/SheetPostCacheCoordinator.java`
  - `src/test/java/com/omegafrog/My/piano/app/cache/SheetPostCachePerformanceCharacterizationTest.java`
  - `scripts/search-benchmark.js`
- 검증 기준:
  - 프로젝트 규칙상 최종 검증 명령은 `./gradlew build`

# Plan of Work
- 기존 특성화 테스트를 before/after 비교용으로 확장한다.
- 동일 mock latency 하에서 uncached baseline과 warm cached hit median을 수집한다.
- `test` task 이전에 로컬 테스트 인프라를 자동 기동하는 gate를 추가한다.
- 개선 배수와 backend 호출 수를 확인하는 focused test를 실행한다.
- repository-wide build gate와 문서를 업데이트한다.

# Concrete Steps
1. `SheetPostCachePerformanceCharacterizationTest` 구조를 확장해 no-cache service와 cached service를 함께 구성한다.
2. 반복 샘플 측정 helper를 추가한다.
3. before/after latency ratio와 backend invocation count를 검증하는 테스트를 추가한다.
4. `test` task 이전에 Docker 인프라를 자동 기동하도록 Gradle gate를 연결한다.
5. focused test를 실행하고 측정치를 수집한다.
6. `./gradlew build --console=plain`를 실행해 repository-wide gate 결과를 기록한다.
7. execution log, test gate, closure 문서를 실제 결과로 갱신한다.

# Validation and Acceptance
- before/after 비교가 같은 요청 경로와 같은 mock latency에서 수행된다.
- warm cached median latency가 uncached baseline보다 명확히 낮다.
- cached 반복 호출에서 backend search 호출 수가 baseline 대비 줄어든다.
- focused test 결과와 `./gradlew build` 결과 또는 blocker가 문서에 남는다.

# Idempotence and Recovery
- 특성화 테스트는 pure test-only change이므로 반복 실행해도 애플리케이션 상태를 바꾸지 않는다.
- `ensureTestInfra`는 이미 존재하는 컨테이너를 재사용하고 누락된 경우만 생성하므로 반복 실행 가능하다.
- build gate 실패 또는 장기 행잉 시 신규 focused test 결과와 repository-wide blocker를 분리 기록한다.

# Documentation Impact
- executor 후 `implementation-log.md`, `test-gate.md`, `doc-verify-after-execute.md`, `closure.md`를 실제 결과로 덮어쓴다.
- 현재 placeholder execution docs는 canonical completeness를 위해 미리 생성한다.

# Change Log
- 2026-04-16 09:40 KST: oracle/doc_writer 산출물 작성, before/after 기준을 `cache disabled` 대 `cache enabled + warm hit`로 확정했다.
- 2026-04-16 10:14 KST: `ensureTestInfra`와 Docker bootstrap 스크립트를 추가했고, focused 성능 테스트는 PASS, repository-wide build gate는 context load 장기 행잉으로 BLOCKED 상태로 기록했다.
- 2026-04-16 11:00 KST: `ensureTestInfra` 경로를 상대경로로 수정한 뒤 focused test와 build gate를 재실행했고, focused PASS와 build BLOCKED를 다시 확인했다.

# Backlinks
- docs/work-units/cache/cache-performance-quantification-20260416-0933/index.md
