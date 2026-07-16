# API

로컬 백엔드는 기본적으로 `http://localhost:8080`에서 실행된다.

## API 문서

- [Swagger UI](http://localhost:8080/swagger-ui/index.html)
- [OpenAPI JSON](http://localhost:8080/v3/api-docs)

두 canonical 경로는 로컬 런타임 검증에서 HTTP 200 응답을 확인했다.

## 로컬 검증에 사용하는 API

| 기능 | 메서드 | 경로 |
| --- | --- | --- |
| 상태 확인 | `GET` | `/healthcheck` |
| 커뮤니티 목록 | `GET` | `/api/v1/community/posts?size=100` |
| 커뮤니티 상세 | `GET` | `/api/v1/community/posts/{id}` |
| 악보 목록 | `GET` | `/api/v1/sheet-post?searchBackend=db&size=100` |
| 악보 상세 | `GET` | `/api/v1/sheet-post/{id}` |

정적 자산은 백엔드의 `/profiles/**`, `/thumbnails/**`, `/sheets/**` 경로로 제공된다. 전체 endpoint와 요청·응답 스키마는 실행 중인 서버의 Swagger UI 또는 OpenAPI JSON을 기준으로 확인한다.
