#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

echo "[infra] starting mariadb/adminer"
docker compose up -d

echo "[infra] waiting for mariadb health..."
for i in {1..60}; do
  STATUS=$(docker inspect -f '{{.State.Health.Status}}' quant-mariadb 2>/dev/null || true)
  if [[ "$STATUS" == "healthy" ]]; then
    echo "[infra] mariadb is healthy"
    exit 0
  fi
  sleep 2
done

echo "[infra] mariadb health check timeout"
exit 1
