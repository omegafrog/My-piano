# Workflow Contract

## Canonical Agent IDs

- use_case_harvester
- oracle
- doc_writer
- doc_verify
- executor
- test_gate
- execute_writer
- closer

## Canonical Paths

- use-case-harvest doc:
  `docs/use-case-harvests/<domain>/<task>/use-case-harvest.md`
- work-unit hub doc:
  `docs/work-units/<domain>/<task>/index.md`
- product spec docs:
  `docs/product-specs/<domain>/<task>/`
- design docs:
  `docs/design-docs/<domain>/<task>/`
- verification reports:
  `docs/verification-reports/<domain>/<task>/`
- active execution docs:
  `docs/exec-plans/active/<domain>/<task>/`
- completed execution docs:
  `docs/exec-plans/completed/<domain>/<task>/`

## Canonical Document Set

- `docs/product-specs/<domain>/<task>/domain-boundary.md`
- `docs/product-specs/<domain>/<task>/use-cases.md`
- `docs/design-docs/<domain>/<task>/event-storming.md`
- `docs/design-docs/<domain>/<task>/aggregate-design.md`
- `docs/design-docs/<domain>/<task>/bounded-context.md`
- `docs/design-docs/<domain>/<task>/detailed-design.md`
- `docs/exec-plans/active/<domain>/<task>/plan.md`
- `docs/exec-plans/active/<domain>/<task>/implementation-log.md`
- `docs/verification-reports/<domain>/<task>/doc-verify-before-execute.md`
- `docs/verification-reports/<domain>/<task>/test-gate.md`
- `docs/verification-reports/<domain>/<task>/doc-verify-after-execute.md`
- `docs/verification-reports/<domain>/<task>/closure.md`

## Invocation Unit Rule

- Each harvester run creates a new `<task>` identity.
- Preferred format: `<prompt-summary-slug>-YYYYMMDD-HHMM`
- Slug rules:
  - lowercase kebab-case
  - allowed chars: `a-z`, `0-9`, `-`
  - no random suffix
  - recommended length: 3-8 words

## Path Reference Invariants

- Document identity is represented by `doc_path` only.
- `doc_path` must be project-root-relative and include the filename.
- Do not use document id fields.
- Every work unit keeps one hub doc at
  `docs/work-units/<domain>/<task>/index.md`.
- Stage docs must backlink to the hub doc.
- The hub doc must forward-link to every stage doc.
- If a canonical document is not substantively applicable, keep the file and
  write an explicit placeholder such as `N/A for this work unit`.

## Canonical Status Enums

- coverage_gate: YES | PARTIAL | NO | N/A
- non_uc_scope_status: READY | PARTIAL | N/A
- verification_verdict: PASS | FAIL | BLOCKED
- test_gate_verdict: PASS | FAIL | BLOCKED
- pr_readiness: PR_READY | NOT_READY | BLOCKED
- closure_verdict: COMPLETED | STOPPED

## Required Source of Truth

1. `.codex/config.toml`
2. `.codex/openai.yaml`
3. `.codex/agents/*`
4. `docs/*`
5. the current codebase
