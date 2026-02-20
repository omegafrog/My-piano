# AGENTS: app/security/

## OVERVIEW
Spring Security configuration, JWT auth, OAuth2 hooks, and security-specific persistence (JPA + Redis).

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Main security wiring | `src/main/java/com/omegafrog/My/piano/app/security/SecurityConfig.java` | Multiple `SecurityFilterChain` beans per path |
| JWT token parsing/claims | `src/main/java/com/omegafrog/My/piano/app/security/jwt/TokenUtils.java` | Creates + validates JWT claims |
| JWT request filter | `src/main/java/com/omegafrog/My/piano/app/security/filter/JwtTokenFilter.java` | Sets SecurityContext from access token |
| Auth providers | `src/main/java/com/omegafrog/My/piano/app/security/provider/` | Admin/CommonUser providers |
| Handlers (login/logout/denied) | `src/main/java/com/omegafrog/My/piano/app/security/handler/` | Success/failure/logout/access denied |
| Refresh token storage | `src/main/java/com/omegafrog/My/piano/app/security/infrastructure/redis/` | Redis-backed refresh tokens |
| Role model | `src/main/java/com/omegafrog/My/piano/app/web/domain/user/authorities/` | `Role` enum |

## CONVENTIONS
- Path-specific security: `SecurityConfig` uses `securityMatcher(...)` and creates many chains.
- Tokens: access token in `Authorization: Bearer ...`; refresh token stored as `HttpOnly` cookie.

## ANTI-PATTERNS
- Avoid duplicating path matchers: `SecurityConfig` already contains overlapping chains; add new chains carefully to prevent unexpected matcher precedence.
- Do not log raw token values; keep logs to ids/metadata only.
