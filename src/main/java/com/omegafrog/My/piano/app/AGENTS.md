# AGENTS: app/

## OVERVIEW
Spring configuration + cross-cutting wiring (Redis/Async/Web/Swagger/QueryDSL), plus profile-specific beans (S3 in `prod`).

## STRUCTURE
```
app/
├── AsyncConfig.java
├── GlobalConfig.java
├── KafkaConfig.java
├── QueryDslConfig.java
├── RedisConfig.java
├── SwaggerConfig.java
├── WebConfig.java
├── batch/
├── external/
├── security/
├── utils/
└── web/
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Thread pool / async | `src/main/java/com/omegafrog/My/piano/app/AsyncConfig.java` | Executor bean name is `ThreadPoolTaskExecutor` |
| Redis cache + templates | `src/main/java/com/omegafrog/My/piano/app/RedisConfig.java` | Two Redis endpoints (`user` + `cache`); cache TTL 30m |
| Static resource mapping | `src/main/java/com/omegafrog/My/piano/app/WebConfig.java` | Serves `./local-storage/*` as HTTP resources |
| OpenAPI/Swagger | `src/main/java/com/omegafrog/My/piano/app/SwaggerConfig.java` | Adds HTTP bearer scheme |
| QueryDSL | `src/main/java/com/omegafrog/My/piano/app/QueryDslConfig.java` | Provides `JPAQueryFactory` |
| Global wiring + integrations | `src/main/java/com/omegafrog/My/piano/app/GlobalConfig.java` | S3 (prod), Toss, Firebase, ES helper wiring |
| Elasticsearch client | `src/main/java/com/omegafrog/My/piano/app/external/elasticsearch/ElasticSearchConfig.java` | ApiKey header; enables ES repositories |

## CONVENTIONS
- Async methods typically use `@Async("ThreadPoolTaskExecutor")`.
- `prod` profile wires AWS S3 (`S3Client`, `S3Template`) and selects S3 upload executor.

## NOTES
- `src/main/java/com/omegafrog/My/piano/MyPianoApplication.java` excludes Spring Boot's Elasticsearch client autoconfig; ES wiring happens via `app/external/elasticsearch/ElasticSearchConfig.java`.
- `src/main/java/com/omegafrog/My/piano/app/KafkaConfig.java` exists but currently defines no beans.
- Common config keys used here: `async.pool.*`, `async.queue.capacity`, `spring.redis.user.*`, `spring.redis.cache.*`, `local.storage.base-path`.

## ANTI-PATTERNS
- Avoid introducing secrets into tracked YAML/properties. Prefer external config (see `scripts/deploy.sh`).
- Be careful with bean names and `@Value` keys; many configs assume exact property paths.
