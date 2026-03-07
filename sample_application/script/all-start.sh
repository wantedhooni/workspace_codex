#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="$ROOT_DIR/.logs"
BACKEND_PID_FILE="$ROOT_DIR/.backend.pid"
FRONTEND_PID_FILE="$ROOT_DIR/.frontend.pid"

mkdir -p "$LOG_DIR"

is_running() {
  local pid="$1"
  kill -0 "$pid" >/dev/null 2>&1
}

read_pid() {
  local file="$1"
  if [[ -f "$file" ]]; then
    tr -d '[:space:]' < "$file"
  fi
}

start_backend() {
  local pid
  pid="$(read_pid "$BACKEND_PID_FILE")"

  if [[ -n "${pid:-}" ]] && is_running "$pid"; then
    echo "[backend] already running (pid=$pid)"
    return
  fi

  echo "[backend] starting..."
  (
    cd "$ROOT_DIR/backend"
    nohup gradle bootRun > "$LOG_DIR/backend.log" 2>&1 &
    echo $! > "$BACKEND_PID_FILE"
  )
  echo "[backend] started (pid=$(read_pid "$BACKEND_PID_FILE"))"
}

start_frontend() {
  local pid
  pid="$(read_pid "$FRONTEND_PID_FILE")"

  if [[ -n "${pid:-}" ]] && is_running "$pid"; then
    echo "[frontend] already running (pid=$pid)"
    return
  fi

  echo "[frontend] starting..."
  (
    cd "$ROOT_DIR/frontend"
    nohup npm run dev > "$LOG_DIR/frontend.log" 2>&1 &
    echo $! > "$FRONTEND_PID_FILE"
  )
  echo "[frontend] started (pid=$(read_pid "$FRONTEND_PID_FILE"))"
}

start_backend
start_frontend

echo "[done] all services started"
echo "- backend log: $LOG_DIR/backend.log"
echo "- frontend log: $LOG_DIR/frontend.log"
echo ""
echo "[urls]"
echo "- frontend: http://localhost:3000"
echo "- backend : http://localhost:8080"
echo "- db      : postgresql://securities:securities@localhost:5432/securities"
echo "            jdbc:postgresql://localhost:5432/securities"
