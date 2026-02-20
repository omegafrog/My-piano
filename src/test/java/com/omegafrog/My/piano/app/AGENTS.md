# AGENTS: src/test/java/.../app/

## OVERVIEW
Mix of Spring Boot integration tests (MockMvc) and JPA slice tests; relies on YAML-based test configuration.

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Controller/integration tests | `src/test/java/com/omegafrog/My/piano/app/web/controller/` | Typically `@SpringBootTest` + `@AutoConfigureMockMvc` |
| Repository/JPA tests | `src/test/java/com/omegafrog/My/piano/app/web/infrastructure/` | Uses `@DataJpaTest` + custom config |
| Shared test utilities | `src/test/java/com/omegafrog/My/piano/app/TestUtil.java` | MockMvc flows (register/login/etc.) |
| Cleanup/reset helpers | `src/test/java/com/omegafrog/My/piano/app/Cleanup.java` | Used in `@BeforeEach` to isolate tests |
| Test wiring | `src/test/java/com/omegafrog/My/piano/app/TestUtilConfig.java` | Provides beans for tests |
| JPA test wiring | `src/test/java/com/omegafrog/My/piano/app/DataJpaTestConfig.java` | Imported into `@DataJpaTest` tests |
| Test configuration | `src/test/resources/application-test.yml` | Uses MySQL `mypianotest` and local Redis/ES |

## CONVENTIONS
- Many controller tests are integration-style (full context) rather than `@WebMvcTest` slices.
- Repository tests often use `@AutoConfigureTestDatabase(replace = NONE)`; ensure the configured DB is available.

## ANTI-PATTERNS
- Avoid tests that depend on developer-local state (running services, leftover DB data) without using `Cleanup`.
- Do not print secrets or tokens in test logs.
