#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PID_FILE="$ROOT_DIR/.next-saas.pid"
LOG_FILE="$ROOT_DIR/.next-saas.log"
PORT="${PORT:-3000}"

cd "$ROOT_DIR"

if [[ -f "$PID_FILE" ]] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
  echo "이미 실행 중입니다. PID: $(cat "$PID_FILE")"
else
  nohup npm run dev -- --port "$PORT" > "$LOG_FILE" 2>&1 &
  echo $! > "$PID_FILE"
  echo "개발 서버를 시작했습니다. PID: $(cat "$PID_FILE")"
fi

echo ""
echo "접속 URL: http://localhost:${PORT}"
echo "API 서버 기본값: http://localhost:8091"
echo "데모 계정 ID: demo@example.com"
echo "데모 계정 PW: Qwer1234!"
echo "로그 파일: $LOG_FILE"
