#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.run"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"
BACKEND_PID_FILE="$RUN_DIR/backend.pid"
FRONTEND_PID_FILE="$RUN_DIR/frontend.pid"
BACKEND_LOG_FILE="$RUN_DIR/backend.log"
FRONTEND_LOG_FILE="$RUN_DIR/frontend.log"
BACKEND_PORT=8080
FRONTEND_PORT=5173

mkdir -p "$RUN_DIR"

require_command() {
  local command_name="$1"

  if ! command -v "$command_name" >/dev/null 2>&1; then
    echo "Missing required command: $command_name" >&2
    exit 1
  fi
}

is_pid_running() {
  local pid="$1"
  kill -0 "$pid" >/dev/null 2>&1
}

cleanup_stale_pid() {
  local pid_file="$1"

  if [[ -f "$pid_file" ]]; then
    local pid
    pid="$(cat "$pid_file")"

    if [[ -n "$pid" ]] && is_pid_running "$pid"; then
      echo "Process already running with PID $pid from $pid_file" >&2
      exit 1
    fi

    rm -f "$pid_file"
  fi
}

ensure_port_available() {
  local port="$1"
  local name="$2"
  local used_pid

  used_pid="$(lsof -ti tcp:"$port" || true)"
  if [[ -n "$used_pid" ]]; then
    echo "$name port $port is already in use by PID $used_pid" >&2
    exit 1
  fi
}

wait_for_port() {
  local port="$1"
  local label="$2"
  local pid_file="$3"

  for _ in {1..30}; do
    if lsof -ti tcp:"$port" >/dev/null 2>&1; then
      echo "$label is ready on port $port"
      return
    fi

    if [[ -f "$pid_file" ]]; then
      local pid
      pid="$(cat "$pid_file")"
      if [[ -n "$pid" ]] && ! is_pid_running "$pid"; then
        echo "$label failed to start. Check log: $RUN_DIR/${label,,}.log" >&2
        exit 1
      fi
    fi

    sleep 1
  done

  echo "$label did not become ready on port $port in time" >&2
  exit 1
}

start_backend() {
  cleanup_stale_pid "$BACKEND_PID_FILE"
  ensure_port_available "$BACKEND_PORT" "Backend"

  nohup bash -lc "cd \"$BACKEND_DIR\" && exec mvn spring-boot:run" >"$BACKEND_LOG_FILE" 2>&1 &
  echo $! >"$BACKEND_PID_FILE"

  echo "Backend started. PID=$(cat "$BACKEND_PID_FILE")"
  wait_for_port "$BACKEND_PORT" "Backend" "$BACKEND_PID_FILE"
}

start_frontend() {
  cleanup_stale_pid "$FRONTEND_PID_FILE"
  ensure_port_available "$FRONTEND_PORT" "Frontend"

  (
    cd "$FRONTEND_DIR"
    if [[ ! -d node_modules ]]; then
      npm install
    fi
  )

  nohup bash -lc "cd \"$FRONTEND_DIR\" && exec npm run dev -- --host 0.0.0.0" >"$FRONTEND_LOG_FILE" 2>&1 &
  echo $! >"$FRONTEND_PID_FILE"

  echo "Frontend started. PID=$(cat "$FRONTEND_PID_FILE")"
  wait_for_port "$FRONTEND_PORT" "Frontend" "$FRONTEND_PID_FILE"
}

require_command lsof
require_command mvn
require_command npm

start_backend
start_frontend

echo "Logs:"
echo "  backend  -> $BACKEND_LOG_FILE"
echo "  frontend -> $FRONTEND_LOG_FILE"
echo "URLs:"
echo "  backend  -> http://127.0.0.1:$BACKEND_PORT/api/dashboard/overview"
echo "  frontend -> http://127.0.0.1:$FRONTEND_PORT/dashboard"
