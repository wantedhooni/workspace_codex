#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_PID="${BACKEND_PID:-/tmp/portal-backend.pid}"
FRONTEND_PID="${FRONTEND_PID:-/tmp/portal-frontend.pid}"

echo "[1/3] Stop frontend (port 5173)"
if [ -f "${FRONTEND_PID}" ]; then
  kill -9 "$(cat "${FRONTEND_PID}")" >/dev/null 2>&1 || true
  rm -f "${FRONTEND_PID}"
fi
if command -v lsof >/dev/null 2>&1; then
  lsof -ti :5173 | xargs -r kill -9 || true
fi

echo "[2/3] Stop backend (AdminApiApplication)"
if [ -f "${BACKEND_PID}" ]; then
  kill "$(cat "${BACKEND_PID}")" >/dev/null 2>&1 || true
  rm -f "${BACKEND_PID}"
fi
if command -v pgrep >/dev/null 2>&1; then
  pgrep -fl "AdminApiApplication|api-admin-server|org.springframework.boot.loader.launch.JarLauncher" | awk '{print $1}' | xargs -r kill || true
fi
if command -v lsof >/dev/null 2>&1; then
  lsof -ti :8090 | xargs -r kill || true
  lsof -ti :8080 | xargs -r kill || true
fi
sleep 1
if command -v lsof >/dev/null 2>&1; then
  lsof -ti :8090 | xargs -r kill -9 || true
  lsof -ti :8080 | xargs -r kill -9 || true
fi

echo "[3/3] Stop docker compose"
cd "${ROOT_DIR}"
docker compose down || true

echo "All stopped."
