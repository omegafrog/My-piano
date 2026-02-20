# AGENTS: app/web/service/

## OVERVIEW
Business logic for each domain (users, posts, sheet posts, lessons, orders, admin).

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Common user flows | `src/main/java/com/omegafrog/My/piano/app/web/service/admin/CommonUserService.java` | Used by `SecurityConfig` |
| Admin flows | `src/main/java/com/omegafrog/My/piano/app/web/service/admin/AdminUserService.java` | Admin auth + management |
| Sheet post core logic | `src/main/java/com/omegafrog/My/piano/app/web/service/SheetPostApplicationService.java` | Upload, metadata, search orchestration |
| Post core logic | `src/main/java/com/omegafrog/My/piano/app/web/service/PostApplicationService.java` | CRUD + event publishing |

## CONVENTIONS
- Service layer calls repository interfaces in `src/main/java/com/omegafrog/My/piano/app/web/domain/`.
- When emitting domain events, use `src/main/java/com/omegafrog/My/piano/app/web/event/EventPublisher.java`.

## ANTI-PATTERNS
- Do not put persistence implementation details here; keep JPA/QueryDSL specifics under `src/main/java/com/omegafrog/My/piano/app/web/infra/`.
