# AGENTS: app/web/exception/

## OVERVIEW
Domain-specific runtime exceptions used by controllers/services; surfaced to clients via `ExceptionAdvisor`.

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Global mapping to API errors | `src/main/java/com/omegafrog/My/piano/app/web/controller/ExceptionAdvisor.java` | 400/500 conversion |
| Payment-related errors | `src/main/java/com/omegafrog/My/piano/app/web/exception/payment/` | Toss + order errors |
| Order errors | `src/main/java/com/omegafrog/My/piano/app/web/exception/order/` | Order flow exceptions |
| Toss errors | `src/main/java/com/omegafrog/My/piano/app/web/exception/toss/` | Toss API error parsing |

## CONVENTIONS
- Exceptions are typically unchecked and carry user-facing messages.

## ANTI-PATTERNS
- Avoid leaking internal exception causes/messages that may contain sensitive details.
