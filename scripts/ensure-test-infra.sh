#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="$ROOT_DIR/docker-compose.yml"
SERVICES=(
  mysql-mypiano
  redis-mypiano-user
  redis-mypiano-cache
  elasticsearch
)

if docker compose version >/dev/null 2>&1; then
  COMPOSE_CMD=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE_CMD=(docker-compose)
else
  echo "Docker Compose is required but was not found." >&2
  exit 1
fi

wait_for() {
  local label="$1"
  local max_attempts="$2"
  shift 2

  for attempt in $(seq 1 "$max_attempts"); do
    if "$@" >/dev/null 2>&1; then
      echo "[ensure-test-infra] $label ready"
      return 0
    fi
    sleep 2
  done

  echo "[ensure-test-infra] timed out waiting for $label" >&2
  return 1
}

wait_for_running() {
  local container_name="$1"
  wait_for "$container_name running" 30 \
    bash -lc "docker inspect --format '{{.State.Status}}' '$container_name' | grep -q '^running$'"
}

ensure_service() {
  local service_name="$1"
  local container_name="$2"

  if docker container inspect "$container_name" >/dev/null 2>&1; then
    echo "[ensure-test-infra] reusing existing container: $container_name"
    docker start "$container_name" >/dev/null 2>&1 || true
  else
    echo "[ensure-test-infra] creating service via compose: $service_name"
    "${COMPOSE_CMD[@]}" -f "$COMPOSE_FILE" up -d "$service_name"
  fi
}

echo "[ensure-test-infra] ensuring docker services: ${SERVICES[*]}"
ensure_service mysql-mypiano mysql-mypiano
ensure_service redis-mypiano-user redis-mypiano-user
ensure_service redis-mypiano-cache redis-mypiano-cache
ensure_service elasticsearch elasticsearch

wait_for "mysql health" 60 \
  bash -lc "docker inspect --format '{{.State.Health.Status}}' mysql-mypiano | grep -q '^healthy$'"

docker exec mysql-mypiano mysql -uroot -ppassword -e \
  "CREATE DATABASE IF NOT EXISTS mypiano CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; \
   CREATE DATABASE IF NOT EXISTS mypianotest CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" \
  >/dev/null
echo "[ensure-test-infra] mysql databases ensured: mypiano, mypianotest"

wait_for "redis user ping" 30 \
  bash -lc "docker exec redis-mypiano-user redis-cli ping | grep -q '^PONG$'"

wait_for "redis cache ping" 30 \
  bash -lc "docker exec redis-mypiano-cache redis-cli ping | grep -q '^PONG$'"

if command -v curl >/dev/null 2>&1; then
  wait_for "elasticsearch http" 60 \
    curl --silent --fail http://127.0.0.1:9200
else
  wait_for_running elasticsearch
fi

echo "[ensure-test-infra] docker test infrastructure is ready"
