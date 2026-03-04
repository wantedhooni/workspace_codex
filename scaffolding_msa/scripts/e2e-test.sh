#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
STACK_PID=""
STACK_LOG="$ROOT_DIR/logs/e2e-stack.log"

cleanup() {
  if [[ -n "$STACK_PID" ]] && kill -0 "$STACK_PID" >/dev/null 2>&1; then
    kill "$STACK_PID" >/dev/null 2>&1 || true
    wait "$STACK_PID" >/dev/null 2>&1 || true
  fi

  "$ROOT_DIR/scripts/all-stop.sh" >/dev/null 2>&1 || true
}

wait_for_http_200() {
  local name="$1"
  local url="$2"

  for _ in {1..90}; do
    if curl -fsSL "$url" >/dev/null 2>&1; then
      echo "[e2e] ${name} is reachable"
      return 0
    fi
    sleep 2
  done

  echo "[e2e] ${name} did not become reachable: ${url}" >&2
  return 1
}

trap cleanup EXIT

cd "$ROOT_DIR"

echo "[e2e] ensuring Playwright dependencies"
if [[ ! -x "$ROOT_DIR/node_modules/.bin/playwright" ]]; then
  npm install
fi

npx playwright install chromium

echo "[e2e] starting full stack"
"$ROOT_DIR/scripts/all-stop.sh" >/dev/null 2>&1 || true
mkdir -p "$ROOT_DIR/logs"
ALL_START_HOLD=true "$ROOT_DIR/scripts/all-start.sh" >"$STACK_LOG" 2>&1 &
STACK_PID=$!

wait_for_http_200 "gateway users" "http://127.0.0.1:8000/api/v1/users"
wait_for_http_200 "gateway orders" "http://127.0.0.1:8000/api/v1/orders?userId=1"
wait_for_http_200 "admin portal" "http://127.0.0.1:3001/dashboard"
wait_for_http_200 "web application" "http://127.0.0.1:3002/"

echo "[e2e] running browser tests"
npm run test:e2e

echo "[e2e] completed"
