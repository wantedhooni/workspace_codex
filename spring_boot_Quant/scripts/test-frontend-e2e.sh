#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
API_BASE="http://127.0.0.1:8088"
ARTIFACT_DIR="$ROOT_DIR/doc/test/artifacts"
BACKEND_PROFILE="${BACKEND_PROFILE:-local}"
FRONTEND_PROFILE="${FRONTEND_PROFILE:-local}"
FRONTEND_PORT="${FRONTEND_PORT:-5176}"
FE_BASE="http://127.0.0.1:${FRONTEND_PORT}"
mkdir -p "$ARTIFACT_DIR"

kill_port_listener() {
  local port="$1"
  local pids
  pids="$(lsof -tiTCP:${port} -sTCP:LISTEN 2>/dev/null || true)"
  if [[ -n "$pids" ]]; then
    kill $pids 2>/dev/null || true
    sleep 1
    pids="$(lsof -tiTCP:${port} -sTCP:LISTEN 2>/dev/null || true)"
    if [[ -n "$pids" ]]; then
      kill -9 $pids 2>/dev/null || true
    fi
  fi
}

cleanup() {
  if [[ -f /tmp/frontend-e2e-backend.pid ]]; then
    kill "$(cat /tmp/frontend-e2e-backend.pid)" 2>/dev/null || true
    rm -f /tmp/frontend-e2e-backend.pid
  fi
  if [[ -f /tmp/frontend-e2e-frontend.pid ]]; then
    kill "$(cat /tmp/frontend-e2e-frontend.pid)" 2>/dev/null || true
    rm -f /tmp/frontend-e2e-frontend.pid
  fi
  kill_port_listener 8088
  kill_port_listener "$FRONTEND_PORT"
}
trap cleanup EXIT

cd "$ROOT_DIR"
./scripts/infra-up.sh
./scripts/db-import-demo.sh

cd "$ROOT_DIR/backend-mvp"
kill_port_listener 8088
SPRING_PROFILES_ACTIVE="$BACKEND_PROFILE" gradle bootRun > /tmp/frontend-e2e-backend.log 2>&1 &
echo $! > /tmp/frontend-e2e-backend.pid
for i in {1..90}; do
  if curl -fsS "$API_BASE/actuator/health" >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

cd "$ROOT_DIR/frontend-admin"
npm ci > "$ARTIFACT_DIR/frontend-npm-ci.log"
kill_port_listener "$FRONTEND_PORT"
VITE_APP_PROFILE="$FRONTEND_PROFILE" npm run dev -- --host 127.0.0.1 --port "$FRONTEND_PORT" > /tmp/frontend-e2e-frontend.log 2>&1 &
echo $! > /tmp/frontend-e2e-frontend.pid
for i in {1..90}; do
  if curl -fsS "$FE_BASE" >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

FRONTEND_URL="$FE_BASE" API_BASE="$API_BASE" OUT_DIR="$ARTIFACT_DIR" node ./scripts/frontend_full_e2e.mjs

cp -f "$ARTIFACT_DIR/frontend-orders-full.png" "$ARTIFACT_DIR/frontend-orders.png"
cp -f "$ARTIFACT_DIR/frontend-order-audits-full.png" "$ARTIFACT_DIR/frontend-order-audits.png"
cp -f "$ARTIFACT_DIR/frontend-trades-full.png" "$ARTIFACT_DIR/frontend-trades.png"
cp -f "$ARTIFACT_DIR/frontend-positions-full.png" "$ARTIFACT_DIR/frontend-positions.png"
cp -f "$ARTIFACT_DIR/frontend-portfolio-summaries-full.png" "$ARTIFACT_DIR/frontend-portfolio-summaries.png"
cp -f "$ARTIFACT_DIR/frontend-order-health-full.png" "$ARTIFACT_DIR/frontend-order-health.png"
cp -f "$ARTIFACT_DIR/frontend-risk-alerts-full.png" "$ARTIFACT_DIR/frontend-risk-alerts.png"
cp -f "$ARTIFACT_DIR/frontend-execution-qualities-full.png" "$ARTIFACT_DIR/frontend-execution-qualities.png"
cp -f "$ARTIFACT_DIR/frontend-risk-limits-full.png" "$ARTIFACT_DIR/frontend-risk-limits.png"
cp -f "$ARTIFACT_DIR/frontend-users-full.png" "$ARTIFACT_DIR/frontend-users.png"
cp -f "$ARTIFACT_DIR/frontend-roles-full.png" "$ARTIFACT_DIR/frontend-roles.png"
cp -f "$ARTIFACT_DIR/frontend-menus-full.png" "$ARTIFACT_DIR/frontend-menus.png"
cp -f "$ARTIFACT_DIR/frontend-menu-permissions-full.png" "$ARTIFACT_DIR/frontend-menu-permissions.png"
cp -f "$ARTIFACT_DIR/frontend-journal-vouchers-full.png" "$ARTIFACT_DIR/frontend-journal-vouchers.png"
cp -f "$ARTIFACT_DIR/frontend-ledger-entries-full.png" "$ARTIFACT_DIR/frontend-ledger-entries.png"
cp -f "$ARTIFACT_DIR/frontend-account-profile-full.png" "$ARTIFACT_DIR/frontend-account-profile.png"
cp -f "$ARTIFACT_DIR/frontend-account-sessions-full.png" "$ARTIFACT_DIR/frontend-account-sessions.png"

for f in \
  frontend-orders.png \
  frontend-order-audits.png \
  frontend-trades.png \
  frontend-positions.png \
  frontend-portfolio-summaries.png \
  frontend-order-health.png \
  frontend-risk-alerts.png \
  frontend-execution-qualities.png \
  frontend-risk-limits.png \
  frontend-users.png \
  frontend-roles.png \
  frontend-menus.png \
  frontend-menu-permissions.png \
  frontend-journal-vouchers.png \
  frontend-ledger-entries.png \
  frontend-account-profile.png \
  frontend-account-sessions.png; do
  [[ -s "$ARTIFACT_DIR/$f" ]]
done

echo "[frontend-e2e] PASS"
