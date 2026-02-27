#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
BACKEND_PID_FILE="/tmp/quant-backend-mvp.pid"
FRONTEND_PID_FILE="/tmp/quant-frontend-admin.pid"
BACKEND_PORT="${BACKEND_PORT:-8088}"
FRONTEND_PORT="${FRONTEND_PORT:-5175}"
SKIP_INFRA_DOWN="${SKIP_INFRA_DOWN:-0}"

force_kill_port() {
  local name="$1"
  local port="$2"

  if ! command -v lsof >/dev/null 2>&1; then
    return 0
  fi

  local pids
  pids="$(lsof -ti tcp:"$port" 2>/dev/null | tr '\n' ' ' | xargs || true)"
  if [[ -z "$pids" ]]; then
    return 0
  fi

  echo "[stop-all] stopping $name by port $port (pid: $pids)"
  kill $pids >/dev/null 2>&1 || true
  sleep 1

  pids="$(lsof -ti tcp:"$port" 2>/dev/null | tr '\n' ' ' | xargs || true)"
  if [[ -n "$pids" ]]; then
    echo "[stop-all] force killing $name by port $port (pid: $pids)"
    kill -9 $pids >/dev/null 2>&1 || true
  fi
}

stop_by_pid_file() {
  local name="$1"
  local pid_file="$2"

  if [[ ! -f "$pid_file" ]]; then
    echo "[stop-all] $name pid file not found: $pid_file"
    return 0
  fi

  local pid
  pid="$(cat "$pid_file" 2>/dev/null || true)"

  if [[ -z "$pid" ]]; then
    echo "[stop-all] $name pid file is empty, removing stale file"
    rm -f "$pid_file"
    return 0
  fi

  if ! kill -0 "$pid" >/dev/null 2>&1; then
    echo "[stop-all] $name already stopped (stale pid: $pid)"
    rm -f "$pid_file"
    return 0
  fi

  echo "[stop-all] stopping $name (pid: $pid)"
  kill "$pid" >/dev/null 2>&1 || true

  for _ in {1..15}; do
    if ! kill -0 "$pid" >/dev/null 2>&1; then
      rm -f "$pid_file"
      echo "[stop-all] $name stopped"
      return 0
    fi
    sleep 1
  done

  echo "[stop-all] $name did not stop gracefully, force killing"
  kill -9 "$pid" >/dev/null 2>&1 || true
  rm -f "$pid_file"
}

main() {
  echo "[stop-all] root: $ROOT_DIR"

  stop_by_pid_file "backend" "$BACKEND_PID_FILE"
  stop_by_pid_file "frontend" "$FRONTEND_PID_FILE"

  force_kill_port "backend" "$BACKEND_PORT"
  force_kill_port "frontend" "$FRONTEND_PORT"

  if [[ "$SKIP_INFRA_DOWN" == "1" ]]; then
    echo "[stop-all] SKIP_INFRA_DOWN=1, skipping infra shutdown"
  else
    "$ROOT_DIR/scripts/infra-down.sh"
  fi

  echo "[stop-all] all stopped"
}

main "$@"
