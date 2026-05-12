#!/usr/bin/env bash
set -euo pipefail

echo "[hook] test-gate-after-executor"

codex run-agent test_gate \
  --config .codex/test-gate.yaml \
  --stack-profile .codex/stack-profile.yaml \
  --repository-settings .codex/repository-settings.md
