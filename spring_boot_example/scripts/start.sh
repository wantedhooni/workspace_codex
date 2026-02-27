#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
RUNTIME_DIR="${PROJECT_ROOT}/.run"
PID_FILE="${RUNTIME_DIR}/spring-ai-example.pid"
LOG_FILE="${RUNTIME_DIR}/spring-ai-example.log"
GRADLEW="${PROJECT_ROOT}/gradlew"

mkdir -p "${RUNTIME_DIR}"

is_running() {
  if [[ ! -f "${PID_FILE}" ]]; then
    return 1
  fi

  local pid
  pid="$(cat "${PID_FILE}")"
  [[ "${pid}" =~ ^[0-9]+$ ]] || return 1
  kill -0 "${pid}" 2>/dev/null
}

if is_running; then
  echo "이미 실행 중입니다. pid=$(cat "${PID_FILE}")"
  echo "로그: ${LOG_FILE}"
  exit 0
fi

rm -f "${PID_FILE}"

if [[ "${NO_BUILD:-0}" != "1" ]]; then
  echo "bootJar 빌드 중..."
  "${GRADLEW}" -q bootJar
fi

JAR_FILE="$(find "${PROJECT_ROOT}/build/libs" -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | head -n 1)"
if [[ -z "${JAR_FILE}" ]]; then
  echo "실행 가능한 jar를 찾지 못했습니다. 먼저 ./gradlew bootJar 를 확인하세요."
  exit 1
fi

JAVA_OPTS_VALUE="${JAVA_OPTS:-}"
JAVA_OPTS_ARRAY=()
if [[ -n "${JAVA_OPTS_VALUE}" ]]; then
  # shellcheck disable=SC2206
  JAVA_OPTS_ARRAY=(${JAVA_OPTS_VALUE})
fi

echo "애플리케이션 시작: ${JAR_FILE}"
if [[ ${#JAVA_OPTS_ARRAY[@]} -gt 0 ]]; then
  nohup java "${JAVA_OPTS_ARRAY[@]}" -jar "${JAR_FILE}" >"${LOG_FILE}" 2>&1 &
else
  nohup java -jar "${JAR_FILE}" >"${LOG_FILE}" 2>&1 &
fi
APP_PID=$!
echo "${APP_PID}" > "${PID_FILE}"

STARTUP_CHECK_SECONDS="${STARTUP_CHECK_SECONDS:-8}"
for _ in $(seq 1 "${STARTUP_CHECK_SECONDS}"); do
  if ! kill -0 "${APP_PID}" 2>/dev/null; then
    echo "시작 실패. 최근 로그:"
    tail -n 60 "${LOG_FILE}" || true
    rm -f "${PID_FILE}"
    exit 1
  fi
  sleep 1
done

echo "시작 완료. pid=${APP_PID}"
echo "로그: ${LOG_FILE}"
