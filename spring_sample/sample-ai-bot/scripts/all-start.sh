#!/usr/bin/env sh
set -eu

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"
COMPOSE_FILE="$ROOT_DIR/docker-compose.yml"
RUNTIME_DIR="$ROOT_DIR/runtime"
PID_DIR="$RUNTIME_DIR/pids"
LOG_DIR="$RUNTIME_DIR/logs"

BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"

BACKEND_PID_FILE="$PID_DIR/backend.pid"
FRONTEND_PID_FILE="$PID_DIR/frontend.pid"
BACKEND_LOG_FILE="$LOG_DIR/backend.log"
FRONTEND_LOG_FILE="$LOG_DIR/frontend.log"

BACKEND_PORT="${BACKEND_PORT:-8088}"
FRONTEND_PORT="${FRONTEND_PORT:-5173}"

if ! command -v docker >/dev/null 2>&1; then
  echo "[ERROR] docker 명령을 찾을 수 없습니다." >&2
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "[ERROR] docker compose를 사용할 수 없습니다." >&2
  exit 1
fi

if [ ! -f "$COMPOSE_FILE" ]; then
  echo "[ERROR] docker-compose 파일이 없습니다: $COMPOSE_FILE" >&2
  exit 1
fi

if [ -f "$ENV_FILE" ]; then
  set -a
  # shellcheck disable=SC1090
  . "$ENV_FILE"
  set +a
fi

mkdir -p "$PID_DIR" "$LOG_DIR"

is_pid_running() {
  pid="$1"
  if [ -z "$pid" ]; then
    return 1
  fi
  kill -0 "$pid" 2>/dev/null
}

listener_pid() {
  port="$1"
  lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null | head -n 1 || true
}

process_command() {
  pid="$1"
  ps -p "$pid" -o command= 2>/dev/null || true
}

ensure_stale_pid_removed() {
  pid_file="$1"
  if [ ! -f "$pid_file" ]; then
    return 0
  fi

  pid="$(cat "$pid_file")"
  if is_pid_running "$pid"; then
    return 0
  fi

  rm -f "$pid_file"
}

wait_for_port() {
  name="$1"
  port="$2"
  pid="$3"
  retries="$4"

  i=0
  while [ "$i" -lt "$retries" ]; do
    listening_pid="$(listener_pid "$port")"
    if [ -n "$listening_pid" ]; then
      echo "[OK] $name 포트 확인 완료 (PORT: $port, PID: $listening_pid)"
      return 0
    fi

    if ! is_pid_running "$pid"; then
      echo "[ERROR] $name 프로세스가 비정상 종료되었습니다. (PID: $pid)" >&2
      return 1
    fi

    i=$((i + 1))
    sleep 1
  done

  echo "[ERROR] $name 포트 오픈 대기 시간 초과 (PORT: $port)" >&2
  return 1
}

prepare_frontend_dependencies() {
  if [ ! -d "$FRONTEND_DIR/node_modules" ]; then
    echo "[INFO] Frontend 의존성 설치"
    (
      cd "$FRONTEND_DIR"
      npm install
    )
  fi
}

start_backend() {
  ensure_stale_pid_removed "$BACKEND_PID_FILE"

  if [ -f "$BACKEND_PID_FILE" ]; then
    current_pid="$(cat "$BACKEND_PID_FILE")"
    echo "[SKIP] Backend 이미 실행 중 (PID: $current_pid)"
    return 0
  fi

  occupied_pid="$(listener_pid "$BACKEND_PORT")"
  if [ -n "$occupied_pid" ]; then
    occupied_cmd="$(process_command "$occupied_pid")"
    if printf '%s' "$occupied_cmd" | grep -q "sample-ai-bot-backend"; then
      echo "$occupied_pid" >"$BACKEND_PID_FILE"
      echo "[SKIP] Backend 이미 포트 사용 중 (PORT: $BACKEND_PORT, PID: $occupied_pid)"
      return 0
    fi
    echo "[ERROR] Backend 포트 충돌 (PORT: $BACKEND_PORT, PID: $occupied_pid)" >&2
    echo "[ERROR] 점유 프로세스: $occupied_cmd" >&2
    exit 1
  fi

  if ! command -v java >/dev/null 2>&1; then
    echo "[ERROR] java 명령을 찾을 수 없습니다." >&2
    exit 1
  fi

  if [ ! -f "$BACKEND_DIR/gradlew" ]; then
    echo "[ERROR] backend gradlew 파일이 없습니다: $BACKEND_DIR/gradlew" >&2
    exit 1
  fi

  echo "[INFO] Backend 빌드 (bootJar)"
  (
    cd "$BACKEND_DIR"
    ./gradlew -q bootJar
  ) >>"$BACKEND_LOG_FILE" 2>&1

  backend_jar="$(ls -1t "$BACKEND_DIR"/build/libs/*.jar 2>/dev/null | head -n 1 || true)"
  if [ -z "$backend_jar" ]; then
    echo "[ERROR] Backend 실행 JAR를 찾을 수 없습니다." >&2
    exit 1
  fi

  echo "[INFO] Backend 시작"
  nohup java -jar "$backend_jar" >>"$BACKEND_LOG_FILE" 2>&1 &
  backend_pid="$!"
  echo "$backend_pid" >"$BACKEND_PID_FILE"

  wait_for_port "Backend" "$BACKEND_PORT" "$backend_pid" 30 || {
    rm -f "$BACKEND_PID_FILE"
    echo "[ERROR] 로그 확인: $BACKEND_LOG_FILE" >&2
    exit 1
  }
}

start_frontend() {
  ensure_stale_pid_removed "$FRONTEND_PID_FILE"

  if [ -f "$FRONTEND_PID_FILE" ]; then
    current_pid="$(cat "$FRONTEND_PID_FILE")"
    echo "[SKIP] Frontend 이미 실행 중 (PID: $current_pid)"
    return 0
  fi

  occupied_pid="$(listener_pid "$FRONTEND_PORT")"
  if [ -n "$occupied_pid" ]; then
    occupied_cmd="$(process_command "$occupied_pid")"
    if printf '%s' "$occupied_cmd" | grep -q "$FRONTEND_DIR"; then
      echo "$occupied_pid" >"$FRONTEND_PID_FILE"
      echo "[SKIP] Frontend 이미 포트 사용 중 (PORT: $FRONTEND_PORT, PID: $occupied_pid)"
      return 0
    fi
    echo "[ERROR] Frontend 포트 충돌 (PORT: $FRONTEND_PORT, PID: $occupied_pid)" >&2
    echo "[ERROR] 점유 프로세스: $occupied_cmd" >&2
    exit 1
  fi

  if ! command -v npm >/dev/null 2>&1; then
    echo "[ERROR] npm 명령을 찾을 수 없습니다." >&2
    exit 1
  fi

  prepare_frontend_dependencies

  echo "[INFO] Frontend 시작"
  (
    cd "$FRONTEND_DIR"
    nohup npm run dev -- --host 0.0.0.0 --port "$FRONTEND_PORT" >>"$FRONTEND_LOG_FILE" 2>&1 &
    echo "$!" >"$FRONTEND_PID_FILE"
  )
  frontend_pid="$(cat "$FRONTEND_PID_FILE")"

  wait_for_port "Frontend" "$FRONTEND_PORT" "$frontend_pid" 20 || {
    rm -f "$FRONTEND_PID_FILE"
    echo "[ERROR] 로그 확인: $FRONTEND_LOG_FILE" >&2
    exit 1
  }
}

if [ -f "$ENV_FILE" ]; then
  echo "[INFO] docker compose --env-file .env up -d"
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d
else
  echo "[INFO] docker compose up -d"
  docker compose -f "$COMPOSE_FILE" up -d
fi

start_backend
start_frontend

echo "[INFO] docker compose 상태 확인"
if [ -f "$ENV_FILE" ]; then
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps
else
  docker compose -f "$COMPOSE_FILE" ps
fi

echo "[INFO] Backend URL:  http://localhost:${BACKEND_PORT}"
echo "[INFO] Frontend URL: http://localhost:${FRONTEND_PORT}"
echo "[INFO] Backend log:  $BACKEND_LOG_FILE"
echo "[INFO] Frontend log: $FRONTEND_LOG_FILE"
echo "[DONE] 전체 기동 완료 (Ollama + Backend + Frontend)"
