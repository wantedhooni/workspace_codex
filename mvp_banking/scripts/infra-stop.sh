#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

cd "$ROOT_DIR"
docker compose -f infra/docker-compose.yml stop

echo "Infra stopped."
print_info_block \
  "Infra stopped" \
  "PostgreSQL stopped: localhost:5432" \
  "Redis stopped: localhost:6379"
