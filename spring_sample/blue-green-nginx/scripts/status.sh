#!/bin/sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
# shellcheck disable=SC1091
. "${SCRIPT_DIR}/common.sh"

ensure_dirs

ACTIVE=$(active_color)
echo "[상태] 활성 색상: ${ACTIVE}"

for color in blue green; do
  port=$(port_of "${color}")
  pid_file=$(pid_file_of "${color}")
  version_file=$(version_file_of "${color}")

  if is_running "${color}"; then
    pid=$(cat "${pid_file}")
    run_state="RUNNING"
  else
    pid="-"
    run_state="STOPPED"
  fi

  if [ -f "${version_file}" ]; then
    version=$(cat "${version_file}")
  else
    version="-"
  fi

  echo "[슬롯] ${color} port=${port} state=${run_state} pid=${pid} version=${version}"
  curl -fsS "http://127.0.0.1:${port}/api/deployment" 2>/dev/null || echo "[슬롯] ${color} 응답 없음"
done

echo "[상태] ingress"
curl -fsS "http://127.0.0.1:8088/api/deployment" 2>/dev/null || echo "[상태] ingress 응답 없음"
