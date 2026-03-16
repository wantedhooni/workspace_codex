#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_HOST_PORT="${BACKEND_HOST_PORT:-18080}"
FRONTEND_HOST_PORT="${FRONTEND_HOST_PORT:-13000}"

cd "$ROOT_DIR"
if [[ -f .env ]]; then
  set -a
  source .env
  set +a
fi

docker compose down
docker compose up -d --build

cat <<EOF

Market Signal Platform restarted.
- Frontend: http://localhost:$FRONTEND_HOST_PORT
- Backend API: http://localhost:$BACKEND_HOST_PORT
- Swagger UI: http://localhost:$BACKEND_HOST_PORT/swagger-ui.html
- Demo Account: demo@marketsignal.dev / Demo1234!
- Seed Snapshot: 2026-03-13 real market snapshot

EOF
