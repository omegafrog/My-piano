# Properties
doc_path: docs/product-specs/cache/cache-performance-quantification-20260416-0933/domain-boundary.md
owner: Codex
status: completed
title: 캐시 적용 전후 성능 향상 정량 비교 테스트
domain: cache
task: cache-performance-quantification-20260416-0933
last_updated: 2026-04-16:09:40

# Scope
- SheetPost 목록 조회 경로에서 캐시 적용 전/후 성능 차이를 정량화할 수 있는 비교 기준을 정의한다.
- 기존 캐시 특성화 테스트를 확장하거나 보강해 재현 가능한 before/after 자동화 검증을 만든다.
- 테스트 실행 결과를 근거로 전후 성능 이득을 수치로 정리한다.

# External Boundaries
- Spring Cache abstraction
  - `CacheManager`, `Cache`, `NoOpCacheManager`, `ConcurrentMapCacheManager`
- SheetPost 캐시 coordination
  - `SheetPostCacheCoordinator`
- SheetPost 조회 서비스 경로
  - `SheetPostApplicationService#getSheetPosts(...)`
- Local verification environment
  - `./gradlew test --tests ...`
  - `./gradlew build`
- Optional manual benchmark asset
  - `scripts/search-benchmark.js`

# In Scope
- before/after 기준을 동일 요청 기준 `cache disabled` 대 `cache enabled + warm hit`로 고정
- mocked backend latency 기반 JUnit 특성화 테스트 설계
- 응답시간과 backend 호출 수 감소를 함께 확인하는 검증 항목 정의
- 실행 결과를 문서화하고 build gate 결과를 기록

# Out of Scope
- Ehcache TTL, SWR, single-flight 정책 자체 변경
- 실제 Elasticsearch/Redis/DB 인프라를 띄워 운영 유사 부하 시험을 새로 설계
- 컨트롤러 API 계약이나 사용자 기능 변경
- `scripts/search-benchmark.js`를 필수 자동화 경로로 승격하는 작업

# Key Constraints
- 비교는 동일한 서비스 메서드와 동일한 요청 파라미터에서 이뤄져야 한다.
- 성능 비교 테스트는 환경 잡음에 취약하므로 절대 시간보다 개선 비율과 backend call 감소를 함께 본다.
- 기존 더러운 워킹트리를 건드리지 않고 테스트 관련 변경만 최소 범위로 추가한다.
- 최종 검증은 프로젝트 규칙에 따라 `./gradlew build` 결과까지 기록한다.

# Backlinks
- docs/work-units/cache/cache-performance-quantification-20260416-0933/index.md

# Discovery Hints (grep)
- grep -n "^# Scope" docs/product-specs/cache/cache-performance-quantification-20260416-0933/domain-boundary.md
