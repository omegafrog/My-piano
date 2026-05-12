# Properties
doc_path: docs/verification-reports/observability/api-observability-logs-metrics-20260416-1742/doc-verify-before-execute.md
owner: Codex
status: completed
title: 컨트롤러 기준 전체 API 디버그 로그 및 메트릭 수집
domain: observability
task: api-observability-logs-metrics-20260416-1742
last_updated: 2026-04-16:18:03 KST

# Verification Summary
- verification_verdict: PASS
- scope: pre-execution documentation verification
- source_of_truth:
  - `AGENTS.md`
  - `docs/ARCHITECTURE.md`
  - `.codex/contracts/workflow-contract.md`
  - `.codex/stack-profile.yaml`
  - `.codex/config.toml`
  - `.codex/openai.yaml`
  - `.codex/agents/doc_verify.toml`
  - `docs/use-case-harvests/observability/api-observability-logs-metrics-20260416-1742/use-case-harvest.md`
  - `docs/work-units/observability/api-observability-logs-metrics-20260416-1742/index.md`
  - canonical product, design, active execution, and verification docs for this work unit
  - current repository state

# Findings
- PASS: harvest status and scope are consistent with the canonical docs. The harvest is `ready-for-oracle` with `coverage_gate: N/A` and `non_uc_scope_status: READY`; the hub and active plan advance that accepted harvest into `ready-for-executor` without changing the task identity or scope.
- PASS: product spec, design docs, and active plan preserve Prometheus as the selected metrics backend. VictoriaMetrics is documented only as a reviewed lightweight alternative and is explicitly not selected for this work unit.
- PASS: no application-owned log or metric query API is planned. The canonical docs consistently assign lookup to Loki and Prometheus HTTP APIs and keep application-owned proxy/query endpoints out of scope.
- PASS: Loki and Prometheus REST API lookup is documented. Loki lookup uses `/loki/api/v1/query_range`, `/loki/api/v1/labels`, and `/loki/api/v1/label/{name}/values`; Prometheus lookup uses `/api/v1/query_range`, `/api/v1/query`, `/api/v1/labels`, and `/api/v1/label/{name}/values`.
- PASS: request/response body logging protections are documented across product, design, and plan docs. The docs require summary-only body capture with masking, maximum size limits, content-type allowlists, multipart/file exclusion, omission reasons, and high-cardinality label restrictions.
- PASS: canonical document files from `.codex/contracts/workflow-contract.md` exist for this active pre-execution work unit. Post-execution reports and closure files exist as explicit placeholders. The completed execution directory is not populated because the work unit is still active and the contract does not define a required completed-stage document in the canonical document set.
- PASS: `doc_path` values match real project-root-relative file paths for the canonical docs that define `doc_path`.
- PASS: hub and stage links are consistent. The work-unit hub forward-links to every canonical stage doc, and every stage doc backlinks to the hub.
- PASS: non-UC work is traceable through `detailed-design.md` and `plan.md`; `implementation-log.md` is an explicit pre-execution placeholder stating that executor work has not started.
- PASS: current repository state supports the planned work without contradicting the docs. `spring-boot-starter-actuator` exists, `micrometer-registry-prometheus` is not yet present, `/actuator/prometheus` is not yet exposed, existing performance logging covers only selected methods, and `ExceptionAdvisor` has inconsistent stack trace/correlation logging. These repository signals match the plan's pending execution steps.

# Canonical Path Check
- use-case-harvest doc: `docs/use-case-harvests/observability/api-observability-logs-metrics-20260416-1742/use-case-harvest.md` exists.
- work-unit hub doc: `docs/work-units/observability/api-observability-logs-metrics-20260416-1742/index.md` exists.
- product spec docs: `docs/product-specs/observability/api-observability-logs-metrics-20260416-1742/` exists with `domain-boundary.md` and `use-cases.md`.
- design docs: `docs/design-docs/observability/api-observability-logs-metrics-20260416-1742/` exists with `event-storming.md`, `aggregate-design.md`, `bounded-context.md`, and `detailed-design.md`.
- verification reports: `docs/verification-reports/observability/api-observability-logs-metrics-20260416-1742/` exists with `doc-verify-before-execute.md`, `test-gate.md`, `doc-verify-after-execute.md`, and `closure.md`.
- active execution docs: `docs/exec-plans/active/observability/api-observability-logs-metrics-20260416-1742/` exists with `plan.md` and `implementation-log.md`.
- completed execution docs: `docs/exec-plans/completed/observability/api-observability-logs-metrics-20260416-1742/` is not populated pre-execution; no completed-stage canonical file is required before closure.

# Traceability Matrix
- Harvest scope "전체 REST 컨트롤러 API 요청/응답 로그 수집 설계" maps to `domain-boundary.md` in-scope logging standards, `detailed-design.md` request/response logging entrypoint, and `plan.md` TECH logging steps.
- Harvest scope "Micrometer/Actuator 기반 API 메트릭 수집 설계" maps to `domain-boundary.md` metrics boundary, `aggregate-design.md` API metric time-series model, `detailed-design.md` metrics design, and `plan.md` Prometheus registry and endpoint steps.
- Harvest scope "Loki/Prometheus REST API 기반 태그/생성일 조회 방식 설계" maps to all three confirmed use cases, external record models, query examples, and plan validation criteria.
- Harvest constraints for masking, body limits, multipart exclusion, and label cardinality map to product constraints, event policies, aggregate invariants, detailed design controls, tests, and acceptance criteria.
- Repository signals map to pending plan work rather than already-implemented claims.

# Open Execution Gaps
- Executor has not implemented logging, Prometheus registry/exposure, structured log output, tests, or query verification yet.
- Test gate evidence is not expected at this stage and remains blocked until executor work exists.
- Loki/Prometheus operational authentication and network access remain documented operational concerns outside the application API.

# Backlinks
- docs/work-units/observability/api-observability-logs-metrics-20260416-1742/index.md
