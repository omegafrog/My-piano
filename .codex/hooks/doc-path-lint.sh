#!/usr/bin/env bash
set -euo pipefail

echo "[hook] doc-path-lint"

FAILED=0

while IFS= read -r -d '' file; do
  rel="${file#./}"

  if ! grep -q '^doc_path:' "$file"; then
    echo "FAIL: missing doc_path -> $rel"
    FAILED=1
    continue
  fi

  actual="$rel"
  declared="$(grep '^doc_path:' "$file" | head -n1 | sed 's/^doc_path:[[:space:]]*//')"

  if [[ "$declared" != "$actual" ]]; then
    echo "FAIL: doc_path mismatch -> $rel"
    echo "      declared: $declared"
    echo "      actual  : $actual"
    FAILED=1
  fi
done < <(find ./docs -type f -name '*.md' -print0)

exit "$FAILED"
