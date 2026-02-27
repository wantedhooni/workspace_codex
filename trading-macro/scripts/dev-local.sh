#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
BACKEND_LOG=/tmp/trading-macro-backend.log
FRONTEND_LOG=/tmp/trading-macro-frontend.log

if ! command -v gradle >/dev/null 2>&1; then
  echo "Gradle is required for local run (install Gradle or use docker-compose)."
  exit 1
fi

cd "$ROOT_DIR/backend"
(gradle bootRun > "$BACKEND_LOG" 2>&1) &
BACKEND_PID=$!

cd "$ROOT_DIR/frontend"
if [ ! -d node_modules ]; then
  npm install
fi
(npm run dev -- --host 0.0.0.0 --port 5173 > "$FRONTEND_LOG" 2>&1) &
FRONTEND_PID=$!

trap 'kill $BACKEND_PID $FRONTEND_PID' EXIT

echo "Backend PID: $BACKEND_PID (log: $BACKEND_LOG)"
echo "Frontend PID: $FRONTEND_PID (log: $FRONTEND_LOG)"

tail -f /dev/null
