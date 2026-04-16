# Properties
doc_path: .codex/repository-settings.md
owner: Codex
status: active
last_updated: 2026-04-15

# Purpose
- Share repository-specific execution rules across executor and test_gate.

# Build & Test Commands
- build: `./gradlew build`
- unit/integration: `./gradlew test`
- e2e: `TBD`

# Test Policy
- Do not treat implementation as complete without at least the unit/integration command passing
- The test gate follows `.codex/test-gate.yaml`

# Environment Notes
- `./gradlew build` is the required post-change verification step in this repository, not an optional extra check.
- `src/test/resources/application-test.yml` expects MySQL on `localhost:3307` (`mypianotest`) and Redis on `16379` / `6380`.
- Some application flows also assume local Elasticsearch and other infra from `docker-compose.yml`; when the environment is incomplete, report the stage as `BLOCKED` instead of claiming success.
- Production settings are externalized and loaded via deployment scripts; do not infer prod behavior only from tracked `application*.yml`.
- Keep existing root and package-level `AGENTS.md` files authoritative for module-specific navigation rules.
