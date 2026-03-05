#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./common.sh
source "${SCRIPT_DIR}/common.sh"
ensure_runtime

TARGET="$(target_color "${1:-}")"
CURRENT="$(active_color)"
VERSION="${DEPLOY_VERSION:-$(date +%Y%m%d%H%M%S)}"

validate_color "${TARGET}"

echo "[정보] 현재 활성 색상: ${CURRENT}"
echo "[정보] 배포 대상 색상: ${TARGET}"
echo "[정보] 배포 버전: ${VERSION}"

echo "[1/4] 애플리케이션 빌드"
(
  cd "${PROJECT_DIR}/app"
  ./gradlew clean bootJar
)

echo "[2/4] app-${TARGET} 컨테이너 기동"
DEPLOY_VERSION="${VERSION}" compose up -d --build "app-${TARGET}"
save_version "${TARGET}" "${VERSION}"

echo "[3/4] app-${TARGET} 헬스체크"
wait_for_health "${TARGET}"

echo "[4/4] nginx 업스트림 전환"
compose up -d nginx
switch_upstream "${TARGET}"

if [[ "${CURRENT}" == "blue" || "${CURRENT}" == "green" ]]; then
  if [[ "${CURRENT}" != "${TARGET}" ]]; then
    echo "[정리] 이전 슬롯 app-${CURRENT} 중지"
    compose stop "app-${CURRENT}" >/dev/null
  fi
fi

echo "[완료] active=${TARGET} ingress=http://127.0.0.1:8098/api/deployment"
