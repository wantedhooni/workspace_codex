#!/usr/bin/env sh
set -eu

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
PID_DIR="$ROOT_DIR/runtime/pids"

BACKEND_PID_FILE="$PID_DIR/backend.pid"
FRONTEND_PID_FILE="$PID_DIR/frontend.pid"

is_pid_running() {
  pid="$1"
  [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null
}

stop_by_pid_file() {
  name="$1"
  pid_file="$2"

  if [ ! -f "$pid_file" ]; then
    echo "[SKIP] $name PID 파일이 없습니다."
    return 0
  fi

  pid="$(cat "$pid_file")"
  if ! is_pid_running "$pid"; then
    echo "[SKIP] $name 이미 종료됨 (PID: $pid)"
    rm -f "$pid_file"
    return 0
  fi

  echo "[INFO] $name 종료 중 (PID: $pid)"
  kill "$pid" 2>/dev/null || true

  retries=0
  while [ "$retries" -lt 10 ]; do
    if ! is_pid_running "$pid"; then
      rm -f "$pid_file"
      echo "[OK] $name 종료 완료"
      return 0
    fi
    retries=$((retries + 1))
    sleep 1
  done

  echo "[WARN] $name 강제 종료 (PID: $pid)"
  kill -9 "$pid" 2>/dev/null || true
  rm -f "$pid_file"
}

stop_by_pid_file "Frontend" "$FRONTEND_PID_FILE"
stop_by_pid_file "Backend" "$BACKEND_PID_FILE"

echo "[DONE] 전체 중지 완료"
