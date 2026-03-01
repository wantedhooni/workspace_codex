#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.run"
BACKEND_PID_FILE="$RUN_DIR/backend.pid"
FRONTEND_PID_FILE="$RUN_DIR/frontend.pid"

stop_by_pid_file() {
  local pid_file="$1"
  local label="$2"

  if [[ ! -f "$pid_file" ]]; then
    echo "$label is not running (missing pid file)"
    return
  fi

  local pid
  pid="$(cat "$pid_file")"

  if [[ -z "$pid" ]]; then
    rm -f "$pid_file"
    echo "$label pid file was empty and has been cleaned up"
    return
  fi

  if kill -0 "$pid" >/dev/null 2>&1; then
    kill "$pid"
    echo "Stopping $label (PID $pid)"

    for _ in {1..20}; do
      if ! kill -0 "$pid" >/dev/null 2>&1; then
        rm -f "$pid_file"
        echo "$label stopped"
        return
      fi
      sleep 1
    done

    kill -9 "$pid" >/dev/null 2>&1 || true
    rm -f "$pid_file"
    echo "$label was force stopped"
    return
  fi

  rm -f "$pid_file"
  echo "$label pid file was stale and has been cleaned up"
}

stop_by_pid_file "$FRONTEND_PID_FILE" "Frontend"
stop_by_pid_file "$BACKEND_PID_FILE" "Backend"
