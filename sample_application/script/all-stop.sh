#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_PID_FILE="$ROOT_DIR/.backend.pid"
FRONTEND_PID_FILE="$ROOT_DIR/.frontend.pid"

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

stop_service() {
  local name="$1"
  local pid_file="$2"

  local pid
  pid="$(read_pid "$pid_file")"

  if [[ -z "${pid:-}" ]]; then
    echo "[$name] pid file not found, skip"
    return
  fi

  if ! is_running "$pid"; then
    echo "[$name] not running, cleaning stale pid"
    rm -f "$pid_file"
    return
  fi

  echo "[$name] stopping (pid=$pid)..."
  kill "$pid" >/dev/null 2>&1 || true

  for _ in {1..20}; do
    if ! is_running "$pid"; then
      break
    fi
    sleep 0.5
  done

  if is_running "$pid"; then
    echo "[$name] graceful stop timeout, force kill"
    kill -9 "$pid" >/dev/null 2>&1 || true
  fi

  rm -f "$pid_file"
  echo "[$name] stopped"
}

stop_service "frontend" "$FRONTEND_PID_FILE"
stop_service "backend" "$BACKEND_PID_FILE"

echo "[done] all services stopped"
