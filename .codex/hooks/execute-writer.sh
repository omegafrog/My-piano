#!/usr/bin/env bash
set -euo pipefail

echo "[hook] execute-writer-after-test-pass"

codex run-agent execute_writer \
  --contract .codex/contracts/workflow-contract.md \
  --stack-profile .codex/stack-profile.yaml
