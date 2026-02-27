#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FRONTEND_DIR="${ROOT_DIR}/frontend/admin-ui"
cd "${FRONTEND_DIR}"
npm install
API_URL="${API_URL:-http://localhost:8090}"
LOG_FILE="${LOG_FILE:-/tmp/admin-ui.log}"
PID_FILE="${PID_FILE:-/tmp/portal-frontend.pid}"

if [ -f "${PID_FILE}" ]; then
  pid="$(cat "${PID_FILE}" || true)"
  if [ -n "${pid}" ] && kill -0 "${pid}" >/dev/null 2>&1; then
    kill "${pid}" >/dev/null 2>&1 || true
    sleep 1
    kill -9 "${pid}" >/dev/null 2>&1 || true
  fi
  rm -f "${PID_FILE}"
fi

if command -v lsof >/dev/null 2>&1; then
  lsof -ti :5173 | xargs -r kill >/dev/null 2>&1 || true
  sleep 1
  lsof -ti :5173 | xargs -r kill -9 >/dev/null 2>&1 || true
fi

nohup env VITE_API_URL="${API_URL}" npm run dev -- --host localhost --port 5173 > "${LOG_FILE}" 2>&1 &
echo $! > "${PID_FILE}"
echo "Frontend PID: $(cat "${PID_FILE}")"
