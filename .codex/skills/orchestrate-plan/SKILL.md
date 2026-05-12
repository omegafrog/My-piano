---
name: orchestrate-plan
description: >
  run the workflow in this order:
  use_case_harvester -> oracle -> doc_writer -> executor -> closer,
  with verification and sync steps triggered by hooks
---

# Orchestrate Plan

This skill runs the end-to-end orchestration workflow.

Source of truth priority:
1. `.codex/contracts/workflow-contract.md`
2. `.codex/stack-profile.yaml`
3. `.codex/test-gate.yaml`
4. `.codex/repository-settings.md`
5. `.codex/hooks.yaml`
6. related docs
7. the current codebase

Primary workflow order:
1. use_case_harvester
2. oracle
3. doc_writer
4. executor
5. closer

Hook-driven stages:
- after doc_writer -> doc_verify before execute
- after executor -> test_gate
- after passing test_gate -> execute_writer
- after execute_writer -> doc_verify after execute
- before closer -> closer preflight
- on docs change -> doc_path lint

Do not start oracle or any downstream step until one of these is true:

- Functional change case:
  - harvest doc exists
  - coverage_gate == YES
  - status == ready-for-oracle
  - stack_profile_status == READY
  - the user explicitly approved proceeding

- Non-UC only case:
  - harvest doc exists
  - non_uc_scope_status == READY
  - status == ready-for-oracle
  - stack_profile_status == READY
  - the user explicitly approved proceeding

If a required condition is missing, stop and explain why orchestration cannot proceed.
