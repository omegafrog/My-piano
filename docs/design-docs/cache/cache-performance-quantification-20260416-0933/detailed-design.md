# Properties
doc_path: docs/design-docs/cache/cache-performance-quantification-20260416-0933/detailed-design.md
owner: Codex
status: completed
title: 캐시 적용 전후 성능 향상 정량 비교 테스트
domain: cache
task: cache-performance-quantification-20260416-0933
last_updated: 2026-04-16:09:40

# Detailed Design

# Functional Behavior Design
- N/A for this work unit. 외부 API 또는 사용자 기능 동작은 바뀌지 않는다.

# UI/UX Design Impact
- N/A for this work unit.

# Technical / Refactoring Design Impact
## 1. before/after 기준 고정
- before:
  - `NoOpCacheManager` 기반 coordinator를 사용해 동일 조회 경로에서 cache miss만 발생하는 baseline
- after:
  - 실제 cache manager 기반 coordinator로 같은 요청을 한 번 warm-up한 뒤 cache hit 경로를 측정
- 이유:
  - cold/warm 비교만으로는 "캐시 미적용 상태"와 "캐시 적용 후 steady-state" 차이를 직접 설명하기 어렵다.

## 2. 측정 대상
- 대상 메서드:
  - `SheetPostApplicationService#getSheetPosts(searchSentence, instrument, difficulty, genre, pageable)`
- 요청 조건:
  - cacheable page (`PageRequest.of(0, 20)`)
  - 동일 query parameter
- backend delay:
  - mocked `ElasticSearchInstance.searchSheetPost(...)` 지연을 고정해 환경 잡음을 낮춘다.

## 3. 비교 지표
- 응답시간:
  - uncached median latency
  - warm cached median latency
  - improvement ratio (`uncached / cached`)
- backend load:
  - 같은 요청 반복 시 backend search invocation count
- acceptance direction:
  - warm cached 경로가 uncached baseline보다 유의미하게 빨라야 한다.
  - cached 경로는 반복 호출에서 backend 호출 수를 줄여야 한다.

## 4. 구현 방식
- 기존 `SheetPostCachePerformanceCharacterizationTest`를 확장한다.
- 두 개의 `SheetPostApplicationService` 인스턴스를 구성한다.
  - one with `NoOpCacheManager`
  - one with `ConcurrentMapCacheManager`
- 공통 mock/stub을 사용해 같은 backend latency를 재사용한다.
- 여러 샘플을 수집하고 median을 비교해 일시적 노이즈를 완화한다.

## 5. Docker 기반 build/test gate
- 대상:
  - `build.gradle`
  - `scripts/ensure-test-infra.sh`
- 설계:
  - `test` task가 실행되기 전에 `ensureTestInfra` task를 반드시 수행한다.
  - 스크립트는 `docker compose` 또는 `docker-compose`를 감지해 MySQL, Redis 2개, Elasticsearch 컨테이너를 기동하거나 재사용한다.
  - MySQL healthcheck 이후 `mypiano`, `mypianotest` 두 DB를 보장한다.
  - Redis는 `redis-cli ping`, Elasticsearch는 HTTP 응답으로 readiness를 확인한다.
- 이유:
  - 현재 repository-wide gate는 로컬 infra 기동 여부에 민감하므로, 최소한의 테스트 인프라는 gate 자체가 먼저 보장해야 한다.

# Test / Quality Design Impact
## Focused tests
- `SheetPostCachePerformanceCharacterizationTest`
  - cache disabled baseline vs warm cached hit median 비교
  - 반복 호출에서 backend search call collapse 확인

## Required gate
- `./gradlew test --tests com.omegafrog.My.piano.app.cache.SheetPostCachePerformanceCharacterizationTest --console=plain`
- `./gradlew build --console=plain`
- 두 명령 모두 `ensureTestInfra` 선행 실행을 통해 Docker 인프라를 먼저 준비한다.

## Optional support evidence
- 필요 시 JUnit XML의 `system-out`을 읽어 측정치 숫자를 문서에 반영한다.

# Documentation Impact
- `implementation-log.md`에 측정치와 개선 배수를 기록한다.
- `test-gate.md`에 focused test 결과와 repository-wide build 결과를 함께 남긴다.

# Assumptions Required For Execution
- mocked latency 기반 특성화 테스트가 이번 작업의 "정량 비교 테스트" 요구를 충족하는 최소 자동화 증거로 인정된다.
- NoOp cache baseline은 "캐시 미적용 상태"를 표현하는 합리적인 대조군이다.
- build gate 실패 또는 장기 행잉이 발생하면 신규 테스트 결과와 별도로 구분 기록한다.

# Backlinks
- docs/work-units/cache/cache-performance-quantification-20260416-0933/index.md

# Discovery Hints (grep)
- grep -n "^# Functional Behavior Design" docs/design-docs/cache/cache-performance-quantification-20260416-0933/detailed-design.md
