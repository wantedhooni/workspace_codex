#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PID_DIR="$ROOT_DIR/pids"

SERVICES=(
  "web-application|3002"
  "admin-portal|3001"
  "order-service|8082"
  "user-service|8081"
  "auth-server|9000"
  "api-gateway|8000"
  "config-server|8888"
  "discovery-service|8761"
)

wait_for_port_closed() {
  local port="$1"

  for _ in {1..20}; do
    if ! lsof -tiTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
  done

  return 1
}

kill_listeners_on_port() {
  local service="$1"
  local port="$2"
  local listeners

  listeners="$(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null || true)"
  if [[ -z "$listeners" ]]; then
    return 0
  fi

  echo "[all-stop] killing remaining listeners on ${service} port=${port}: ${listeners}"
  while IFS= read -r listener_pid; do
    [[ -z "$listener_pid" ]] && continue
    kill "$listener_pid" >/dev/null 2>&1 || true
  done <<<"$listeners"

  if ! wait_for_port_closed "$port"; then
    listeners="$(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null || true)"
    while IFS= read -r listener_pid; do
      [[ -z "$listener_pid" ]] && continue
      kill -9 "$listener_pid" >/dev/null 2>&1 || true
    done <<<"$listeners"
  fi
}

stop_service() {
  local service="$1"
  local port="$2"
  local pid_file="$PID_DIR/${service}.pid"

  if [[ -f "$pid_file" ]]; then
    local pid
    pid="$(cat "$pid_file")"

    if kill -0 "$pid" >/dev/null 2>&1; then
      echo "[all-stop] stopping ${service} pid=${pid}"
      kill "$pid" >/dev/null 2>&1 || true
      for _ in {1..20}; do
        if ! kill -0 "$pid" >/dev/null 2>&1; then
          break
        fi
        sleep 1
      done

      if kill -0 "$pid" >/dev/null 2>&1; then
        echo "[all-stop] force killing ${service} pid=${pid}"
        kill -9 "$pid" >/dev/null 2>&1 || true
      fi
    else
      echo "[all-stop] ${service} already stopped"
    fi

    rm -f "$pid_file"
  else
    echo "[all-stop] ${service} pid file not found"
  fi

  kill_listeners_on_port "$service" "$port"

  if wait_for_port_closed "$port"; then
    echo "[all-stop] ${service} stopped"
  else
    echo "[all-stop] ${service} port ${port} is still in use" >&2
    return 1
  fi
}

for item in "${SERVICES[@]}"; do
  IFS='|' read -r service port <<<"$item"
  stop_service "$service" "$port"
done

echo "[all-stop] all services stopped"
