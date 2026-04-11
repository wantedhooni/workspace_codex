#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
RUNTIME_DIR="$ROOT_DIR/runtime"
PID_DIR="$RUNTIME_DIR/pids"
LOG_DIR="$RUNTIME_DIR/logs"
PID_FILE="$PID_DIR/app.pid"
LOG_FILE="$LOG_DIR/app.log"
APP_PORT="${APP_PORT:-8092}"

mkdir -p "$PID_DIR" "$LOG_DIR" "$ROOT_DIR/runtime/data"

is_running() {
  local pid="$1"
  [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null
}

listener_pid() {
  lsof -tiTCP:"$1" -sTCP:LISTEN 2>/dev/null | head -n 1 || true
}

wait_for_port() {
  local port="$1"
  local pid="$2"
  local retries=0

  while [[ "$retries" -lt 40 ]]; do
    if [[ -n "$(listener_pid "$port")" ]]; then
      return 0
    fi

    if ! is_running "$pid"; then
      echo "[ERROR] 애플리케이션이 비정상 종료되었습니다. 로그: $LOG_FILE" >&2
      return 1
    fi

    retries=$((retries + 1))
    sleep 1
  done

  echo "[ERROR] 포트 오픈 대기 시간이 초과되었습니다. 로그: $LOG_FILE" >&2
  return 1
}

if [[ -f "$PID_FILE" ]]; then
  current_pid="$(cat "$PID_FILE")"
  if is_running "$current_pid"; then
    echo "[SKIP] sample_ranking 이미 실행 중입니다. PID=$current_pid"
    exit 0
  fi
  rm -f "$PID_FILE"
fi

occupied_pid="$(listener_pid "$APP_PORT")"
if [[ -n "$occupied_pid" ]]; then
  echo "[ERROR] 포트 ${APP_PORT}를 다른 프로세스가 사용 중입니다. PID=$occupied_pid" >&2
  exit 1
fi

echo "[INFO] sample_ranking 빌드 시작"
(
  cd "$ROOT_DIR"
  ./gradlew -q bootJar
) >>"$LOG_FILE" 2>&1

JAR_FILE="$(ls -1t "$ROOT_DIR"/build/libs/*.jar 2>/dev/null | head -n 1 || true)"
if [[ -z "$JAR_FILE" ]]; then
  echo "[ERROR] 실행할 JAR 파일을 찾을 수 없습니다." >&2
  exit 1
fi

echo "[INFO] sample_ranking 실행"
(
  cd "$ROOT_DIR"
  nohup java -jar "$JAR_FILE" --server.port="$APP_PORT" >>"$LOG_FILE" 2>&1 &
  echo "$!" >"$PID_FILE"
)

wait_for_port "$APP_PORT" "$(cat "$PID_FILE")"

cat <<INFO
[DONE] sample_ranking 기동 완료
- API URL: http://localhost:${APP_PORT}
- H2 Console: http://localhost:${APP_PORT}/h2-console
  - JDBC URL: jdbc:h2:file:./runtime/data/ranking-db
  - Username: sa
  - Password: (비어 있음)

- 데모 시즌
  - season-2026-spring

- 데모 플레이어
  - player-100 / Astra
  - player-104 / Echo
  - player-106 / Glint

- 대표 확인 API
  - GET http://localhost:${APP_PORT}/api/seasons/season-2026-spring/leaderboard?limit=5
  - GET http://localhost:${APP_PORT}/api/seasons/season-2026-spring/players/player-100

- 로그 경로
  - ${LOG_FILE}
INFO
