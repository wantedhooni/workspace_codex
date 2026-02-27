#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
LOCAL_DIR="${ROOT_DIR}/scripts/local"
BACKEND_PORT="${BACKEND_PORT:-8080}"
FRONTEND_PORT="${FRONTEND_PORT:-3000}"

kill_listeners_by_port() {
  local port="$1"
  local label="$2"
  local pids
  local remaining

  pids="$(lsof -tiTCP:${port} -sTCP:LISTEN 2>/dev/null || true)"
  if [ -z "${pids}" ]; then
    echo "[stop-all] ${label}: no listener on port ${port}"
    return
  fi

  echo "[stop-all] ${label}: stopping pid(s) on port ${port}: ${pids}"
  for pid in ${pids}; do
    kill "${pid}" 2>/dev/null || true
  done

  for _ in $(seq 1 10); do
    remaining="$(lsof -tiTCP:${port} -sTCP:LISTEN 2>/dev/null || true)"
    if [ -z "${remaining}" ]; then
      echo "[stop-all] ${label}: stopped"
      return
    fi
    sleep 1
  done

  echo "[stop-all] ${label}: forcing shutdown for pid(s): ${remaining}"
  for pid in ${remaining}; do
    kill -9 "${pid}" 2>/dev/null || true
  done
}

echo "[stop-all] stopping local app services"
kill_listeners_by_port "${FRONTEND_PORT}" "frontend"
kill_listeners_by_port "${BACKEND_PORT}" "backend"

echo "[stop-all] stopping local mariadb"
"${LOCAL_DIR}/db-down.sh" > /dev/null || true

echo "[stop-all] done (backend:${BACKEND_PORT}, frontend:${FRONTEND_PORT})"

