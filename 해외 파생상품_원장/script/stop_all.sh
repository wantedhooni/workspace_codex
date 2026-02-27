#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.run"

stop_from_pidfile() {
  local name="$1"
  local pid_file="$RUN_DIR/${name}.pid"

  if [[ ! -f "$pid_file" ]]; then
    echo "[$name] pid file not found, skip"
    return
  fi

  local pid
  pid="$(cat "$pid_file")"
  if [[ -z "$pid" ]]; then
    rm -f "$pid_file"
    echo "[$name] empty pid file removed"
    return
  fi

  if kill -0 "$pid" >/dev/null 2>&1; then
    echo "[$name] stopping pid=$pid"
    kill "$pid" >/dev/null 2>&1 || true

    for _ in {1..10}; do
      if ! kill -0 "$pid" >/dev/null 2>&1; then
        break
      fi
      sleep 1
    done

    if kill -0 "$pid" >/dev/null 2>&1; then
      echo "[$name] force killing pid=$pid"
      kill -9 "$pid" >/dev/null 2>&1 || true
    fi
  else
    echo "[$name] process already stopped"
  fi

  rm -f "$pid_file"
}

stop_from_pidfile "frontend"
stop_from_pidfile "backend"

echo "[db] stopping postgres..."
docker compose --project-name derivops_mvp -f "$ROOT_DIR/docker-compose.yml" stop postgres >/dev/null || true

echo "All services stopped."
