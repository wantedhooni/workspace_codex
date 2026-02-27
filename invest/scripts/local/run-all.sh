#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
LOCAL_DIR="${ROOT_DIR}/scripts/local"
AUTO_SEED_LOCAL="${AUTO_SEED_LOCAL:-true}"

BACKEND_PID=""
FRONTEND_PID=""

cleanup() {
  if [ -n "${BACKEND_PID}" ] && kill -0 "${BACKEND_PID}" 2>/dev/null; then
    kill "${BACKEND_PID}" 2>/dev/null || true
  fi

  if [ -n "${FRONTEND_PID}" ] && kill -0 "${FRONTEND_PID}" 2>/dev/null; then
    kill "${FRONTEND_PID}" 2>/dev/null || true
  fi

  wait "${BACKEND_PID}" 2>/dev/null || true
  wait "${FRONTEND_PID}" 2>/dev/null || true
}

trap cleanup INT TERM EXIT

echo "[run-all] starting local services"
"${LOCAL_DIR}/db-up.sh" > /dev/null

"${LOCAL_DIR}/run-backend.sh" &
BACKEND_PID=$!

for _ in $(seq 1 60); do
  if ! kill -0 "${BACKEND_PID}" 2>/dev/null; then
    echo "[run-all] backend process exited before ready"
    wait "${BACKEND_PID}" || true
    exit 1
  fi

  if curl -fsS "http://127.0.0.1:8080/api/v1/public/ping" > /dev/null 2>&1; then
    break
  fi
  sleep 1
done

if [ "${AUTO_SEED_LOCAL}" = "true" ]; then
  echo "[run-all] auto seed: on"
  if ! "${LOCAL_DIR}/seed-all.sh"; then
    echo "[run-all] warning: auto seed failed (continue)"
  fi
else
  echo "[run-all] auto seed: off"
fi

"${LOCAL_DIR}/run-frontend.sh" &
FRONTEND_PID=$!

echo "[run-all] backend pid=${BACKEND_PID}, frontend pid=${FRONTEND_PID}"
echo "[run-all] backend=http://127.0.0.1:8080  frontend=http://127.0.0.1:3000"

while true; do
  if ! kill -0 "${BACKEND_PID}" 2>/dev/null; then
    wait "${BACKEND_PID}"
    exit $?
  fi

  if ! kill -0 "${FRONTEND_PID}" 2>/dev/null; then
    wait "${FRONTEND_PID}"
    exit $?
  fi

  sleep 1
done
