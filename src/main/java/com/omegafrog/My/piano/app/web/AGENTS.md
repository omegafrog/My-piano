# AGENTS: app/web/

## OVERVIEW
API surface + domain logic: controllers, services, entities/repos, infra implementations, DTOs, events, and API responses.

## STRUCTURE
```
web/
├── controller/
├── dto/
├── domain/
├── enums/
├── event/
├── exception/
├── infra/
├── response/
├── service/
└── vo/
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| REST endpoints | `src/main/java/com/omegafrog/My/piano/app/web/controller/` | Most feature entry points |
| Global exception handling | `src/main/java/com/omegafrog/My/piano/app/web/controller/ExceptionAdvisor.java` | `@RestControllerAdvice` |
| Services | `src/main/java/com/omegafrog/My/piano/app/web/service/` | Application/business logic |
| Entities + domain repos | `src/main/java/com/omegafrog/My/piano/app/web/domain/` | `@Entity` + repo interfaces |
| Infra implementations | `src/main/java/com/omegafrog/My/piano/app/web/infra/` | JPA/Redis/QueryDSL implementations |
| Kafka events | `src/main/java/com/omegafrog/My/piano/app/web/event/` | Producer + consumers; saga/compensation events |
| DTOs | `src/main/java/com/omegafrog/My/piano/app/web/dto/` | Request/response shapes |
| Error types | `src/main/java/com/omegafrog/My/piano/app/web/exception/` | Domain-specific exceptions |
| API responses | `src/main/java/com/omegafrog/My/piano/app/web/response/` | `API*Response` classes |

## CONVENTIONS
- Repository pattern: interfaces under `app/web/domain/**`, implementations under `app/web/infra/**`.
- Event-driven updates: Kafka topics are referenced from code (see `app/web/event/EventPublisher.java`).
- File upload pipeline: `app/web/domain/*File*Executor*` + async methods emit Kafka events on completion/failure.

## NOTES
- Kafka topics used here include `post-created-topic`, `post-updated-topic`, `post-deleted-topic`, `elasticsearch-failed-topic`, `compensation-topic`, `file-upload-completed-topic`, `file-upload-failed-topic`.
- File serving in dev is wired via `src/main/java/com/omegafrog/My/piano/app/WebConfig.java` (resource handlers) plus local executors under `src/main/java/com/omegafrog/My/piano/app/web/domain/`.

## ANTI-PATTERNS
- Do not add new topics ad-hoc in multiple places; centralize send names in `src/main/java/com/omegafrog/My/piano/app/web/event/EventPublisher.java`.
- Keep controller methods thin; heavy logic belongs in `src/main/java/com/omegafrog/My/piano/app/web/service/`.
