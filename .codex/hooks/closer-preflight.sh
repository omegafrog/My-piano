#!/usr/bin/env bash
set -euo pipefail

echo "[hook] closer-preflight"

LATEST_PLAN="$(find docs/exec-plans/active -path '*/plan.md' | sort | tail -n 1 || true)"
LATEST_IMPL_LOG="$(find docs/exec-plans/active -path '*/implementation-log.md' | sort | tail -n 1 || true)"
LATEST_DOC_VERIFY="$(find docs/verification-reports -path '*/doc-verify-after-execute.md' | sort | tail -n 1 || true)"
LATEST_TEST_GATE="$(find docs/verification-reports -path '*/test-gate.md' | sort | tail -n 1 || true)"

if [[ -z "${LATEST_PLAN:-}" ]]; then
  echo "BLOCKED: plan.md not found"
  exit 2
fi

if [[ -z "${LATEST_IMPL_LOG:-}" ]]; then
  echo "BLOCKED: implementation-log.md not found"
  exit 2
fi

if [[ -z "${LATEST_DOC_VERIFY:-}" ]]; then
  echo "BLOCKED: doc-verify-after-execute.md not found"
  exit 2
fi

if [[ -z "${LATEST_TEST_GATE:-}" ]]; then
  echo "BLOCKED: test-gate.md not found"
  exit 2
fi

if ! grep -q "verification_verdict: PASS" "$LATEST_DOC_VERIFY"; then
  echo "BLOCKED: doc_verify is not PASS"
  exit 2
fi

if ! grep -q "test_gate_verdict: PASS" "$LATEST_TEST_GATE"; then
  echo "BLOCKED: test_gate is not PASS"
  exit 2
fi

if grep -qE '^- \[ \]' "$LATEST_PLAN"; then
  echo "BLOCKED: plan progress has unchecked items"
  exit 2
fi

echo "OK: closer preflight passed"
