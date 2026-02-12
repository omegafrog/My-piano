# AGENTS: app/web/domain/

## OVERVIEW
Domain model and repository interfaces: JPA entities, value types, and abstractions used by services. Also includes file storage executors.

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Sheet post entities/repos | `src/main/java/com/omegafrog/My/piano/app/web/domain/sheet/` | `SheetPost`, `Sheet`, repo interfaces |
| Community post entities/repos | `src/main/java/com/omegafrog/My/piano/app/web/domain/post/` | `Post`, `VideoPost`, repos |
| User entities/repos | `src/main/java/com/omegafrog/My/piano/app/web/domain/user/` | `User`, `SecurityUserRepository`, etc. |
| Orders/payments | `src/main/java/com/omegafrog/My/piano/app/web/domain/order/` | `Order`, `SellableItem`, repo |
| Relations (likes/scraps/follow) | `src/main/java/com/omegafrog/My/piano/app/web/domain/relation/` | Join entities for social actions |
| File storage abstraction | `src/main/java/com/omegafrog/My/piano/app/web/domain/UploadFileExecutor.java` | Interface used by `FileStorageExecutor` |
| Local storage executor | `src/main/java/com/omegafrog/My/piano/app/web/domain/LocalFileStorageExecutor.java` | Writes into `local.storage.base-path` |
| S3 upload executor | `src/main/java/com/omegafrog/My/piano/app/web/domain/S3UploadFileExecutor.java` | Uses `S3Template` + publishes events |
| Storage facade | `src/main/java/com/omegafrog/My/piano/app/web/domain/FileStorageExecutor.java` | Delegates to `UploadFileExecutor` |

## CONVENTIONS
- Repository interfaces live here; implementations live under `src/main/java/com/omegafrog/My/piano/app/web/infra/`.
- File upload operations are async (`@Async("ThreadPoolTaskExecutor")`) and emit Kafka events on completion/failure.

## ANTI-PATTERNS
- Avoid putting infrastructure-specific code (QueryDSL/JPA impl details, HTTP clients) into domain packages.
