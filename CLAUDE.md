# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

My-Piano is a Spring Boot-based piano sheet music sharing platform with advanced features including payments, user
management, content search, and event-driven architecture. The application uses a hybrid storage approach with local
file storage in development and AWS S3 in production.

## Build and Development Commands

### Core Commands

```bash
# Build the application
./gradlew build

# Run the application (dev profile is default)
./gradlew bootRun

# Clean build
./gradlew clean build

# Run tests
./gradlew test

# Run a specific test class
./gradlew test --tests "com.omegafrog.My.piano.app.web.controller.PostControllerTest"

# Run a single test method
./gradlew test --tests "com.omegafrog.My.piano.app.web.controller.PostControllerTest.testCreatePost"
```

### Infrastructure Commands

```bash
# Start infrastructure services (Elasticsearch, Kafka, Kibana)
docker-compose up -d

# Stop infrastructure services
docker-compose down

# View logs
docker-compose logs -f kafka
docker-compose logs -f elasticsearch
```

## Architecture Overview

### Core Technologies

- **Spring Boot 3.2.6** with Java 17
- **MySQL** for primary data storage
- **Redis** for caching (view counts, like counts, user sessions)
- **Elasticsearch** for search functionality
- **Kafka** for event-driven architecture
- **AWS S3** for file storage (production) / Local filesystem (development)

### Key Architecture Patterns

#### Profile-Based Configuration

- **dev**: Local development with file storage, HTTP Elasticsearch
- **prod**: Production with S3 storage, secured Elasticsearch

#### Event-Driven Architecture

The application uses Kafka for asynchronous processing:

- **Post Events**: `PostCreatedEvent`, `PostUpdatedEvent`, `PostDeletedEvent`
- **Saga Pattern**: Compensation events for handling Elasticsearch failures
- **Topics**: `post-created-topic`, `post-updated-topic`, `post-deleted-topic`, `elasticsearch-failed-topic`,
  `compensation-topic`

#### File Storage Strategy

- **Development**: Files stored in `./local-storage/` directory
- **Production**: Files stored in AWS S3
- **Abstraction**: `FileStorageExecutor` provides unified interface

### Domain Structure

#### Core Domains

- **Sheet Posts**: PDF sheet music with thumbnails and metadata
- **Posts/Video Posts**: Community content (text posts, video posts)
- **Users**: Authentication, profiles, social features (following, liking)
- **Orders/Payments**: E-commerce functionality with Toss Payments integration
- **Lessons**: Educational content
- **Tickets**: Customer support system

#### Repository Pattern Implementation

- Interface repositories in `domain` packages
- JPA implementations in `infra` packages
- Redis implementations for caching layers
- Custom QueryDSL implementations for complex queries

### Security Architecture

- **JWT-based authentication** with refresh tokens
- **OAuth2 integration** (Google)
- **Role-based access control** (Admin, CommonUser)
- **Custom security filters** for token validation

### Batch Processing

- **View Count Persistence**: Scheduled jobs to persist Redis view counts to MySQL
- **Ranking Jobs**: Elasticsearch-based ranking calculations
- **Like Count Management**: Async processing for social features

## Important Configuration Notes

### Database Configuration

- Development uses local MySQL: `mypianodev` database
- Connection details in `application-dev.properties`
- JPA auto-DDL enabled for development

### Elasticsearch Configuration

- **Development**: HTTP connection to localhost:9200
- **Production**: HTTPS with API key authentication
- Custom `ElasticSearchInstance` for search operations

### File Upload Handling

- PDF processing with **Apache PDFBox**
- Thumbnail generation with blur effects for preview
- Async file processing with `@Async` annotations

### Kafka Configuration

- Bootstrap servers: `localhost:9092`
- Consumer group: `mypiano-consumer-group`
- Auto-create topics enabled
- JSON serialization for events

## Development Guidelines

### Testing Strategy

- Test classes located in `src/test/java`
- Controller tests use `@WebMvcTest`
- Repository tests use `@DataJpaTest`
- Integration tests available for key workflows

### Code Organization

- **Controllers**: REST endpoints in `web/controller`
- **Services**: Business logic in `web/service`
- **Domain**: Entity classes and repository interfaces
- **DTOs**: Data transfer objects in `web/dto`
- **Events**: Kafka events and consumers in `web/event`
- **Configuration**: Spring configurations in root `app` package

### External Integrations

- **Toss Payments**: Korean payment gateway integration
- **Firebase**: Push notification service
- **Google OAuth2**: Social authentication
- **AWS S3**: File storage (profile-dependent)

### Performance Considerations

- Redis caching for view counts and like counts
- Async processing for file uploads and thumbnails
- Elasticsearch for fast search across sheet music
- Connection pooling for HTTP clients
- Batch processing for periodic data synchronization

## Local Development Setup

1. Start infrastructure: `docker-compose up -d`
2. Ensure MySQL is running with `mypianodev` database
3. Redis should be available on default ports (6379, 6380)
4. Run application: `./gradlew bootRun`
5. Access Swagger UI: http://localhost:8080/swagger-ui.html
6. Kibana dashboard: http://localhost:5601

The application creates a `local-storage` directory for file uploads in development mode.

## Git Commit and PR Guidelines

### Commit Message Style
- 모든 커밋 메시지는 한글로 작성합니다
- 형식: `타입: 변경사항 요약`
- 타입 예시:
  - `feat`: 새로운 기능 추가
  - `fix`: 버그 수정  
  - `refactor`: 코드 리팩토링
  - `docs`: 문서 변경
  - `test`: 테스트 코드 추가/수정
  - `style`: 코드 포맷팅, 세미콜론 누락 등
  - `chore`: 빌드 설정, 패키지 매니저 설정 등

### Pull Request Guidelines
- PR 제목과 본문은 한글로 작성합니다
- 제목 형식: `[타입] 기능/변경사항 요약`
- 본문에는 다음 내용을 포함합니다:
  - **변경 사항**: 무엇을 변경했는지
  - **변경 이유**: 왜 이 변경이 필요한지
  - **테스트 계획**: 어떻게 테스트했는지
  - **영향 범위**: 다른 기능에 미치는 영향

## Code Changes and Build Verification

**IMPORTANT**: 코드 변경을 포함하는 모든 작업이 완료되었을 때는 반드시 빌드와 컴파일을 진행하고 오류가 없는지 확인해야 합니다.

```bash
# 코드 변경 후 빌드 확인
./gradlew build
```

이 과정을 통해 코드 변경사항이 컴파일 오류나 빌드 실패를 일으키지 않는지 검증합니다.
