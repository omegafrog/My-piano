# My-Piano 🎹

> 피아노 악보 공유 및 커뮤니티 플랫폼

My-Piano는 Spring Boot 기반의 피아노 악보 공유 플랫폼으로, 결제 시스템, 사용자 관리, 콘텐츠 검색, 이벤트 기반 아키텍처를 갖춘 종합적인 음악 플랫폼입니다.

## 🚀 주요 기능

### 악보 관리 시스템
- **PDF 악보 업로드 및 처리**: Apache PDFBox를 이용한 악보 처리
- **썸네일 자동 생성**: 블러 효과가 적용된 미리보기 생성
- **악보 검색 및 필터링**: Elasticsearch 기반 고성능 검색
- **카테고리별 분류**: 장르, 난이도, 악기별 체계적 분류

### 커뮤니티 기능
- **게시글 시스템**: 일반 게시글 및 동영상 게시글 지원
- **댓글 및 답글**: 계층형 댓글 시스템
- **좋아요 및 스크랩**: 실시간 좋아요/스크랩 기능
- **팔로우 시스템**: 사용자간 팔로우/팔로워 관계

### 전자상거래 시스템
- **Toss Payments 연동**: 한국형 결제 게이트웨이
- **악보 및 강의 구매**: 디지털 콘텐츠 판매
- **캐시 충전 시스템**: 포인트 기반 결제
- **주문 관리**: 구매 이력 및 환불 처리

### 사용자 관리
- **JWT 기반 인증**: 액세스 토큰 및 리프레시 토큰
- **OAuth2 소셜 로그인**: Google 로그인 지원
- **역할 기반 권한 관리**: 관리자/일반 사용자 구분
- **프로필 관리**: 사용자 정보 및 활동 내역

### 고객 지원
- **티켓 시스템**: 문의사항 및 신고 처리
- **Push 알림**: Firebase를 통한 실시간 알림
- **관리자 도구**: 콘텐츠 관리 및 사용자 제재

## 🏗️ 시스템 아키텍처

### 기술 스택
- **Backend**: Spring Boot 3.2.6, Java 17
- **데이터베이스**: MySQL (Primary), Redis (Cache)
- **검색 엔진**: Elasticsearch 8.15.0
- **비동기 이벤트 처리**: DB Outbox Polling
- **파일 저장소**: AWS S3 (Production) / Local Storage (Development)
- **보안**: Spring Security, JWT, OAuth2
- **문서화**: Swagger/OpenAPI 3

### 아키텍처 패턴

#### 이벤트 기반 아키텍처
```
Post Creation → Outbox Event 저장 → Polling Processor → Elasticsearch Indexing
     ↓
Compensation Events (실패 시 데이터 정합성 보장)
```

#### 레이어드 아키텍처
```
Controller Layer → Service Layer → Domain Layer → Infrastructure Layer
```

#### 프로필 기반 환경 분리
- **dev**: 로컬 개발 환경 (파일 저장소, HTTP Elasticsearch)
- **prod**: 운영 환경 (S3 저장소, 보안 Elasticsearch)

### 주요 설계 패턴
- **Repository Pattern**: 데이터 접근 추상화
- **Strategy Pattern**: 다양한 콘텐츠 타입 처리
- **Saga Pattern**: 분산 트랜잭션 보상 처리
- **Factory Pattern**: 객체 생성 추상화

## 📊 성능 최적화

성능 개선에 대한 자세한 내용은 [성능 개선사항](./PERFORMANCE.md)을 참조하세요.

### 주요 성능 특징
- **Redis 캐싱**: 조회수, 좋아요 수 고속 처리
- **비동기 처리**: 파일 업로드 및 썸네일 생성
- **Elasticsearch 검색**: 전문 검색 엔진을 통한 빠른 콘텐츠 검색
- **커넥션 풀링**: HTTP 클라이언트 최적화
- **배치 처리**: 주기적 데이터 동기화

## 🗂️ 도메인 모델

### 핵심 엔티티
- **SheetPost**: PDF 악보 게시물
- **Post/VideoPost**: 커뮤니티 게시물
- **User**: 사용자 정보 및 인증
- **Order**: 주문 및 결제 정보
- **Lesson**: 강의 콘텐츠
- **Ticket**: 고객 지원 티켓

### 관계형 엔티티
- **UserPurchasedSheetPost**: 사용자-악보 구매 관계
- **UserLikedPost**: 사용자-게시물 좋아요 관계
- **FollowedUser**: 사용자간 팔로우 관계

## 🛠️ 개발 환경 설정

### 필수 요구사항
- Java 17
- MySQL 8.0+
- Docker & Docker Compose

### 로컬 개발 환경 구축

1. **인프라 서비스 시작**
```bash
docker-compose up -d
```

2. **애플리케이션 빌드 및 실행**
```bash
./gradlew build
./gradlew bootRun
```

3. **서비스 접속**
- API 서버: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Kibana: http://localhost:5601

### 테스트 실행
```bash
# 전체 테스트
./gradlew test

# 특정 클래스 테스트
./gradlew test --tests "PostControllerTest"

# 특정 메서드 테스트
./gradlew test --tests "PostControllerTest.testCreatePost"
```

## 🔧 주요 설정

### 데이터베이스 설정
- 개발: MySQL `mypianodev` 데이터베이스
- JPA Auto-DDL 활성화 (개발 환경)

### 파일 저장소 설정
- 개발: `./local-storage/` 디렉토리
- 운영: AWS S3 버킷

### Outbox 설정
- Post/SheetPost/FileUpload 이벤트를 DB Outbox로 저장
- 스케줄러 Polling Processor가 인덱싱/후속 처리를 비동기로 수행

## 📝 API 문서

프로젝트 실행 후 Swagger UI에서 전체 API 문서를 확인할 수 있습니다:
http://localhost:8080/swagger-ui.html

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/amazing-feature`)
3. Commit your Changes (`git commit -m 'feat: 놀라운 기능 추가'`)
4. Push to the Branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### 커밋 메시지 규칙
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `refactor`: 코드 리팩토링
- `docs`: 문서 변경
- `test`: 테스트 코드
- `style`: 코드 포맷팅
- `chore`: 빌드 설정 등

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 🔗 관련 링크

- [성능 개선사항](./PERFORMANCE.md)
- [API 문서](http://localhost:8080/swagger-ui.html)
- [Kibana 대시보드](http://localhost:5601)
