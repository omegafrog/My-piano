# Properties
doc_path: docs/verification-reports/batch/startup-batch-runner-20260415-1727/test-gate.md
owner: Codex
status: fail
domain: batch
task: startup-batch-runner-20260415-1727
last_updated: 2026-04-15:18:30

# Test Gate
- test_gate_verdict: FAIL
- scope: executor output verification for startup batch runner
- policy_reference: .codex/test-gate.yaml
- required_stages:
  - build
  - unit-and-integration

# Evidence
- Focused regression test `BatchStartupJobRunnerTest` passed in a standalone run and verifies the new startup runner behavior in isolation.
- Required build gate `./gradlew build --console=plain` progressed through `:test` and surfaced concrete failing tests before the session was interrupted, so the build gate is currently failing rather than blocked.
- `bootRun`-specific runtime validation remains unverified in this test gate.

# Commands
- `./gradlew test --tests com.omegafrog.My.piano.app.batch.BatchStartupJobRunnerTest`
  - first attempt failed because it was launched in parallel with `./gradlew build` and both tasks wrote to the same `build/test-results` path
  - standalone rerun result: `BUILD SUCCESSFUL in 32s`
- `./gradlew build`
  - first run started and entered `:test`, but overlapped with the focused test command and could not be used as valid evidence
- `./gradlew build --console=plain`
  - isolated rerun progressed through `:test` and reported concrete failing tests before the session was interrupted
  - observed failing tests included `SheetPostCacheWarmupJobIntegrationTest` (JPA/JDBC exception), multiple `SecurityControllerTest` assertion failures, and multiple `CommonUserServiceTest` SQL grammar failures
- Support inspection:
  - `find build/test-results/test -maxdepth 1 -type f | wc -l`
  - `ls -lt build/test-results/test | head -20`
  - `find build/reports/tests/test -maxdepth 2 -type f | head -20`

# Findings
- `BatchStartupJobRunnerTest` regression coverage is present and passed when run in isolation.
- The required build gate from repository rules and `.codex/test-gate.yaml` is not satisfied because `./gradlew build --console=plain` surfaced concrete test failures in the broader suite.
- The observed build failures are broader repository-suite failures, not evidence that the new startup runner regression coverage itself failed. The isolated focused pass does not override the failing repository-wide gate.
- The initial parallel invocation was an invalid verification attempt because concurrent Gradle tasks contended on `build/test-results/test/TEST-com.omegafrog.My.piano.app.batch.BatchStartupJobRunnerTest.xml`.
- Runtime confirmation that `./gradlew bootRun` no longer dies with `Job name must be specified in case of multiple jobs` remains unverified.

# Next Action
- Triage the failing tests from the isolated `./gradlew build --console=plain` run and determine whether they are pre-existing failures or regressions introduced by this change set.
- Re-run `./gradlew build --console=plain` after stabilizing the failing suite and capture the final Gradle exit result.
- Perform a local `./gradlew bootRun` validation to confirm startup no longer fails with the multiple-job auto-launch exception.

# Backlinks
- docs/work-units/batch/startup-batch-runner-20260415-1727/index.md
