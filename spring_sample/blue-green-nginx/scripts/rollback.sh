#!/bin/sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
# shellcheck disable=SC1091
. "${SCRIPT_DIR}/common.sh"

ensure_dirs

CURRENT=$(active_color)
if [ "${CURRENT}" != "blue" ] && [ "${CURRENT}" != "green" ]; then
  echo "[오류] 현재 활성 색상을 확인할 수 없습니다." >&2
  exit 1
fi

TARGET=$(next_color "${CURRENT}")
TARGET_LINK=$(jar_link_of "${TARGET}")
if [ ! -f "${TARGET_LINK}" ]; then
  echo "[오류] 롤백 대상(${TARGET}) 슬롯의 배포 이력이 없어 롤백할 수 없습니다." >&2
  exit 1
fi

if [ -f "$(version_file_of "${TARGET}")" ]; then
  VERSION=$(cat "$(version_file_of "${TARGET}")")
else
  VERSION="rollback"
fi

echo "[정보] 현재 활성 색상: ${CURRENT}"
echo "[정보] 롤백 대상 색상: ${TARGET}"

echo "[1/3] ${TARGET} 슬롯 기동"
start_slot "${TARGET}" "${VERSION}"

echo "[2/3] ${TARGET} 슬롯 헬스체크"
if ! wait_for_health "${TARGET}"; then
  stop_slot "${TARGET}"
  exit 1
fi

echo "[3/3] nginx 트래픽 롤백"
if ! switch_traffic "${TARGET}"; then
  stop_slot "${TARGET}"
  exit 1
fi

stop_slot "${CURRENT}"

echo "[완료] rollback active=${TARGET}"
