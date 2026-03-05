#!/bin/sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROJECT_DIR=$(CDPATH= cd -- "${SCRIPT_DIR}/.." && pwd)
APP_DIR="${PROJECT_DIR}/app"
RUNTIME_DIR="${PROJECT_DIR}/runtime"
DEPLOY_DIR="${RUNTIME_DIR}/deploy"
PID_DIR="${RUNTIME_DIR}/pids"
LOG_DIR="${RUNTIME_DIR}/logs"
UPSTREAM_DIR="${PROJECT_DIR}/nginx/upstreams"
ACTIVE_FILE="${UPSTREAM_DIR}/active-upstream.conf"
NGINX_RELOAD_CMD=${NGINX_RELOAD_CMD:-"nginx -s reload"}

ensure_dirs() {
  mkdir -p "${DEPLOY_DIR}/blue" "${DEPLOY_DIR}/green" "${PID_DIR}" "${LOG_DIR}"
}

port_of() {
  color=$1
  if [ "${color}" = "blue" ]; then
    echo "18081"
  else
    echo "18082"
  fi
}

pid_file_of() {
  color=$1
  echo "${PID_DIR}/${color}.pid"
}

version_file_of() {
  color=$1
  echo "${DEPLOY_DIR}/${color}/version.txt"
}

jar_link_of() {
  color=$1
  echo "${DEPLOY_DIR}/${color}/current.jar"
}

active_color() {
  if grep -q "18081" "${ACTIVE_FILE}"; then
    echo "blue"
    return
  fi
  if grep -q "18082" "${ACTIVE_FILE}"; then
    echo "green"
    return
  fi
  echo "unknown"
}

is_running() {
  color=$1
  pid_file=$(pid_file_of "${color}")

  if [ ! -f "${pid_file}" ]; then
    return 1
  fi

  pid=$(cat "${pid_file}")
  if [ -z "${pid}" ]; then
    return 1
  fi

  if kill -0 "${pid}" 2>/dev/null; then
    return 0
  fi

  return 1
}

stop_slot() {
  color=$1
  pid_file=$(pid_file_of "${color}")

  if ! is_running "${color}"; then
    rm -f "${pid_file}"
    return 0
  fi

  pid=$(cat "${pid_file}")
  kill "${pid}" 2>/dev/null || true

  i=0
  while [ "${i}" -lt 10 ]; do
    if ! kill -0 "${pid}" 2>/dev/null; then
      break
    fi
    sleep 1
    i=$((i + 1))
  done

  if kill -0 "${pid}" 2>/dev/null; then
    kill -9 "${pid}" 2>/dev/null || true
  fi

  rm -f "${pid_file}"
}

start_slot() {
  color=$1
  version=$2
  port=$(port_of "${color}")
  jar_link=$(jar_link_of "${color}")
  pid_file=$(pid_file_of "${color}")
  log_file="${LOG_DIR}/${color}.log"

  if [ ! -f "${jar_link}" ]; then
    echo "[오류] ${color} 슬롯의 실행 파일이 없습니다: ${jar_link}" >&2
    exit 1
  fi

  stop_slot "${color}"

  nohup java -jar "${jar_link}" \
    --server.port="${port}" \
    --deploy.color="${color}" \
    --deploy.version="${version}" \
    > "${log_file}" 2>&1 &

  echo "$!" > "${pid_file}"
}

wait_for_health() {
  color=$1
  port=$(port_of "${color}")

  i=0
  while [ "${i}" -lt 30 ]; do
    if curl -fsS "http://127.0.0.1:${port}/actuator/health" >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
    i=$((i + 1))
  done

  echo "[오류] ${color} 슬롯 헬스체크 실패: http://127.0.0.1:${port}/actuator/health" >&2
  return 1
}

switch_traffic() {
  color=$1
  cp "${UPSTREAM_DIR}/${color}.conf" "${ACTIVE_FILE}"
  if ! sh -c "${NGINX_RELOAD_CMD}"; then
    echo "[오류] nginx reload 실패. NGINX_RELOAD_CMD 값을 확인하세요." >&2
    return 1
  fi
  return 0
}

next_color() {
  current=$1
  if [ "${current}" = "blue" ]; then
    echo "green"
  else
    echo "blue"
  fi
}

validate_color() {
  color=$1
  if [ "${color}" != "blue" ] && [ "${color}" != "green" ]; then
    echo "[오류] 색상은 blue 또는 green 만 허용됩니다: ${color}" >&2
    exit 1
  fi
}
