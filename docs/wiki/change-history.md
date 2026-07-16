# 변경 이력

## CHG-20260716-001

로컬에서 My-Piano 백엔드와 필수 인프라를 한 번에 배포하고, 화면 확인에 사용할 수 있는 자연스러운 초기 콘텐츠와 정상 자산을 제공하도록 실행 환경을 정비했다.

### 주요 변경

- Docker Compose에 `local` 프로필의 백엔드 서비스와 MySQL 초기화 구성을 추가했다.
- `application-local.yml`로 로컬 MySQL, Redis, Elasticsearch와 파일 저장소 연결을 분리했다.
- `local-seed` 전용 적재기로 사용자 5명, 커뮤니티 게시글 8개, 악보 6개와 댓글을 제공했다.
- 프로필 SVG 5개, 썸네일 SVG 6개와 PDF 6개를 로컬 자산으로 추가하고 `/profiles/**`, `/thumbnails/**`, `/sheets/**`에서 제공했다.
- `scripts/local-deploy.sh`에 인프라 기동, Elasticsearch Nori 구성, 백엔드 health와 초기 콘텐츠 준비 대기를 구현했다.
- `scripts/verify-local-deploy.sh`에 목록·상세·자산·콘텐츠 품질과 재기동 안정성 검증을 구현했다.
- 로컬 프로필, seed, 정적 자산과 배포 계약을 자동 테스트로 검증했다.

### 호환성과 구조 영향

도메인 Entity, Aggregate, Bounded Context, 기존 HTTP endpoint와 보안 규칙은 변경하지 않았다. 초기 콘텐츠는 `local-seed` 프로필에서만 활성화되며 운영 환경에서는 비활성화된다.

### 프런트엔드 확인

프런트엔드의 Node.js 20 설치·빌드·기동은 성공했다. API 경로 접두사 누락으로 발생한 CORS 연동 실패는 [my-piano-frontend Issue #3](https://github.com/omegafrog/my-piano-frontend/issues/3)에 재현 정보와 백엔드 독립 성공 근거를 기록했다.
