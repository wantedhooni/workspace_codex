#!/usr/bin/env bash

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PID_FILE="${PROJECT_DIR}/.run/app.pid"

if [[ ! -f "${PID_FILE}" ]]; then
  echo "중지할 sample_file_manage 프로세스 PID 파일이 없습니다."
  exit 0
fi

PID="$(cat "${PID_FILE}")"
if kill -0 "${PID}" 2>/dev/null; then
  kill "${PID}"
  echo "sample_file_manage 애플리케이션을 중지했습니다. PID=${PID}"
else
  echo "이미 종료된 PID입니다. PID=${PID}"
fi

rm -f "${PID_FILE}"
