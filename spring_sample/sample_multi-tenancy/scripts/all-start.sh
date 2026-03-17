#!/usr/bin/env sh
set -eu

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"
RUNTIME_DIR="$ROOT_DIR/runtime"
PID_DIR="$RUNTIME_DIR/pids"
LOG_DIR="$RUNTIME_DIR/logs"
DATA_DIR="$RUNTIME_DIR/data"

BACKEND_PORT="${BACKEND_PORT:-8086}"
FRONTEND_PORT="${FRONTEND_PORT:-3000}"

BACKEND_PID_FILE="$PID_DIR/backend.pid"
FRONTEND_PID_FILE="$PID_DIR/frontend.pid"
BACKEND_LOG_FILE="$LOG_DIR/backend.log"
FRONTEND_LOG_FILE="$LOG_DIR/frontend.log"

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

  i=0
  while [ "$i" -lt "$retries" ]; do
    if [ -n "$(listener_pid "$port")" ]; then
      echo "[OK] $name 기동 완료 (PORT: $port)"
      return 0
    fi

    if ! is_pid_running "$pid"; then
      echo "[ERROR] $name 프로세스가 비정상 종료되었습니다. 로그: $5" >&2
      return 1
    fi

    i=$((i + 1))
    sleep 1
  done

  echo "[ERROR] $name 포트 오픈 대기 시간 초과 (PORT: $port, 로그: $5)" >&2
  return 1
}

ensure_available_port() {
  name="$1"
  port="$2"
  pid_file="$3"

  if [ -f "$pid_file" ]; then
    current_pid="$(cat "$pid_file")"
    if is_pid_running "$current_pid"; then
      echo "[SKIP] $name 이미 실행 중 (PID: $current_pid)"
      return 1
    fi
    rm -f "$pid_file"
  fi

  occupied_pid="$(listener_pid "$port")"
  if [ -n "$occupied_pid" ]; then
    echo "[ERROR] $name 포트를 다른 프로세스가 사용 중입니다. (PORT: $port, PID: $occupied_pid)" >&2
    exit 1
  fi

  return 0
}

start_backend() {
  ensure_available_port "Backend" "$BACKEND_PORT" "$BACKEND_PID_FILE" || return 0

  echo "[INFO] Backend build 시작"
  (
    cd "$BACKEND_DIR"
    ./gradlew -q bootJar
  ) >>"$BACKEND_LOG_FILE" 2>&1

  backend_jar="$(ls -1t "$BACKEND_DIR"/build/libs/*.jar 2>/dev/null | head -n 1 || true)"
  if [ -z "$backend_jar" ]; then
    echo "[ERROR] Backend 실행 JAR를 찾을 수 없습니다." >&2
    exit 1
  fi

  echo "[INFO] Backend 실행"
  (
    cd "$ROOT_DIR"
    nohup java -jar "$backend_jar" >>"$BACKEND_LOG_FILE" 2>&1 &
    echo "$!" >"$BACKEND_PID_FILE"
  )

  wait_for_port "Backend" "$BACKEND_PORT" "$(cat "$BACKEND_PID_FILE")" 30 "$BACKEND_LOG_FILE"
}

start_frontend() {
  ensure_available_port "Frontend" "$FRONTEND_PORT" "$FRONTEND_PID_FILE" || return 0

  if [ ! -d "$FRONTEND_DIR/node_modules" ]; then
    echo "[INFO] Frontend 의존성 설치"
    (
      cd "$FRONTEND_DIR"
      npm install
    ) >>"$FRONTEND_LOG_FILE" 2>&1
  fi

  echo "[INFO] Frontend 실행"
  (
    cd "$FRONTEND_DIR"
    nohup npm run dev -- --hostname 0.0.0.0 --port "$FRONTEND_PORT" >>"$FRONTEND_LOG_FILE" 2>&1 &
    echo "$!" >"$FRONTEND_PID_FILE"
  )

  wait_for_port "Frontend" "$FRONTEND_PORT" "$(cat "$FRONTEND_PID_FILE")" 30 "$FRONTEND_LOG_FILE"
}

start_backend
start_frontend

cat <<INFO
[DONE] smaple_multi-tenancy 전체 기동 완료
- Backend URL: http://localhost:${BACKEND_PORT}
- Frontend URL: http://localhost:${FRONTEND_PORT}
- H2 Console: http://localhost:${BACKEND_PORT}/h2-console
  - JDBC URL: jdbc:h2:file:./runtime/data/multitenancy
  - Username: sa
  - Password: (비어 있음)

- 데모 계정
  - alpha / alpha.admin / demo1234
  - alpha / alpha.viewer / demo1234
  - beta / beta.admin / demo1234
  - beta / beta.viewer / demo1234

- 로그 경로
  - Backend: ${BACKEND_LOG_FILE}
  - Frontend: ${FRONTEND_LOG_FILE}
INFO
