# 운영

## 준비 사항

- Windows 11과 WSL2 Ubuntu 24.04
- Docker와 Docker Compose
- Bash, curl, Python 3

로컬 Compose 실행에는 운영 비밀정보가 필요하지 않다. 저장소 루트에서 아래 명령을 실행한다.

## 빠른 시작

```bash
./scripts/local-deploy.sh
```

스크립트는 다음 순서로 실행된다.

1. MySQL, Elasticsearch, 사용자용 Redis와 캐시용 Redis를 시작한다.
2. Elasticsearch가 준비되면 Nori 분석기용 인덱스 템플릿을 설정한다.
3. 백엔드를 `local,local-seed` 프로필로 시작한다.
4. `/healthcheck` 성공을 기다린다.
5. 커뮤니티와 악보 목록에 초기 콘텐츠가 준비될 때까지 기다린다.

## 로컬 포트

| 구성 요소 | 호스트 포트 | 용도 |
| --- | ---: | --- |
| 백엔드 | 8080 | HTTP API와 정적 자산 |
| MySQL | 3307 | 로컬 데이터베이스 |
| Elasticsearch | 9200 | 검색 엔진 |
| 사용자 Redis | 16379 | 사용자 관련 Redis |
| 캐시 Redis | 6380 | 애플리케이션 캐시 |

## 상태 확인

```bash
docker compose --profile local ps
docker compose --profile local logs --tail=200 app-local
curl -fsS http://localhost:8080/healthcheck
```

목록·상세·자산과 재기동 안정성까지 확인하려면 전체 검증을 실행한다.

```bash
./scripts/verify-local-deploy.sh
```

검증 항목 하나라도 실패하면 명령은 0이 아닌 종료 코드로 끝난다.

## 수명주기

데이터 볼륨을 유지하며 백엔드만 재기동한다.

```bash
docker compose --profile local restart app-local
```

전체 로컬 서비스를 중지한다.

```bash
docker compose --profile local down
```

MySQL, Elasticsearch와 Redis 볼륨까지 삭제해 초기화한 뒤 다시 기동한다.

```bash
docker compose --profile local down -v
./scripts/local-deploy.sh
```

## 프런트엔드

프런트엔드는 백엔드와 별도로 3000 포트에서 실행할 수 있다. Node.js 20 환경의 설치·빌드·기동은 성공했지만 현재 API 접두사 문제로 백엔드 연동 시 CORS 오류가 재현된다. 조치 상태는 [프런트엔드 Issue #3](https://github.com/omegafrog/my-piano-frontend/issues/3)에서 확인한다. 프런트엔드 문제와 관계없이 백엔드 배포와 검증은 위 명령으로 독립 수행할 수 있다.
