#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
COMPOSE_FILE="${PROJECT_DIR}/docker-compose.yml"
UPSTREAM_DIR="${PROJECT_DIR}/nginx/upstreams"
ACTIVE_FILE="${UPSTREAM_DIR}/active-upstream.conf"
RUNTIME_DIR="${PROJECT_DIR}/runtime"
VERSION_DIR="${RUNTIME_DIR}/versions"

compose() {
  docker compose -f "${COMPOSE_FILE}" "$@"
}

ensure_runtime() {
  mkdir -p "${VERSION_DIR}"
}

active_color() {
  if grep -q "app-blue" "${ACTIVE_FILE}"; then
    echo "blue"
  elif grep -q "app-green" "${ACTIVE_FILE}"; then
    echo "green"
  else
    echo "unknown"
  fi
}

target_color() {
  local requested="${1:-}"
  local current
  current="$(active_color)"

  if [[ -n "${requested}" ]]; then
    echo "${requested}"
    return
  fi

  if [[ "${current}" == "blue" ]]; then
    echo "green"
  else
    echo "blue"
  fi
}

validate_color() {
  local color="$1"
  if [[ "${color}" != "blue" && "${color}" != "green" ]]; then
    echo "[오류] 허용 색상: blue | green (입력: ${color})" >&2
    exit 1
  fi
}

port_of() {
  local color="$1"
  if [[ "${color}" == "blue" ]]; then
    echo "19081"
  else
    echo "19082"
  fi
}

wait_for_health() {
  local color="$1"
  local port
  port="$(port_of "${color}")"

  for _ in {1..30}; do
    if curl -fsS "http://127.0.0.1:${port}/actuator/health" >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done

  echo "[오류] app-${color} 헬스체크 실패: http://127.0.0.1:${port}/actuator/health" >&2
  return 1
}

switch_upstream() {
  local color="$1"
  cp "${UPSTREAM_DIR}/${color}.conf" "${ACTIVE_FILE}"
  compose exec -T nginx nginx -s reload
}

version_file() {
  local color="$1"
  echo "${VERSION_DIR}/${color}.version"
}

save_version() {
  local color="$1"
  local version="$2"
  echo "${version}" > "$(version_file "${color}")"
}

read_version() {
  local color="$1"
  local file
  file="$(version_file "${color}")"
  if [[ -f "${file}" ]]; then
    cat "${file}"
  else
    echo "local"
  fi
}
