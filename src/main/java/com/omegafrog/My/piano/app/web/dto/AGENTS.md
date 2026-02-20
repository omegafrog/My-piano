# AGENTS: app/web/dto/

## OVERVIEW
Request/response DTOs grouped by feature area (sheet posts, posts, users, orders, tickets, etc.).

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Sheet post DTOs | `src/main/java/com/omegafrog/My/piano/app/web/dto/sheetPost/` | Upload/search/detail payloads |
| Community post DTOs | `src/main/java/com/omegafrog/My/piano/app/web/dto/post/` | Post create/update/list payloads |
| Order DTOs | `src/main/java/com/omegafrog/My/piano/app/web/dto/order/` | Order/payment payloads |
| User DTOs | `src/main/java/com/omegafrog/My/piano/app/web/dto/user/` | Register/login/profile payloads |

## CONVENTIONS
- DTO naming generally mirrors controller endpoints and service methods.
- Custom serialization exists for some DTOs (see `src/main/java/com/omegafrog/My/piano/app/utils/OrderDtoCustomSerializer.java`).

## ANTI-PATTERNS
- Avoid using JPA entities directly as API response bodies; keep them separated via DTOs.
