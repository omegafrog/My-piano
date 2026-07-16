#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="$ROOT_DIR/docker-compose.yml"
BASE_URL="${LOCAL_BACKEND_URL:-http://localhost:8080}"
COMPOSE_UP_BUILD_ARGS=()

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

prepare_elasticsearch_image() {
  if [[ -d "$ROOT_DIR/docker/elasticsearch" ]]; then
    return 0
  fi

  local source_image="docker.elastic.co/elasticsearch/elasticsearch:8.15.0"
  local compose_image
  compose_image="$(compose config --images | grep -E -- '(^|/)[^/]*-elasticsearch$|^[^/]*-elasticsearch$' | head -n 1)"

  if [[ -z "$compose_image" ]]; then
    echo "[local-deploy] unable to resolve the Compose Elasticsearch image name" >&2
    return 1
  fi

  echo "[local-deploy] Elasticsearch build context is absent; building $source_image with analysis-nori"
  docker build --tag "$compose_image" - <<EOF
FROM $source_image
RUN bin/elasticsearch-plugin install --batch analysis-nori
EOF
  COMPOSE_UP_BUILD_ARGS+=(--no-build)
}

prepare_local_gradle_cache() {
  compose run --rm --no-deps --user root --entrypoint bash app-local -lc \
    'mkdir -p /home/gradle/.gradle-app-local && chown -R gradle:gradle /home/gradle/.gradle-app-local'
}

configure_elasticsearch() {
  curl -fsS -X PUT "http://localhost:9200/_index_template/mypiano-local-nori" \
    -H 'Content-Type: application/json' --data-binary @- >/dev/null <<'JSON'
{
  "index_patterns": ["sheetpost"],
  "template": {
    "settings": {
      "analysis": {
        "analyzer": {
          "my_nori_analyzer": {
            "type": "custom",
            "tokenizer": "nori_tokenizer"
          }
        }
      }
    }
  }
}
JSON
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
prepare_elasticsearch_image
compose up -d "${COMPOSE_UP_BUILD_ARGS[@]}" mysql-mypiano elasticsearch redis-mypiano-user redis-mypiano-cache
wait_for "Elasticsearch" 60 curl -fsS http://localhost:9200
configure_elasticsearch
prepare_local_gradle_cache
compose up -d "${COMPOSE_UP_BUILD_ARGS[@]}" app-local

wait_for "backend health" 120 curl -fsS "$BASE_URL/healthcheck"
wait_for "community seed" 60 community_seed_ready
wait_for "sheet seed" 60 sheet_seed_ready

echo "[local-deploy] backend deployment ready: $BASE_URL"
echo "[local-deploy] run ./scripts/verify-local-deploy.sh for the complete smoke gate"
