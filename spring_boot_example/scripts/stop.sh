#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
RUNTIME_DIR="${PROJECT_ROOT}/.run"
PID_FILE="${RUNTIME_DIR}/spring-ai-example.pid"

if [[ ! -f "${PID_FILE}" ]]; then
  echo "실행 중인 프로세스를 찾지 못했습니다. (${PID_FILE} 없음)"
  exit 0
fi

PID="$(cat "${PID_FILE}")"
if [[ ! "${PID}" =~ ^[0-9]+$ ]]; then
  echo "PID 파일 값이 올바르지 않습니다. PID 파일을 정리합니다."
  rm -f "${PID_FILE}"
  exit 0
fi

if ! kill -0 "${PID}" 2>/dev/null; then
  echo "프로세스가 이미 종료된 상태입니다. PID 파일을 정리합니다."
  rm -f "${PID_FILE}"
  exit 0
fi

echo "종료 시도: pid=${PID}"
kill "${PID}"

for _ in {1..20}; do
  if ! kill -0 "${PID}" 2>/dev/null; then
    rm -f "${PID_FILE}"
    echo "정상 종료되었습니다."
    exit 0
  fi
  sleep 0.5
done

echo "정상 종료 타임아웃. 강제 종료합니다: pid=${PID}"
kill -9 "${PID}" 2>/dev/null || true
rm -f "${PID_FILE}"
echo "강제 종료 완료."
