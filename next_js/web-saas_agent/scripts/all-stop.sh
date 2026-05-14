#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PID_FILE="$ROOT_DIR/.next-saas.pid"

if [[ -f "$PID_FILE" ]]; then
  PID="$(cat "$PID_FILE")"
  if kill -0 "$PID" 2>/dev/null; then
    kill "$PID"
    echo "개발 서버를 중지했습니다. PID: $PID"
  else
    echo "실행 중인 개발 서버 PID를 찾지 못했습니다."
  fi
  rm -f "$PID_FILE"
else
  echo "PID 파일이 없습니다. 실행 중인 서버가 없을 수 있습니다."
fi
