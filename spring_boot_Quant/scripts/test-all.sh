#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
BACKEND_PROFILE="${BACKEND_PROFILE:-local}"

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
  if [[ -f /tmp/quant-backend-mvp.pid ]]; then
    kill "$(cat /tmp/quant-backend-mvp.pid)" 2>/dev/null || true
    rm -f /tmp/quant-backend-mvp.pid
  fi
  if [[ -f /tmp/market-data-adapter.pid ]]; then
    kill "$(cat /tmp/market-data-adapter.pid)" 2>/dev/null || true
    rm -f /tmp/market-data-adapter.pid
  fi
  kill_port_listener 8088
  kill_port_listener 4010
}
trap cleanup EXIT

cd "$ROOT_DIR"

./scripts/infra-up.sh

echo "[test] backend gradle test"
cd "$ROOT_DIR/backend-mvp"
gradle test

echo "[test] backend bootRun + smoke"
kill_port_listener 8088
SPRING_PROFILES_ACTIVE="$BACKEND_PROFILE" gradle bootRun > /tmp/quant-backend-mvp.log 2>&1 &
echo $! > /tmp/quant-backend-mvp.pid
for i in {1..60}; do
  if curl -fsS http://127.0.0.1:8088/actuator/health >/dev/null 2>&1; then
    break
  fi
  sleep 1
done
cd "$ROOT_DIR"
./scripts/smoke_a7.sh

echo "[test] adapter health/quote"
cd "$ROOT_DIR/adapter/market-data-adapter"
npm install >/dev/null
kill_port_listener 4010
node src/server.js > /tmp/market-data-adapter.log 2>&1 &
echo $! > /tmp/market-data-adapter.pid
for i in {1..30}; do
  if curl -fsS http://127.0.0.1:4010/health >/dev/null 2>&1; then
    break
  fi
  sleep 1
done
curl -fsS "http://127.0.0.1:4010/api/market/quote?symbol=AAPL" >/dev/null

echo "[test] frontend build"
cd "$ROOT_DIR/frontend-admin"
npm ci >/dev/null
VITE_APP_PROFILE=local npm run build >/dev/null

echo "[test] ALL PASS"
