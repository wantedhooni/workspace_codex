#!/usr/bin/env sh
set -eu

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
PID_FILE="$ROOT_DIR/runtime/pids/app.pid"

is_pid_running() {
  pid="$1"
  [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null
}

if [ -f "$PID_FILE" ]; then
  pid="$(cat "$PID_FILE")"
  if is_pid_running "$pid"; then
    echo "[INFO] 애플리케이션 종료 중 (PID: $pid)"
    kill "$pid" 2>/dev/null || true
    sleep 2
    if is_pid_running "$pid"; then
      echo "[WARN] 애플리케이션 강제 종료 (PID: $pid)"
      kill -9 "$pid" 2>/dev/null || true
    fi
  fi
  rm -f "$PID_FILE"
else
  echo "[SKIP] 애플리케이션 PID 파일이 없습니다."
fi

echo "[INFO] Prometheus / Grafana 종료"
(
  cd "$ROOT_DIR"
  docker compose down
)

echo "[DONE] 전체 중지 완료"
