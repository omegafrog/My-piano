#!/usr/bin/env bash
set -euo pipefail

echo "[hook] doc-verify-before-execute"

codex run-agent doc_verify \
  --phase before-execute \
  --contract .codex/contracts/workflow-contract.md \
  --stack-profile .codex/stack-profile.yaml
