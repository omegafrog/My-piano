# AGENTS: app/utils/

## OVERVIEW
Cross-cutting utilities (mapping, auth helpers, serializers, performance logging, cache helpers).

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Mapping helpers | `src/main/java/com/omegafrog/My/piano/app/utils/MapperUtil.java` | Parses payment + error payloads |
| Auth helper | `src/main/java/com/omegafrog/My/piano/app/utils/AuthenticationUtil.java` | Shared auth utilities |
| DTO serialization | `src/main/java/com/omegafrog/My/piano/app/utils/OrderDtoCustomSerializer.java` | Custom order serialization |
| Perf logging | `src/main/java/com/omegafrog/My/piano/app/utils/logging/PerformanceLoggingAspect.java` | Aspect-based perf logs |

## CONVENTIONS
- Utilities are used across `app/web`, `app/security`, and external integrations.

## ANTI-PATTERNS
- Avoid turning utils into a dumping ground; prefer domain-specific helpers near their domains.
