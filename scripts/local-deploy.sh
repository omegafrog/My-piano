#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="$ROOT_DIR/docker-compose.yml"
BASE_URL="${LOCAL_BACKEND_URL:-http://localhost:8080}"

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

wait_for() {
  local label="$1"
  local attempts="$2"
  shift 2

  for attempt in $(seq 1 "$attempts"); do
    if "$@" >/dev/null 2>&1; then
      echo "[local-deploy] $label ready"
      return 0
    fi
    sleep 2
  done

  echo "[local-deploy] timed out waiting for $label" >&2
  compose logs --tail=120 app-local >&2 || true
  return 1
}

community_seed_ready() {
  curl -fsS "$BASE_URL/api/v1/community/posts?size=100" |
    python3 -c 'import json,sys; items=json.load(sys.stdin)["data"]["postListDtos"]; raise SystemExit(0 if items else 1)'
}

sheet_seed_ready() {
  curl -fsS "$BASE_URL/api/v1/sheet-post?searchBackend=db&size=100" |
    python3 -c 'import json,sys; items=json.load(sys.stdin)["data"]["content"]; raise SystemExit(0 if items else 1)'
}

echo "[local-deploy] starting local infrastructure and backend"
mkdir -p "$ROOT_DIR/local-storage"
compose up -d mysql-mypiano elasticsearch redis-mypiano-user redis-mypiano-cache app-local

wait_for "backend health" 120 curl -fsS "$BASE_URL/healthcheck"
wait_for "community seed" 60 community_seed_ready
wait_for "sheet seed" 60 sheet_seed_ready

echo "[local-deploy] backend deployment ready: $BASE_URL"
echo "[local-deploy] run ./scripts/verify-local-deploy.sh for the complete smoke gate"
