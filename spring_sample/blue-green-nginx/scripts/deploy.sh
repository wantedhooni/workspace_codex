#!/bin/sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
# shellcheck disable=SC1091
. "${SCRIPT_DIR}/common.sh"

ensure_dirs

REQUESTED_COLOR=${1:-}
CURRENT=$(active_color)
if [ -n "${REQUESTED_COLOR}" ]; then
  TARGET=${REQUESTED_COLOR}
else
  TARGET=$(next_color "${CURRENT}")
fi
validate_color "${TARGET}"

VERSION=${DEPLOY_VERSION:-$(date +%Y%m%d%H%M%S)}

echo "[정보] 현재 활성 색상: ${CURRENT}"
echo "[정보] 배포 대상 색상: ${TARGET}"
echo "[정보] 배포 버전: ${VERSION}"

echo "[1/5] 애플리케이션 빌드"
(
  cd "${APP_DIR}"
  ./gradlew clean bootJar >/dev/null
)

ARTIFACT=$(find "${APP_DIR}/build/libs" -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | head -n 1)
if [ -z "${ARTIFACT}" ]; then
  echo "[오류] 실행 jar를 찾을 수 없습니다." >&2
  exit 1
fi

TARGET_DIR="${DEPLOY_DIR}/${TARGET}"
TARGET_JAR="${TARGET_DIR}/app-${VERSION}.jar"
TARGET_LINK="${TARGET_DIR}/current.jar"

cp "${ARTIFACT}" "${TARGET_JAR}"
ln -sfn "${TARGET_JAR}" "${TARGET_LINK}"
echo "${VERSION}" > "$(version_file_of "${TARGET}")"

echo "[2/5] ${TARGET} 슬롯 기동"
start_slot "${TARGET}" "${VERSION}"

echo "[3/5] ${TARGET} 슬롯 헬스체크"
if ! wait_for_health "${TARGET}"; then
  stop_slot "${TARGET}"
  exit 1
fi

echo "[4/5] nginx 트래픽 전환"
if ! switch_traffic "${TARGET}"; then
  stop_slot "${TARGET}"
  exit 1
fi

echo "[5/5] 기존 슬롯 정리"
if [ "${CURRENT}" = "blue" ] || [ "${CURRENT}" = "green" ]; then
  if [ "${CURRENT}" != "${TARGET}" ]; then
    stop_slot "${CURRENT}"
  fi
fi

echo "[완료] active=${TARGET} ingress=http://127.0.0.1:8088/api/deployment"
