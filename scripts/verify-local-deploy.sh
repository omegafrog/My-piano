#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="$ROOT_DIR/docker-compose.yml"
BASE_URL="${LOCAL_BACKEND_URL:-http://localhost:8080}"
WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT

if docker compose version >/dev/null 2>&1; then
  COMPOSE_CMD=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE_CMD=(docker-compose)
else
  echo "Docker Compose is required but was not found." >&2
  exit 1
fi

compose() {
  "${COMPOSE_CMD[@]}" -f "$COMPOSE_FILE" --profile local "$@"
}

wait_for_health() {
  for attempt in $(seq 1 120); do
    if curl -fsS "$BASE_URL/healthcheck" >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done
  echo "[verify-local-deploy] backend health timeout" >&2
  return 1
}

request_json() {
  local url="$1"
  local output="$2"
  curl -fsS --retry 3 --retry-delay 1 "$url" -o "$output"
}

fetch_snapshot() {
  local prefix="$1"
  request_json "$BASE_URL/api/v1/community/posts?size=100" "$WORK_DIR/$prefix-community.json"
  request_json "$BASE_URL/api/v1/sheet-post?searchBackend=db&size=100" "$WORK_DIR/$prefix-sheets.json"

  mapfile -t community_ids < <(python3 - "$WORK_DIR/$prefix-community.json" <<'PY'
import json, sys
items = json.load(open(sys.argv[1], encoding="utf-8"))["data"]["postListDtos"]
if not items:
    raise SystemExit("required community list is empty")
for item in items:
    print(item["id"])
PY
  )
  mapfile -t sheet_ids < <(python3 - "$WORK_DIR/$prefix-sheets.json" <<'PY'
import json, sys
items = json.load(open(sys.argv[1], encoding="utf-8"))["data"]["content"]
if not items:
    raise SystemExit("required sheet list is empty")
for item in items:
    print(item["id"])
PY
  )

  for id in "${community_ids[@]}"; do
    request_json "$BASE_URL/api/v1/community/posts/$id" "$WORK_DIR/$prefix-community-detail-$id.json"
  done
  for id in "${sheet_ids[@]}"; do
    request_json "$BASE_URL/api/v1/sheet-post/$id" "$WORK_DIR/$prefix-sheet-detail-$id.json"
  done

  python3 - "$WORK_DIR/$prefix-community-detail.json" "$WORK_DIR"/$prefix-community-detail-*.json <<'PY'
import json, sys
with open(sys.argv[1], "w", encoding="utf-8") as output:
    json.dump([json.load(open(path, encoding="utf-8")) for path in sys.argv[2:]], output, ensure_ascii=False)
PY
  python3 - "$WORK_DIR/$prefix-sheet-detail.json" "$WORK_DIR"/$prefix-sheet-detail-*.json <<'PY'
import json, sys
with open(sys.argv[1], "w", encoding="utf-8") as output:
    json.dump([json.load(open(path, encoding="utf-8")) for path in sys.argv[2:]], output, ensure_ascii=False)
PY

  python3 - "$WORK_DIR/$prefix-community.json" "$WORK_DIR/$prefix-sheets.json" <<'PY'
import json, sys
community = json.load(open(sys.argv[1], encoding="utf-8"))["data"]["postListDtos"]
sheets = json.load(open(sys.argv[2], encoding="utf-8"))["data"]["content"]
print(len(community), len(sheets))
PY
}

verify_content_quality() {
  python3 - "$@" <<'PY'
import json, re, sys
forbidden = re.compile(r"dummy|test|sample|fixture", re.I)
sequence = re.compile(r"(?:[_ -]\d+)(?:\.[^.]+)?$")
visible_keys = {"title", "content", "name", "username", "originalFileName", "artistName", "sheetTitle"}

def walk(value):
    if isinstance(value, dict):
        for key, child in value.items():
            if key in visible_keys and isinstance(child, str):
                if not child.strip() or forbidden.search(child) or sequence.search(child):
                    raise SystemExit(f"content quality violation in {key}: {child}")
            walk(child)
    elif isinstance(value, list):
        for child in value:
            walk(child)

for path in sys.argv[1:]:
    with open(path, encoding="utf-8") as source:
        walk(json.load(source))
PY
}

collect_asset_urls() {
  python3 - "$@" <<'PY'
import json, sys
urls = set()

def walk(value):
    if isinstance(value, dict):
        for child in value.values(): walk(child)
    elif isinstance(value, list):
        for child in value: walk(child)
    elif isinstance(value, str) and value.startswith("http://localhost:8080/"):
        if any(part in value for part in ("/profiles/", "/thumbnails/", "/sheets/")):
            urls.add(value)

for path in sys.argv[1:]:
    with open(path, encoding="utf-8") as source:
        walk(json.load(source))
for url in sorted(urls):
    print(url)
PY
}

verify_asset() {
  local url="$1"
  local key
  key="$(printf '%s' "$url" | sha256sum | cut -d' ' -f1)"
  local headers="$WORK_DIR/$key.headers"
  local body="$WORK_DIR/$key.body"
  curl -fsS -D "$headers" -o "$body" "$url"
  test -s "$body"

  local content_type
  content_type="$(awk 'BEGIN{IGNORECASE=1} /^content-type:/{print $2}' "$headers" | tr -d '\r' | tail -1)"
  case "$url" in
    */profiles/*|*/thumbnails/*) [[ "$content_type" == image/* ]] ;;
    */sheets/*) [[ "$content_type" == application/pdf* ]] ;;
    *) return 1 ;;
  esac
}

wait_for_health
read -r community_before sheets_before < <(fetch_snapshot before)

responses=(
  "$WORK_DIR/before-community.json"
  "$WORK_DIR/before-sheets.json"
  "$WORK_DIR/before-community-detail.json"
  "$WORK_DIR/before-sheet-detail.json"
)
verify_content_quality "${responses[@]}"

asset_count=0
while IFS= read -r asset_url; do
  verify_asset "$asset_url"
  asset_count=$((asset_count + 1))
done < <(collect_asset_urls "${responses[@]}")
if (( asset_count == 0 )); then
  echo "[verify-local-deploy] no local asset URL found" >&2
  exit 1
fi

echo "[verify-local-deploy] restarting backend to verify repeatable seed"
compose restart app-local >/dev/null
wait_for_health
read -r community_after sheets_after < <(fetch_snapshot after)

if [[ "$community_before" != "$community_after" || "$sheets_before" != "$sheets_after" ]]; then
  echo "[verify-local-deploy] seed counts changed after restart" >&2
  exit 1
fi

echo "[verify-local-deploy] backend verification successful"
echo "[verify-local-deploy] community=$community_after sheets=$sheets_after assets=$asset_count"
# Backend success is intentionally independent of any frontend build or runtime result.
