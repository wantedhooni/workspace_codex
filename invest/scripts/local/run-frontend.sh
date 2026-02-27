#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
FRONTEND_DIR="${ROOT_DIR}/frontend-admin"

cd "${FRONTEND_DIR}"

if [ ! -d node_modules ]; then
  npm install
fi

export NEXT_PUBLIC_API_BASE_URL="${NEXT_PUBLIC_API_BASE_URL:-http://localhost:8080/api/v1}"
export NEXT_PUBLIC_DEMO_USERNAME="${NEXT_PUBLIC_DEMO_USERNAME:-demo}"
export NEXT_PUBLIC_DEMO_PASSWORD="${NEXT_PUBLIC_DEMO_PASSWORD:-demo1234}"

echo "[local-frontend] admin: http://127.0.0.1:3000"
echo "[local-frontend] demo login prefill: ${NEXT_PUBLIC_DEMO_USERNAME}/${NEXT_PUBLIC_DEMO_PASSWORD}"

exec npm run dev -- --hostname 127.0.0.1 --port "${FRONTEND_PORT:-3000}"
