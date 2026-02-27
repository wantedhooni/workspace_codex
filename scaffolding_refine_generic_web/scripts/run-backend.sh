#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="${ROOT_DIR}/backand"
PORT="${PORT:-8090}"
LOG_FILE="${LOG_FILE:-/tmp/api-admin.log}"
PID_FILE="${PID_FILE:-/tmp/portal-backend.pid}"

stop_existing_backend() {
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
    existing_pids="$(lsof -ti :"${PORT}" || true)"
    if [ -n "${existing_pids}" ]; then
      echo "${existing_pids}" | xargs kill >/dev/null 2>&1 || true
      sleep 1
      echo "${existing_pids}" | xargs -r kill -9 >/dev/null 2>&1 || true
    fi
  fi
}

# Build boot jar first for stable background execution (gradle bootRun can detach unexpectedly under nohup).
bash -lc "cd \"${BACKEND_DIR}\" && ./gradlew :server:api-admin-server:bootJar --no-daemon" >/dev/null

JAR_FILE="$(ls -1t "${BACKEND_DIR}"/server/api-admin-server/build/libs/api-admin-server-*.jar | grep -v -- '-plain.jar' | head -n 1)"
if [ -z "${JAR_FILE}" ]; then
  echo "Backend jar not found under ${BACKEND_DIR}/server/api-admin-server/build/libs" >&2
  exit 1
fi

stop_existing_backend

cd "${BACKEND_DIR}"
nohup java -jar "${JAR_FILE}" --server.port="${PORT}" > "${LOG_FILE}" 2>&1 &
echo $! > "${PID_FILE}"
echo "Backend PID: $(cat "${PID_FILE}")"
