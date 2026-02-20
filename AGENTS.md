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
