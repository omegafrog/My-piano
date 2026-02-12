# AGENTS: app/web/controller/

## OVERVIEW
REST API controllers (mostly JSON) under `/api/v1/**`.

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Global exception mapping | `src/main/java/com/omegafrog/My/piano/app/web/controller/ExceptionAdvisor.java` | `@RestControllerAdvice` |
| Community posts | `src/main/java/com/omegafrog/My/piano/app/web/controller/PostController.java` | Creates/updates posts; emits Kafka events |
| Sheet posts + search | `src/main/java/com/omegafrog/My/piano/app/web/controller/SheetPostController.java` | Upload/search endpoints |
| File uploads | `src/main/java/com/omegafrog/My/piano/app/web/controller/FileUploadController.java` | Upload pipeline entry |
| Payments/orders | `src/main/java/com/omegafrog/My/piano/app/web/controller/OrderController.java` | Toss integration paths |
| Auth/user | `src/main/java/com/omegafrog/My/piano/app/web/controller/UserController.java` | Login/register/profile |
| OAuth2 callbacks | `src/main/java/com/omegafrog/My/piano/app/web/controller/SecurityController.java` | OAuth2 glue |

## CONVENTIONS
- Controllers are thin; business logic lives in `src/main/java/com/omegafrog/My/piano/app/web/service/`.
- Exception responses are centralized in `ExceptionAdvisor`.

## ANTI-PATTERNS
- Avoid hard-coding Kafka topic names here; publish via `src/main/java/com/omegafrog/My/piano/app/web/event/EventPublisher.java`.
