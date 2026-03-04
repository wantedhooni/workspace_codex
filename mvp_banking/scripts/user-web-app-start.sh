#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

APP_DIR="$ROOT_DIR/frontend/user-web-app"
PID_FILE="$PID_DIR/user-web-app.pid"
LOG_FILE="$LOG_DIR/user-web-app.log"

if ensure_pid_stopped "$PID_FILE" "User web app"; then
  exit 0
fi

stop_listener_on_port "User web app" 5174 >/dev/null 2>&1 || true
require_free_port 5174 "User web app"
cd "$APP_DIR"

if [[ ! -d node_modules ]]; then
  npm install
fi

start_background_process \
  "User web app" \
  "$PID_FILE" \
  "$LOG_FILE" \
  "http://localhost:5174" \
  "$APP_DIR/node_modules/.bin/vite" --host 0.0.0.0 --port 5174

print_info_block \
  "User web app access" \
  "URL: http://localhost:5174" \
  "Login: user@mvpbanking.local / User1234!" \
  "API proxy: http://localhost:8080" \
  "Log: $LOG_FILE"
