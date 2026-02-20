# AGENTS: app/web/infra/

## OVERVIEW
Infrastructure implementations of domain repositories (JPA/QueryDSL/Redis-backed pieces).

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Sheet post persistence | `src/main/java/com/omegafrog/My/piano/app/web/infra/sheetPost/` | Custom sheet-post queries |
| Post persistence | `src/main/java/com/omegafrog/My/piano/app/web/infra/post/` | Post + video post storage |
| Lesson persistence | `src/main/java/com/omegafrog/My/piano/app/web/infra/lesson/` | Lesson storage |

## CONVENTIONS
- Implement the repository interfaces defined under `src/main/java/com/omegafrog/My/piano/app/web/domain/`.
- QueryDSL wiring comes from `src/main/java/com/omegafrog/My/piano/app/QueryDslConfig.java`.

## ANTI-PATTERNS
- Avoid placing new repository interfaces here; keep interfaces in domain packages.
