#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
ROOT_DIR=$(cd "$SCRIPT_DIR/.." && pwd)
RUNTIME_DIR="$ROOT_DIR/.runtime"
PID_DIR="$RUNTIME_DIR/pids"
LOG_DIR="$RUNTIME_DIR/logs"

mkdir -p "$PID_DIR" "$LOG_DIR"

ensure_pid_stopped() {
  local pid_file="$1"
  local service_name="$2"

  if [[ -f "$pid_file" ]]; then
    local pid
    pid=$(cat "$pid_file")
    if kill -0 "$pid" 2>/dev/null; then
      echo "$service_name is already running with PID $pid."
      return 0
    fi
    rm -f "$pid_file"
  fi
  return 1
}

stop_pid_file() {
  local pid_file="$1"
  local service_name="$2"
  local port="${3:-}"

  if [[ ! -f "$pid_file" ]]; then
    if [[ -n "$port" ]]; then
      stop_listener_on_port "$service_name" "$port"
      return 0
    fi
    echo "$service_name is not running."
    return 0
  fi

  local pid
  pid=$(cat "$pid_file")

  if kill -0 "$pid" 2>/dev/null; then
    kill "$pid" 2>/dev/null || true
    wait_for_pid_exit "$pid" 20
    echo "$service_name stopped."
  else
    echo "$service_name process $pid is not running."
  fi

  if [[ -n "$port" ]]; then
    stop_listener_on_port "$service_name" "$port"
  fi

  rm -f "$pid_file"
}

stop_listener_on_port() {
  local service_name="$1"
  local port="$2"
  local listener_pid
  listener_pid=$(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null | head -n 1 || true)

  if [[ -z "$listener_pid" ]]; then
    echo "$service_name is not running."
    return 0
  fi

  kill "$listener_pid" 2>/dev/null || true
  wait_for_pid_exit "$listener_pid" 20
  echo "$service_name listener on port $port stopped."
}

wait_for_pid_exit() {
  local pid="$1"
  local timeout="${2:-20}"
  local elapsed=0

  while kill -0 "$pid" 2>/dev/null; do
    if (( elapsed >= timeout )); then
      kill -9 "$pid" 2>/dev/null || true
      break
    fi
    sleep 1
    elapsed=$((elapsed + 1))
  done
}

wait_for_http() {
  local url="$1"
  local timeout="${2:-60}"
  local label="${3:-service}"
  local elapsed=0

  until curl -fsS "$url" >/dev/null 2>&1; do
    if (( elapsed >= timeout )); then
      echo "Timed out waiting for $label at $url" >&2
      return 1
    fi
    sleep 1
    elapsed=$((elapsed + 1))
  done
}

wait_for_http_status() {
  local url="$1"
  local expected_status="$2"
  local timeout="${3:-60}"
  local label="${4:-service}"
  shift 4
  local elapsed=0
  local status

  while true; do
    status=$(curl -s -o /dev/null -w '%{http_code}' "$@" "$url" 2>/dev/null || true)
    if [[ "$status" == "$expected_status" ]]; then
      return 0
    fi

    if (( elapsed >= timeout )); then
      echo "Timed out waiting for $label at $url to return $expected_status. Last status: ${status:-n/a}" >&2
      return 1
    fi

    sleep 1
    elapsed=$((elapsed + 1))
  done
}

wait_for_eureka_app() {
  local app_name="$1"
  local timeout="${2:-60}"
  local elapsed=0

  while true; do
    if curl -fsS "http://localhost:8761/eureka/apps/${app_name}" >/dev/null 2>&1; then
      return 0
    fi

    if (( elapsed >= timeout )); then
      echo "Timed out waiting for Eureka registration for ${app_name}" >&2
      return 1
    fi

    sleep 1
    elapsed=$((elapsed + 1))
  done
}

require_free_port() {
  local port="$1"
  local service_name="$2"

  if lsof -tiTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "Port $port is already in use. Cannot start $service_name." >&2
    return 1
  fi
}

stop_legacy_backend_if_present() {
  local legacy_pid_file="$PID_DIR/backend.pid"
  if [[ -f "$legacy_pid_file" ]]; then
    local legacy_pid
    legacy_pid=$(cat "$legacy_pid_file")
    if kill -0 "$legacy_pid" 2>/dev/null; then
      echo "Stopping legacy monolith backend on PID $legacy_pid."
      kill "$legacy_pid" 2>/dev/null || true
      wait_for_pid_exit "$legacy_pid" 20
    fi
    rm -f "$legacy_pid_file"
  fi

  local listener_pid
  listener_pid=$(lsof -tiTCP:8080 -sTCP:LISTEN 2>/dev/null | head -n 1 || true)
  if [[ -n "$listener_pid" ]] && ps -p "$listener_pid" -o command= | grep -q "BankingPlatformApplication"; then
    echo "Stopping legacy monolith backend on PID $listener_pid."
    kill "$listener_pid" 2>/dev/null || true
    wait_for_pid_exit "$listener_pid" 20
  fi
}

start_background_process() {
  local service_name="$1"
  local pid_file="$2"
  local log_file="$3"
  local startup_url="$4"
  shift 4

  nohup "$@" >"$log_file" 2>&1 &
  local pid=$!
  echo "$pid" >"$pid_file"

  if ! wait_for_http "$startup_url" 90 "$service_name"; then
    echo "$service_name failed to start. Recent log output:" >&2
    tail -n 120 "$log_file" >&2 || true
    rm -f "$pid_file"
    return 1
  fi

  echo "$service_name started. PID=$pid, log=$log_file"
}

print_info_block() {
  local title="$1"
  shift

  echo "[$title]"
  for line in "$@"; do
    echo "- $line"
  done
}
