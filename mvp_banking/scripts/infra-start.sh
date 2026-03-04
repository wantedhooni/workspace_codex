#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

cd "$ROOT_DIR"
docker compose -f infra/docker-compose.yml up -d
wait_for_http "http://localhost:5432" 1 "postgres tcp probe" >/dev/null 2>&1 || true
sleep 2

echo "Infra started."
print_info_block \
  "Infra access" \
  "PostgreSQL: localhost:5432" \
  "PostgreSQL DB: mvp_banking" \
  "PostgreSQL user/password: mvp_admin / mvp_admin" \
  "Redis: localhost:6379"
