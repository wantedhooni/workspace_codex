#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

for SERVER in user-server admin-server; do
  PID_FILE="/tmp/web-sample-${SERVER}.pid"
  if [[ -f "$PID_FILE" ]]; then
    PID="$(cat "$PID_FILE")"
    if kill -0 "$PID" >/dev/null 2>&1; then
      kill "$PID"
    fi
    rm -f "$PID_FILE"
  fi
done

docker compose stop user-postgres admin-postgres redis

echo "USER 서버, ADMIN 서버, PostgreSQL, Redis 중지 완료"
