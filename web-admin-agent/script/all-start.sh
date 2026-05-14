#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PID_FILE="$ROOT_DIR/.next-dev.pid"
LOG_FILE="$ROOT_DIR/.next-dev.log"
APP_URL="${APP_URL:-http://localhost:3333}"
API_URL="${NEXT_PUBLIC_API_URL:-http://localhost:8081}"
DEMO_ID="${DEMO_ID:-admin@example.com}"
DEMO_PASSWORD="${DEMO_PASSWORD:-Qwer1234!}"

cd "$ROOT_DIR"

if [[ -f "$PID_FILE" ]] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
  echo "이미 개발 서버가 실행 중입니다. PID=$(cat "$PID_FILE")"
else
  npm run dev >"$LOG_FILE" 2>&1 &
  echo "$!" >"$PID_FILE"
  echo "개발 서버를 시작했습니다. PID=$(cat "$PID_FILE")"
fi

echo ""
echo "접속 URL: $APP_URL"
echo "API 서버 URL: $API_URL"
echo "데모 계정: $DEMO_ID / $DEMO_PASSWORD"
echo "로그 파일: $LOG_FILE"
