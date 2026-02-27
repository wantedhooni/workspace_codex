#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_PORT="${BACKEND_PORT:-8090}"
API_URL="${API_URL:-http://localhost:${BACKEND_PORT}}"
BACKEND_LOG="${BACKEND_LOG:-/tmp/api-admin.log}"
FRONTEND_LOG="${FRONTEND_LOG:-/tmp/admin-ui.log}"
BACKEND_PID="${BACKEND_PID:-/tmp/portal-backend.pid}"
FRONTEND_PID="${FRONTEND_PID:-/tmp/portal-frontend.pid}"

echo "[1/4] Start Postgres (docker compose)"
cd "${ROOT_DIR}"
docker compose up -d

echo "[2/4] Start backend (Spring Boot)"
cd "${ROOT_DIR}"
PORT="${BACKEND_PORT}" LOG_FILE="${BACKEND_LOG}" PID_FILE="${BACKEND_PID}" "${ROOT_DIR}/scripts/run-backend.sh"

echo "      Waiting for backend on :${BACKEND_PORT}"
for i in {1..60}; do
  code="$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:${BACKEND_PORT}/v3/api-docs" || true)"
  if [ "${code}" = "200" ]; then
    echo "      Backend is ready."
    break
  fi
  if [ "${i}" -eq 60 ]; then
    echo "      Backend did not become ready in time. Check ${BACKEND_LOG}" >&2
    exit 1
  fi
  sleep 1
done

echo "[3/4] Start frontend (Vite)"
cd "${ROOT_DIR}"
API_URL="${API_URL}" LOG_FILE="${FRONTEND_LOG}" PID_FILE="${FRONTEND_PID}" "${ROOT_DIR}/scripts/run-frontend.sh"

echo "      Waiting for frontend on :5173"
for i in {1..40}; do
  code="$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:5173" || true)"
  if [ "${code}" = "200" ]; then
    echo "      Frontend is ready."
    break
  fi
  if [ "${i}" -eq 40 ]; then
    echo "      Frontend did not become ready in time. Check ${FRONTEND_LOG}" >&2
    exit 1
  fi
  sleep 1
done

echo "[4/4] Done"
echo "Backend:  http://localhost:${BACKEND_PORT}"
echo "Swagger:  http://localhost:${BACKEND_PORT}/swagger-ui/index.html"
echo "Frontend: http://localhost:5173"
echo "Postgres: localhost:55433"
echo "Logs:     ${BACKEND_LOG}, ${FRONTEND_LOG}"
echo "PIDs:     ${BACKEND_PID}, ${FRONTEND_PID}"
echo ""
echo "Demo/Test Accounts"
echo "- admin / admin1234 (SUPER_ADMIN)"
echo "- tester / tester1234 (CONTENT_MANAGER)"
echo "- ops / ops1234 (OPS_MANAGER)"
echo "- manager / manager1234 (AUDITOR)"
echo "- security / security1234 (SECURITY_ADMIN)"
