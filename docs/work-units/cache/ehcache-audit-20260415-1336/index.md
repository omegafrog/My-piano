# Ehcache 캐시 검증 및 비판적 분석

- task: `ehcache-audit-20260415-1336`
- domain: `cache`
- harvest: `docs/use-case-harvests/cache/ehcache-audit-20260415-1336/use-case-harvest.md`
- scope
  - 기존 ehcache 검증 수단 확인
  - 관련 테스트 실행
  - 성능 전/후 검증 공백 보강
  - 캐시 코드 비판적 분석
- status: in-progress
- current-findings
  - 기존 자동화는 SWR/single-flight/warm-up 정합성 중심이며, cold/warm 전후 응답 차이를 직접 보여주는 테스트는 없었다.
  - 이를 보강하기 위해 `SheetPostCachePerformanceCharacterizationTest`를 추가했다.
  - 수동 `k6` 벤치마크 스크립트는 이미 있으나, 현재 로컬 서버 미기동으로 실행은 보류됐다.
  - 코드 리뷰에서 검색 순서 보존, 개인화 필드, count cache durability, metrics 순회 비용이 핵심 리스크로 보인다.
