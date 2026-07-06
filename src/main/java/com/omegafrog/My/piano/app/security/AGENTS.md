# AGENTS: app/security/

## OVERVIEW
Spring Security configuration, session auth, OAuth2 hooks, and security-specific persistence.

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Main security wiring | `src/main/java/com/omegafrog/My/piano/app/security/SecurityConfig.java` | Multiple `SecurityFilterChain` beans per path |
| Session auth | `src/main/java/com/omegafrog/My/piano/app/security/SecurityConfig.java` | Stores authenticated users in HTTP session |
| Auth providers | `src/main/java/com/omegafrog/My/piano/app/security/provider/` | Admin/CommonUser providers |
| Handlers (login/logout/denied) | `src/main/java/com/omegafrog/My/piano/app/security/handler/` | Success/failure/logout/access denied |
| Role model | `src/main/java/com/omegafrog/My/piano/app/web/domain/user/authorities/` | `Role` enum |

## CONVENTIONS
- Path-specific security: `SecurityConfig` uses `securityMatcher(...)` and creates many chains.
- Auth state is stored in server-side HTTP sessions.

## ANTI-PATTERNS
- Avoid duplicating path matchers: `SecurityConfig` already contains overlapping chains; add new chains carefully to prevent unexpected matcher precedence.
- Do not log raw credential values; keep logs to ids/metadata only.
