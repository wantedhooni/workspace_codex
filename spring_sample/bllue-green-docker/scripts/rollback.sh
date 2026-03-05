#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./common.sh
source "${SCRIPT_DIR}/common.sh"
ensure_runtime

CURRENT="$(active_color)"
if [[ "${CURRENT}" == "blue" ]]; then
  TARGET="green"
elif [[ "${CURRENT}" == "green" ]]; then
  TARGET="blue"
else
  echo "[오류] 현재 활성 색상을 확인할 수 없습니다." >&2
  exit 1
fi

echo "[정보] 현재 활성 색상: ${CURRENT}"
echo "[정보] 롤백 대상 색상: ${TARGET}"
VERSION="$(read_version "${TARGET}")"
echo "[정보] 롤백 버전: ${VERSION}"

DEPLOY_VERSION="${VERSION}" compose up -d "app-${TARGET}" nginx
wait_for_health "${TARGET}"
switch_upstream "${TARGET}"
compose stop "app-${CURRENT}" >/dev/null

echo "[완료] rollback active=${TARGET}"
