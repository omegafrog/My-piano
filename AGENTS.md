# PROJECT KNOWLEDGE BASE

**Generated:** 2026-02-12 (KST)
**Branch:** master
**Commit:** 9999b28

## OVERVIEW
My-Piano is a Spring Boot (Java 17) backend for piano sheet sharing + community, with payments, search (Elasticsearch), caching (Redis), and Kafka-based events.

## STRUCTURE
```
./
├── src/
│   ├── main/
│   │   ├── java/                  # Application code (Spring Boot)
│   │   └── resources/             # application*.yml (some secrets tracked)
│   └── test/
│       ├── java/                  # Spring Boot tests + JPA tests
│       └── resources/             # application-test.yml, etc.
├── scripts/                       # Deployment hook scripts (CodeDeploy)
├── docker-compose.yml             # Local infra (Kafka/ES/Kibana/Redis)
├── Jenkinsfile                    # Jenkins pipeline (build + docker image)
├── appspec.yml                    # CodeDeploy spec
├── build.gradle / settings.gradle # Gradle (Groovy) build
└── CLAUDE.md                      # Project-specific rules + commands
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Spring Boot entry point | `src/main/java/com/omegafrog/My/piano/MyPianoApplication.java` | Excludes Elasticsearch autoconfig; enables scheduling; writes PID file |
| REST endpoints | `src/main/java/com/omegafrog/My/piano/app/web/controller/` | 17 controllers; JSON APIs under `/api/v1/**` |
| Business logic | `src/main/java/com/omegafrog/My/piano/app/web/service/` | Service layer (domain-oriented) |
| Entities + domain repos | `src/main/java/com/omegafrog/My/piano/app/web/domain/` | `@Entity` + repository interfaces live here |
| Infra repo implementations | `src/main/java/com/omegafrog/My/piano/app/web/infra/` | JPA/Redis/QueryDSL implementations |
| Kafka events (producers/consumers) | `src/main/java/com/omegafrog/My/piano/app/web/event/` | Topic names are hard-coded in publisher/consumers |
| Security (JWT/OAuth2) | `src/main/java/com/omegafrog/My/piano/app/security/` | Many `SecurityFilterChain` beans + JWT filters |
| External integrations | `src/main/java/com/omegafrog/My/piano/app/external/` | Elasticsearch client/config + Toss payment client |
| Batch + scheduling | `src/main/java/com/omegafrog/My/piano/app/batch/` | Spring Batch jobs + `@Scheduled` scheduler |
| Global Spring configuration | `src/main/java/com/omegafrog/My/piano/app/*.java` | Redis/Async/Web/Swagger/QueryDSL, plus GlobalConfig wiring |
| Tests | `src/test/java/` | Mix of `@SpringBootTest` + MockMvc and `@DataJpaTest` |
| Local infra/dev deps | `docker-compose.yml` | ES 8.15.0, Kafka, Redis (2 instances), Kibana |
| Deploy pipeline | `Jenkinsfile`, `appspec.yml`, `scripts/deploy.sh` | Jenkins builds + pushes image; CodeDeploy pulls config from S3 |

## CONVENTIONS
- **Commit/PR language**: Korean commit messages; format `타입: 변경사항 요약` (see `CLAUDE.md`).
- **After code changes**: run `./gradlew build` (explicitly required in `CLAUDE.md`).
- **Profiles**: base `src/main/resources/application.yml` activates `dev` and includes `secret`.
- **Prod config source**: deployment script uses `-Dspring.config.location=.../application-prod.properties` (externalized config), not the tracked YAMLs.
- **Java LSP**: not configured in this environment; prefer `grep`/`ast-grep` for navigation.

## HARVESTER / ORCHESTRATION GUARDRAILS
- `use-case-harvester`는 요구사항 수집 전용이다. 구현, 리팩터링, 테스트 수정, 실행 검증을 절대 수행하지 않는다.
- `use-case-harvester`가 수정할 수 있는 파일은 `docs/use-case-harvests/<domain>/<task>/use-case-harvest.md`, `docs/work-units/<domain>/<task>/index.md`, 그리고 필요한 경우 `.codex/stack-profile.yaml`만이다.
- `orchestrate-plan` 플로우에서는 사용자 명시 승인 전까지 `oracle` 이하 단계로 진행하지 않는다.
- 코드와 테스트(`src/**`, `build.gradle`, `settings.gradle`, 스크립트)는 `executor` 단계만 수정할 수 있다.
- 계획/설계/검증 문서(`docs/**`, `.codex/**` 메타 문서)는 `oracle`, `doc_writer`, `doc_verify`, `execute_writer`, `closer`만 수정할 수 있다.

## PR 작성 요령
- PR 제목/본문은 한국어로 작성한다.
- PR 본문에는 최소 `변경 배경`, `주요 변경사항`, `테스트/검증 결과`, `영향 범위 및 리스크`를 포함한다.
- `주요 변경사항`은 파일 단위 또는 기능 단위로 구체적으로 작성하고, 누락 없이 모든 변경을 기록한다.
- 테스트 명령(`./gradlew build`, `./gradlew test --tests ...` 등)과 성공/실패 원인을 명시한다.
- 기존 실패가 있으면 신규 이슈와 구분해서 적고, 재현 조건과 후속 조치를 함께 적는다.
- 보안/인증/권한 변경이 있으면 동작 변화(예: 200/401/403)를 시나리오로 요약한다.

## ANTI-PATTERNS (THIS PROJECT)
- Do not commit real secrets/credentials. `.gitignore` already excludes `src/main/resources/firebase-admin.json` and `src/main/resources/application**.properties`.
- Do not rely on `build/`, `bin/`, `local-storage/`, `.gradle/` contents in code reviews; they are generated/runtime artifacts.

## COMMANDS
```bash
# Build / run / test
./gradlew build
./gradlew bootRun
./gradlew test

# Infra (Kafka/ES/Kibana/Redis)
docker-compose up -d
docker-compose down
docker-compose logs -f kafka
docker-compose logs -f elasticsearch
```

## NOTES
- Local file serving: `src/main/java/com/omegafrog/My/piano/app/WebConfig.java` maps `./local-storage/*` to `/sheets/**`, `/thumbnails/**`, `/profiles/**`.
- S3 + local storage: `src/main/java/com/omegafrog/My/piano/app/GlobalConfig.java` wires S3 beans under `prod` profile; local executor exists for dev.
- Kafka topics: see `src/main/java/com/omegafrog/My/piano/app/web/event/EventPublisher.java` and consumers under `src/main/java/com/omegafrog/My/piano/app/web/event/`.
