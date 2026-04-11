#!/usr/bin/env bash

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="${PROJECT_DIR}/.run"
PID_FILE="${RUN_DIR}/app.pid"
LOG_FILE="${RUN_DIR}/app.log"

mkdir -p "${RUN_DIR}"

if [[ -f "${PID_FILE}" ]] && kill -0 "$(cat "${PID_FILE}")" 2>/dev/null; then
  echo "sample_recommend 애플리케이션이 이미 실행 중입니다. PID=$(cat "${PID_FILE}")"
else
  cd "${PROJECT_DIR}"
  nohup ./gradlew bootRun > "${LOG_FILE}" 2>&1 &
  echo $! > "${PID_FILE}"
  sleep 8
fi

echo "sample_recommend 실행 정보"
echo "- 애플리케이션 URL: http://localhost:8080"
echo "- H2 콘솔: http://localhost:8080/h2-console"
echo "- 추천 API 예시: http://localhost:8080/api/recommendations?customerId=CUST-001&limit=3"
echo "- 데모 고객: CUST-001 (AUDIO/VIP), CUST-002 (OFFICE), CUST-003 (WEARABLE)"
echo "- 로그 파일: ${LOG_FILE}"

