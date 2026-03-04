#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PID_DIR="$ROOT_DIR/pids"

SERVICES=(
  "order-service"
  "user-service"
  "auth-server"
  "api-gateway"
  "config-server"
  "discovery-service"
)

stop_service() {
  local service="$1"
  local pid_file="$PID_DIR/${service}.pid"

  if [[ ! -f "$pid_file" ]]; then
    echo "[all-stop] ${service} pid file not found"
    return 0
  fi

  local pid
  pid="$(cat "$pid_file")"

  if kill -0 "$pid" >/dev/null 2>&1; then
    echo "[all-stop] stopping ${service} pid=${pid}"
    kill "$pid"
    for _ in {1..20}; do
      if ! kill -0 "$pid" >/dev/null 2>&1; then
        rm -f "$pid_file"
        echo "[all-stop] ${service} stopped"
        return 0
      fi
      sleep 1
    done

    echo "[all-stop] force killing ${service} pid=${pid}"
    kill -9 "$pid"
  else
    echo "[all-stop] ${service} already stopped"
  fi

  rm -f "$pid_file"
}

for service in "${SERVICES[@]}"; do
  stop_service "$service"
done

echo "[all-stop] all services stopped"
