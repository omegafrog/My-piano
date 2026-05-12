#!/usr/bin/env bash
set -euo pipefail

echo "[hook] doc-verify-after-execute"

codex run-agent doc_verify \
  --phase after-execute \
  --contract .codex/contracts/workflow-contract.md \
  --stack-profile .codex/stack-profile.yaml
