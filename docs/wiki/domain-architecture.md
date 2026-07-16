# 도메인과 아키텍처

## 기존 구조

애플리케이션은 HTTP 요청을 받는 Controller, 업무 흐름을 조정하는 Service, Entity와 Repository 인터페이스가 있는 Domain, 저장소 구현을 제공하는 Infrastructure 계층으로 구성된다. 커뮤니티 게시글, 악보, 사용자와 댓글은 기존 도메인 모델과 저장소 경계를 그대로 사용한다.

로컬 초기 콘텐츠 적재기는 `local-seed` 프로필에서만 실행되며 기존 Repository를 통해 데이터를 저장한다. 프로필이 활성화되지 않은 환경에서는 seed 구성과 실행기가 등록되지 않는다.

## 로컬 실행 경계

- `local` 프로필은 Docker Compose 서비스 주소와 로컬 저장소 경로를 제공한다.
- `local-seed` 프로필은 사용자, 게시글, 악보와 댓글을 준비한다.
- 정적 자원 매핑은 `/profiles/**`, `/thumbnails/**`, `/sheets/**` 요청을 로컬 저장소의 검증된 파일에 연결한다.
- MySQL은 영속 데이터를, 두 Redis 인스턴스는 사용자·캐시 용도를, Elasticsearch는 검색 인프라를 담당한다.

초기 적재는 기준 데이터의 존재를 확인하는 sentinel 방식으로 중복 실행을 방지한다. 백엔드를 재기동해도 커뮤니티 8개와 악보 6개의 수량이 유지되는 것이 검증됐다.

## 아키텍처 영향

이번 로컬 배포 정비의 architecture impact는 `none`이다. 새 Entity, Aggregate, Bounded Context, 포트, Controller endpoint 또는 보안 규칙을 추가하지 않았다. 변경은 로컬 프로필, bootstrap, 정적 자원과 캐시 classpath 설정 경계 안에 있으며 기존 도메인 경계를 유지한다.
