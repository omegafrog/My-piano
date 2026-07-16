# My-Piano 프로젝트 위키

My-Piano는 피아노 악보 공유와 커뮤니티 기능을 제공하는 Spring Boot 백엔드다. 이 위키는 현재 코드와 로컬 배포 검증 결과를 기준으로 사용자 흐름, 구조, 실행 방법, API 확인 경로를 정리한다.

## 문서 안내

- [사용자 흐름](user-workflows.md): 커뮤니티와 악보 콘텐츠를 조회하는 주요 흐름
- [도메인과 아키텍처](domain-architecture.md): 기존 도메인 경계와 로컬 실행 변경의 영향
- [운영](operations.md): Docker 기반 로컬 기동, 포트, 재기동과 초기화
- [검증](verification.md): 자동 검증 범위와 확인된 결과
- [API](api.md): 로컬 API와 OpenAPI 문서 진입점
- [변경 이력](change-history.md): 로컬 배포 환경 정비 내역

## 로컬 환경 요약

`./scripts/local-deploy.sh`를 실행하면 Docker Compose가 MySQL, Elasticsearch, Redis 두 인스턴스와 백엔드를 시작한다. 백엔드는 `local,local-seed` 프로필로 실행되며 커뮤니티 게시글과 악보, 댓글, 프로필·썸네일·PDF 자산을 준비한다.

전체 로컬 검증은 `./scripts/verify-local-deploy.sh`로 실행한다. 이 검증은 목록과 상세 응답, 자산 응답, 콘텐츠 품질, 백엔드 재기동 후 초기 콘텐츠 안정성을 확인한다.
