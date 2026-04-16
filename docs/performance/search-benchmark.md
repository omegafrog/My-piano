# SheetPost 검색 벤치마크

이 문서는 Elasticsearch 검색과 DB 대체 검색을 같은 사용자 검색 흐름에서 비교하는 벤치마크 방법을 설명합니다.

## 목적

- Elasticsearch 사용 여부에 따른 응답시간 차이를 측정한다.
- 실제 사용자 흐름에 가까운 `검색 -> 첫 번째 결과 상세 조회` 경로를 계측한다.
- 캐시가 있는 상태와 없는 상태를 분리해서 본다.

## 측정 대상

- 검색 목록 API
  - `GET /api/v1/sheet-post`
  - `searchBackend=es`
  - `searchBackend=db`
- 상세 API
  - `GET /api/v1/sheet-post/{id}`

## 전제

- 동일한 데이터셋에서 비교한다.
- ES와 DB 비교는 같은 검색어/필터/페이지/동시성으로 수행한다.
- 캐시 효과를 보기 위해 아래 두 모드로 각각 실행한다.
  - cold: 애플리케이션 재기동 직후
  - warm: 워밍업 이후

## 실행 방법

### Elasticsearch 경로

```bash
k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e SEARCH_BACKEND=es \
  -e SEARCH_SENTENCE=title \
  -e INSTRUMENT=GUITAR_ACOUSTIC \
  -e DIFFICULTY=MEDIUM \
  -e GENRE=BGM \
  scripts/search-benchmark.js
```

### DB 경로

```bash
k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e SEARCH_BACKEND=db \
  -e SEARCH_SENTENCE=title \
  -e INSTRUMENT=GUITAR_ACOUSTIC \
  -e DIFFICULTY=MEDIUM \
  -e GENRE=BGM \
  scripts/search-benchmark.js
```

## 읽는 법

- `search_latency_ms`
  - 사용자 검색 결과 목록이 나오는 데 걸린 시간
- `detail_latency_ms`
  - 검색 결과에서 하나를 눌렀을 때 상세 화면이 열리는 시간
- `journey_latency_ms`
  - 검색부터 상세 조회까지의 전체 체감 시간
- `p95`
  - 실사용자가 느끼는 상위 지연 구간을 보기 위한 핵심 지표

## 해석 기준

- Elasticsearch가 DB보다 낮은 `search_latency_ms`와 `journey_latency_ms`를 보이면 검색 고도화의 체감 이점이 있다고 본다.
- `detail_latency_ms`는 검색 백엔드보다 캐시와 조회 경로의 영향을 더 많이 받는다.
- warm 상태에서의 개선 폭이 크면 SWR/Single-flight/cache warm-up의 체감 효과가 있다.

## 참고

- 이 벤치마크는 `searchBackend=db`를 통해 Elasticsearch를 우회하는 비교 경로를 쓴다.
- 운영 기본값은 계속 Elasticsearch 경로이다.
