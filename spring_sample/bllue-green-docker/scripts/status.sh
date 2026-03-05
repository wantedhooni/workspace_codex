#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./common.sh
source "${SCRIPT_DIR}/common.sh"
ensure_runtime

echo "[상태] active=$(active_color)"
echo "[상태] saved-version blue=$(read_version blue) green=$(read_version green)"
echo "[상태] 컨테이너"
compose ps

echo "[상태] blue 응답"
curl -fsS http://127.0.0.1:19081/api/deployment || echo "blue 응답 없음"

echo "[상태] green 응답"
curl -fsS http://127.0.0.1:19082/api/deployment || echo "green 응답 없음"

echo "[상태] ingress 응답"
curl -fsS http://127.0.0.1:8098/api/deployment || echo "ingress 응답 없음"
