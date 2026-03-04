#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

APP_DIR="$ROOT_DIR/frontend/admin-portal"
PID_FILE="$PID_DIR/admin-portal.pid"
LOG_FILE="$LOG_DIR/admin-portal.log"

if ensure_pid_stopped "$PID_FILE" "Admin portal"; then
  exit 0
fi

stop_listener_on_port "Admin portal" 5173 >/dev/null 2>&1 || true
require_free_port 5173 "Admin portal"
cd "$APP_DIR"

if [[ ! -d node_modules ]]; then
  npm install
fi

start_background_process \
  "Admin portal" \
  "$PID_FILE" \
  "$LOG_FILE" \
  "http://localhost:5173" \
  "$APP_DIR/node_modules/.bin/vite" --host 0.0.0.0 --port 5173

print_info_block \
  "Admin portal access" \
  "URL: http://localhost:5173" \
  "Login: admin@mvpbanking.local / Admin1234!" \
  "API proxy: http://localhost:8080" \
  "Log: $LOG_FILE"
