#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
PID_FILE="$ROOT_DIR/runtime/pids/app.pid"

is_running() {
  local pid="$1"
  [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null
}

if [[ ! -f "$PID_FILE" ]]; then
  echo "[SKIP] 실행 중인 sample_ranking PID 파일이 없습니다."
  exit 0
fi

pid="$(cat "$PID_FILE")"
if ! is_running "$pid"; then
  rm -f "$PID_FILE"
  echo "[SKIP] sample_ranking 프로세스가 이미 종료되었습니다. PID=$pid"
  exit 0
fi

echo "[INFO] sample_ranking 종료 중. PID=$pid"
kill "$pid" 2>/dev/null || true

for _ in $(seq 1 10); do
  if ! is_running "$pid"; then
    rm -f "$PID_FILE"
    echo "[DONE] sample_ranking 종료 완료"
    exit 0
  fi
  sleep 1
done

kill -9 "$pid" 2>/dev/null || true
rm -f "$PID_FILE"
echo "[WARN] sample_ranking 강제 종료 완료"
