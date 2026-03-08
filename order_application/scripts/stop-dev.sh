#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="${ROOT_DIR}/.run"
WEB_API_PORT="${WEB_API_PORT:-8080}"
API_ADMIN_PORT="${API_ADMIN_PORT:-8081}"
WEB_APP_PORT="${WEB_APP_PORT:-3000}"
ADMIN_APP_PORT="${ADMIN_APP_PORT:-3001}"

is_running() {
  local pid="$1"
  kill -0 "${pid}" >/dev/null 2>&1
}

kill_child_processes() {
  local pid="$1"
  local children
  children="$(pgrep -P "${pid}" || true)"

  if [[ -z "${children}" ]]; then
    return 0
  fi

  local child
  for child in ${children}; do
    kill_child_processes "${child}"
    kill "${child}" >/dev/null 2>&1 || true
  done
}

stop_process() {
  local name="$1"
  local port="$2"
  local pid_file="${RUN_DIR}/${name}.pid"
  local pid

  if [[ -f "${pid_file}" ]]; then
    pid="$(cat "${pid_file}")"
    if is_running "${pid}"; then
      kill_child_processes "${pid}"
      kill "${pid}" >/dev/null 2>&1 || true
      sleep 1
      if is_running "${pid}"; then
        kill_child_processes "${pid}"
        kill -9 "${pid}" >/dev/null 2>&1 || true
      fi
      echo "stopped ${name}, pid=${pid}"
    else
      echo "${name} had stale pid ${pid}"
    fi
    rm -f "${pid_file}"
    return 0
  fi

  pid="$(lsof -tiTCP:"${port}" -sTCP:LISTEN || true)"
  if [[ -n "${pid}" ]]; then
    kill "${pid}" >/dev/null 2>&1 || true
    echo "stopped ${name} by port ${port}, pid=${pid}"
  else
    echo "${name} is not running"
  fi
}

stop_process "admin-app" "${ADMIN_APP_PORT}"
stop_process "web-app" "${WEB_APP_PORT}"
stop_process "api-admin" "${API_ADMIN_PORT}"
stop_process "web-api" "${WEB_API_PORT}"

echo "all services stopped"
