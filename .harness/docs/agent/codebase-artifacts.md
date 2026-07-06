# Existing Codebase Artifacts

## Repository Inventory

- Description: Spring Boot Java 17 backend for piano sheet sharing and community with payments, Elasticsearch, Redis, and Kafka.
- Technologies: Java/Gradle
- Manifests: `build.gradle`, `settings.gradle`
- Source roots: `src`, `harness_codex`
- Test roots: `tests`
- Docs roots: `docs`, `README.md`, `AGENTS.md`
- Config files: `.codex/config.toml`, `.codex/openai.yaml`, `.gitignore`, `.harness/workflows`, `AGENTS.md`
- Workflow docs: `docs/design`, `docs/changes`, `docs/use-cases`, `docs/maintenance`, `docs/plans`, `.harness/workflows`
- Commands:
  - List files: `rg --files`
  - Search text: `rg -n "<pattern>"`
  - Git status: `git status --porcelain=v1 -uno`
  - Diff stat: `git diff --stat`
  - Python tests: `./venv/bin/python3 -m pytest -q -s`
  - Harness CLI help: `python3 -m harness_codex --help`
  - Gradle tests: `./gradlew test`

## Reverse-Engineered Implementation

Semantic implementation artifacts were not generated because the LLM analysis did not complete (status: skipped (disabled)). Run `harness init` with LLM analysis enabled to reverse-engineer code-level behavior.

## Evidence Policy

- Source and test paths are implementation evidence.
- Generated summaries are discovery aids, not canonical product requirements.
- Validate findings against current code before using them for workflow decisions.
