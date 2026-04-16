---
name: use-case-harvester
description: >
  interpret the latest request, classify work into UC/UI/TECH/TEST/DOC,
  create docs/use-case-harvests/<domain>/<task>/use-case-harvest.md,
  and judge oracle readiness
---

# Use Case Harvester

This skill reads the latest user request and creates a new run-scoped harvest document.

Core rules:
- Create a new `<task>` identity on each invocation.
- Update `docs/use-case-harvests/<domain>/<task>/use-case-harvest.md`.
- Update `docs/work-units/<domain>/<task>/index.md`.
- Refine `.codex/stack-profile.yaml` when critical fields are missing.
- Never implement anything during harvester execution.
- Never modify `src/**`, Gradle files, runtime scripts, tests, or downstream planning/design/verification docs.
- Harvester may only edit the harvest doc, the work-unit index, and `.codex/stack-profile.yaml` when required.
- Classify all requested work as UC, UI, TECH, TEST, or DOC.
- Only UC items may become candidate or confirmed use cases.
- Keep UI/TECH/TEST/DOC items under Non-Use-Case Changes.
- Do not start oracle or downstream work.
- User approval is still required before orchestration begins.
