#!/usr/bin/env bash

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="${PROJECT_DIR}/.run"
PID_FILE="${RUN_DIR}/app.pid"
LOG_FILE="${RUN_DIR}/app.log"

mkdir -p "${RUN_DIR}"

if [[ -f "${PID_FILE}" ]] && kill -0 "$(cat "${PID_FILE}")" 2>/dev/null; then
  echo "sample_file_manage 애플리케이션이 이미 실행 중입니다. PID=$(cat "${PID_FILE}")"
else
  cd "${PROJECT_DIR}"
  nohup ./gradlew bootRun > "${LOG_FILE}" 2>&1 &
  echo $! > "${PID_FILE}"
  sleep 8
fi

echo "sample_file_manage 실행 정보"
echo "- 애플리케이션 URL: http://localhost:8080"
echo "- H2 콘솔: http://localhost:8080/h2-console"
echo "- 업로드 API: POST http://localhost:8080/api/files"
echo "- 파일 목록 API: GET http://localhost:8080/api/files"
echo "- 데모 계정 정보: 인증 없음, uploadedBy/downloadedBy 파라미터로 사용자 식별"
echo "- 저장 경로: ${PROJECT_DIR}/data/files"
echo "- 로그 파일: ${LOG_FILE}"
