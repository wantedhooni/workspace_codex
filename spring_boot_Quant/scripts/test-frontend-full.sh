#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
OUT_DIR="$ROOT_DIR/doc/test/artifacts"
BACKEND_PROFILE="${BACKEND_PROFILE:-local}"
FRONTEND_PROFILE="${FRONTEND_PROFILE:-local}"
FRONTEND_PORT="${FRONTEND_PORT:-5176}"
FRONTEND_URL="http://127.0.0.1:${FRONTEND_PORT}"

cleanup() {
  if [[ -f /tmp/frontend-full-backend.pid ]]; then
    kill "$(cat /tmp/frontend-full-backend.pid)" 2>/dev/null || true
    rm -f /tmp/frontend-full-backend.pid
  fi
  if [[ -f /tmp/frontend-full-frontend.pid ]]; then
    kill "$(cat /tmp/frontend-full-frontend.pid)" 2>/dev/null || true
    rm -f /tmp/frontend-full-frontend.pid
  fi
}
trap cleanup EXIT

cd "$ROOT_DIR"
./scripts/infra-up.sh
./scripts/db-import-demo.sh

cd "$ROOT_DIR/backend-mvp"
SPRING_PROFILES_ACTIVE="$BACKEND_PROFILE" gradle bootRun > /tmp/frontend-full-backend.log 2>&1 &
echo $! > /tmp/frontend-full-backend.pid
for i in {1..90}; do
  if curl -fsS http://127.0.0.1:8088/actuator/health >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

cd "$ROOT_DIR/frontend-admin"
npm ci > /tmp/frontend-full-npm-ci.log
VITE_APP_PROFILE="$FRONTEND_PROFILE" npm run dev -- --host 127.0.0.1 --port "$FRONTEND_PORT" > /tmp/frontend-full-frontend.log 2>&1 &
echo $! > /tmp/frontend-full-frontend.pid
for i in {1..90}; do
  if curl -fsS "$FRONTEND_URL" >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

cd "$ROOT_DIR"
cd "$ROOT_DIR/frontend-admin"
FRONTEND_URL="$FRONTEND_URL" node ./scripts/frontend_full_e2e.mjs

echo "[frontend-full] PASS"
