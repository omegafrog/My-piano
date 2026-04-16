---
name: orchestrate-plan
description: >
  run the workflow in this order:
  use_case_harvester -> oracle -> doc_writer -> doc_verify ->
  executor -> test_gate -> execute_writer -> doc_verify -> closer
---

# Orchestrate Plan

This skill runs the end-to-end orchestration workflow.

Source of truth priority:
1. `.codex/contracts/workflow-contract.md`
2. `.codex/stack-profile.yaml`
3. `.codex/test-gate.yaml`
4. `.codex/repository-settings.md`
5. related docs
6. the current codebase

Workflow order:
1. use_case_harvester
2. oracle
3. doc_writer
4. doc_verify
5. executor
6. test_gate
7. execute_writer
8. doc_verify
9. closer

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
