# 검증

## 자동 smoke 검증

백엔드를 로컬로 기동한 뒤 다음 명령을 실행한다.

```bash
./scripts/verify-local-deploy.sh
```

검증 범위는 다음과 같다.

- `/healthcheck` 응답
- 커뮤니티 게시글 목록 8개와 각 상세 응답
- 악보 목록 6개와 각 상세 응답
- 응답에 포함된 프로필, 썸네일, PDF 자산 URL
- 자산 HTTP 상태와 콘텐츠 유형
- 콘텐츠 문자열의 금칙어 포함 여부
- 백엔드 재기동 후 초기 콘텐츠 수량 유지 여부

## 확인된 결과

| 검증 대상 | 결과 |
| --- | --- |
| 전체 build와 계획된 focused test | 통과 |
| 백엔드 health | 통과 |
| 커뮤니티 목록·상세 8개 | 통과 |
| 악보 목록·상세 6개 | 통과 |
| 프로필 5개, 썸네일 6개, PDF 6개 | 17개 모두 HTTP 200 및 올바른 콘텐츠 유형 |
| 콘텐츠 품질 검사 | 통과 |
| 재기동 후 seed 안정성 | 수량 유지 |
| Swagger UI `/swagger-ui/index.html` | HTTP 200 |
| OpenAPI `/v3/api-docs` | HTTP 200 |

프런트엔드는 Node.js 20 환경에서 설치, 빌드, 시작과 3000 포트 응답까지 통과했다. 백엔드 연동은 API 접두사 누락에 따른 CORS 오류로 실패했으며 [Issue #3](https://github.com/omegafrog/my-piano-frontend/issues/3)에 기록됐다. 이 실패와 별도로 백엔드 전체 smoke 검증은 성공했다.

## 초기 콘텐츠 보호 범위

초기 콘텐츠 구성은 `local-seed` 프로필로 제한된다. 프로필 기반 테스트에서 운영 프로필에 seed가 활성화되지 않는 것을 확인했으며, 재기동 검증에서 sentinel 기반 중복 방지가 확인됐다.
