#!/usr/bin/env sh
set -eu

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
RUNTIME_DIR="$ROOT_DIR/runtime"
PID_DIR="$RUNTIME_DIR/pids"
LOG_DIR="$RUNTIME_DIR/logs"
DATA_DIR="$RUNTIME_DIR/data"
APP_PID_FILE="$PID_DIR/app.pid"
APP_LOG_FILE="$LOG_DIR/app.log"
APP_PORT="${APP_PORT:-8089}"

mkdir -p "$PID_DIR" "$LOG_DIR" "$DATA_DIR"

is_pid_running() {
  pid="$1"
  [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null
}

listener_pid() {
  port="$1"
  lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null | head -n 1 || true
}

wait_for_port() {
  name="$1"
  port="$2"
  pid="$3"
  retries="$4"
  log_file="$5"

  i=0
  while [ "$i" -lt "$retries" ]; do
    if [ -n "$(listener_pid "$port")" ]; then
      echo "[OK] $name 기동 완료 (PORT: $port)"
      return 0
    fi

    if ! is_pid_running "$pid"; then
      echo "[ERROR] $name 프로세스가 비정상 종료되었습니다. 로그: $log_file" >&2
      return 1
    fi

    i=$((i + 1))
    sleep 1
  done

  echo "[ERROR] $name 포트 오픈 대기 시간 초과 (PORT: $port, 로그: $log_file)" >&2
  return 1
}

if [ -f "$APP_PID_FILE" ]; then
  current_pid="$(cat "$APP_PID_FILE")"
  if is_pid_running "$current_pid"; then
    echo "[SKIP] 애플리케이션 이미 실행 중 (PID: $current_pid)"
  else
    rm -f "$APP_PID_FILE"
  fi
fi

occupied_pid="$(listener_pid "$APP_PORT")"
if [ -n "$occupied_pid" ] && [ ! -f "$APP_PID_FILE" ]; then
  echo "[ERROR] 애플리케이션 포트를 다른 프로세스가 사용 중입니다. (PORT: $APP_PORT, PID: $occupied_pid)" >&2
  exit 1
fi

echo "[INFO] Prometheus / Grafana 기동"
(
  cd "$ROOT_DIR"
  docker compose up -d
)

if [ ! -f "$APP_PID_FILE" ]; then
  echo "[INFO] 애플리케이션 빌드"
  (
    cd "$ROOT_DIR"
    ./gradlew -q bootJar
  ) >>"$APP_LOG_FILE" 2>&1

  app_jar="$(ls -1t "$ROOT_DIR"/build/libs/*.jar 2>/dev/null | head -n 1 || true)"
  if [ -z "$app_jar" ]; then
    echo "[ERROR] 실행 JAR를 찾을 수 없습니다." >&2
    exit 1
  fi

  echo "[INFO] 애플리케이션 실행"
  (
    cd "$ROOT_DIR"
    nohup java -jar "$app_jar" >>"$APP_LOG_FILE" 2>&1 &
    echo "$!" >"$APP_PID_FILE"
  )

  wait_for_port "애플리케이션" "$APP_PORT" "$(cat "$APP_PID_FILE")" 30 "$APP_LOG_FILE"
fi

cat <<INFO
[DONE] sample_Observability 전체 기동 완료
- 애플리케이션 URL: http://localhost:${APP_PORT}
- H2 Console: http://localhost:${APP_PORT}/h2-console
  - JDBC URL: jdbc:h2:file:./runtime/data/observability
  - Username: sa
  - Password: (비어 있음)
- Prometheus: http://localhost:9099
- Grafana: http://localhost:3010
  - 계정: admin / admin

- 데모 계정
  - ops / ops.admin / demo1234
  - ops / ops.viewer / demo1234
  - biz / biz.admin / demo1234
  - biz / biz.viewer / demo1234

- 로그 경로
  - 애플리케이션: ${APP_LOG_FILE}
INFO
