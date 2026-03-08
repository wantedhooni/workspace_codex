#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="${ROOT_DIR}/.run"
LOG_DIR="${ROOT_DIR}/logs"

WEB_API_PORT="${WEB_API_PORT:-8080}"
API_ADMIN_PORT="${API_ADMIN_PORT:-8081}"
WEB_APP_PORT="${WEB_APP_PORT:-3000}"
ADMIN_APP_PORT="${ADMIN_APP_PORT:-3001}"

mkdir -p "${RUN_DIR}" "${LOG_DIR}"

is_port_open() {
  local port="$1"
  lsof -nP -iTCP:"${port}" -sTCP:LISTEN >/dev/null 2>&1
}

is_running() {
  local pid="$1"
  kill -0 "${pid}" >/dev/null 2>&1
}

start_process() {
  local name="$1"
  local workdir="$2"
  local port="$3"
  local command="$4"
  local pid_file="${RUN_DIR}/${name}.pid"
  local log_file="${LOG_DIR}/${name}.log"

  if [[ -f "${pid_file}" ]]; then
      local existing_pid
      existing_pid="$(cat "${pid_file}")"
      if is_running "${existing_pid}"; then
      echo "${name} is already running with PID ${existing_pid}"
      return 0
      fi
      rm -f "${pid_file}"
  fi

  if is_port_open "${port}"; then
    echo "failed to start ${name}: port ${port} is already in use"
    echo "set WEB_API_PORT, API_ADMIN_PORT, WEB_APP_PORT, ADMIN_APP_PORT to override defaults"
    return 1
  fi

  : >"${log_file}"
  bash -lc "
    set -m
    cd '${workdir}'
    nohup bash -lc \"exec ${command}\" >>'${log_file}' 2>&1 &
    pid=\$!
    disown %1
    echo \$pid >'${pid_file}'
  "

  sleep 1

  local pid
  pid="$(cat "${pid_file}")"
  if is_running "${pid}"; then
    echo "started ${name}, pid=${pid}, port=${port}, log=${log_file}"
    return 0
  fi

  echo "failed to start ${name}, see ${log_file}"
  return 1
}

start_process "web-api" "${ROOT_DIR}/backend" "${WEB_API_PORT}" "./gradlew :web-api:bootRun --args='--server.port=${WEB_API_PORT}'"
start_process "api-admin" "${ROOT_DIR}/backend" "${API_ADMIN_PORT}" "./gradlew :api-admin:bootRun --args='--server.port=${API_ADMIN_PORT}'"
start_process "web-app" "${ROOT_DIR}/frontend" "${WEB_APP_PORT}" "npm run dev --workspace web-app -- --port ${WEB_APP_PORT}"
start_process "admin-app" "${ROOT_DIR}/frontend" "${ADMIN_APP_PORT}" "npm run dev --workspace admin-app -- --port ${ADMIN_APP_PORT}"

echo "all services started"
echo
echo "connection info"
echo "- web-api: http://localhost:${WEB_API_PORT}/api/public/health"
echo "- api-admin: http://localhost:${API_ADMIN_PORT}/api/admin/health"
echo "- web-app: http://localhost:${WEB_APP_PORT}"
echo "- admin-app: http://localhost:${ADMIN_APP_PORT}"
