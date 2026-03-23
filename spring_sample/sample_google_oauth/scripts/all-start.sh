#!/usr/bin/env sh
set -eu

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"
RUNTIME_DIR="$ROOT_DIR/runtime"
PID_DIR="$RUNTIME_DIR/pids"
LOG_DIR="$RUNTIME_DIR/logs"
DATA_DIR="$RUNTIME_DIR/data"
REDIS_LOG_FILE="$LOG_DIR/redis.log"
ENV_FILE="$ROOT_DIR/.env"
FRONTEND_ENV_FILE="$FRONTEND_DIR/.env.local"

load_env_defaults() {
  file_path="$1"

  if [ ! -f "$file_path" ]; then
    return 0
  fi

  while IFS='=' read -r key value; do
    case "$key" in
      ""|\#*)
        continue
        ;;
    esac

    key="$(printf '%s' "$key" | tr -d ' ')"

    case "$value" in
      \"*\")
        value="${value#\"}"
        value="${value%\"}"
        ;;
      \'*\')
        value="${value#\'}"
        value="${value%\'}"
        ;;
    esac

    eval "is_set=\${${key}+x}"
    if [ -z "${is_set:-}" ]; then
      export "$key=$value"
    fi
  done < "$file_path"
}

load_env_defaults "$ENV_FILE"
load_env_defaults "$FRONTEND_ENV_FILE"

BACKEND_PORT="${BACKEND_PORT:-8087}"
FRONTEND_PORT="${FRONTEND_PORT:-5173}"
REDIS_PORT="${REDIS_PORT:-6379}"
APP_FRONTEND_URL="${APP_FRONTEND_URL:-http://localhost:${FRONTEND_PORT}}"
VITE_API_BASE_URL="${VITE_API_BASE_URL:-http://localhost:${BACKEND_PORT}}"
APP_REDISSON_ADDRESS="${APP_REDISSON_ADDRESS:-redis://localhost:${REDIS_PORT}}"

BACKEND_PID_FILE="$PID_DIR/backend.pid"
FRONTEND_PID_FILE="$PID_DIR/frontend.pid"
BACKEND_LOG_FILE="$LOG_DIR/backend.log"
FRONTEND_LOG_FILE="$LOG_DIR/frontend.log"
BACKEND_STARTED="false"
FRONTEND_STARTED="false"

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
    nohup env \
      BACKEND_PORT="$BACKEND_PORT" \
      APP_FRONTEND_URL="$APP_FRONTEND_URL" \
      GOOGLE_CLIENT_ID="${GOOGLE_CLIENT_ID:-google-client-id}" \
      GOOGLE_CLIENT_SECRET="${GOOGLE_CLIENT_SECRET:-google-client-secret}" \
      APP_LOGIN_SUCCESS_PATH="${APP_LOGIN_SUCCESS_PATH:-/}" \
      APP_LOGIN_FAILURE_PATH="${APP_LOGIN_FAILURE_PATH:-/}" \
      APP_JWT_SECRET="${APP_JWT_SECRET:-sample-google-oauth-demo-secret-key-for-jwt-signing-please-change}" \
      APP_JWT_ACCESS_TOKEN_MINUTES="${APP_JWT_ACCESS_TOKEN_MINUTES:-30}" \
      APP_JWT_REFRESH_TOKEN_DAYS="${APP_JWT_REFRESH_TOKEN_DAYS:-7}" \
      APP_REDISSON_ADDRESS="$APP_REDISSON_ADDRESS" \
      APP_REDISSON_PASSWORD="${APP_REDISSON_PASSWORD:-}" \
      java -jar "$backend_jar" >>"$BACKEND_LOG_FILE" 2>&1 &
    echo "$!" >"$BACKEND_PID_FILE"
  )

  wait_for_port "Backend" "$BACKEND_PORT" "$(cat "$BACKEND_PID_FILE")" 45 "$BACKEND_LOG_FILE"
  BACKEND_STARTED="true"
}

start_redis() {
  echo "[INFO] Redis 실행"
  (
    cd "$ROOT_DIR"
    docker compose up -d redis
  ) >>"$REDIS_LOG_FILE" 2>&1

  i=0
  while [ "$i" -lt 20 ]; do
    if [ -n "$(listener_pid "$REDIS_PORT")" ]; then
      echo "[OK] Redis 기동 완료 (PORT: $REDIS_PORT)"
      return 0
    fi

    i=$((i + 1))
    sleep 1
  done

  echo "[ERROR] Redis 포트 오픈 대기 시간 초과 (PORT: $REDIS_PORT, 로그: $REDIS_LOG_FILE)" >&2
  return 1
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
    nohup env VITE_API_BASE_URL="$VITE_API_BASE_URL" FRONTEND_PORT="$FRONTEND_PORT" \
      npm run dev -- --host 0.0.0.0 --port "$FRONTEND_PORT" >>"$FRONTEND_LOG_FILE" 2>&1 &
    echo "$!" >"$FRONTEND_PID_FILE"
  )

  wait_for_port "Frontend" "$FRONTEND_PORT" "$(cat "$FRONTEND_PID_FILE")" 45 "$FRONTEND_LOG_FILE"
  FRONTEND_STARTED="true"
}

cleanup_on_failure() {
  (
    cd "$ROOT_DIR"
    docker compose stop redis >/dev/null 2>&1 || true
  )

  if [ "$FRONTEND_STARTED" = "true" ] && [ -f "$FRONTEND_PID_FILE" ]; then
    kill "$(cat "$FRONTEND_PID_FILE")" 2>/dev/null || true
    rm -f "$FRONTEND_PID_FILE"
  fi

  if [ "$BACKEND_STARTED" = "true" ] && [ -f "$BACKEND_PID_FILE" ]; then
    kill "$(cat "$BACKEND_PID_FILE")" 2>/dev/null || true
    rm -f "$BACKEND_PID_FILE"
  fi
}

trap 'cleanup_on_failure' INT TERM HUP

if ! start_redis; then
  cleanup_on_failure
  exit 1
fi

if ! start_backend; then
  cleanup_on_failure
  exit 1
fi

if ! start_frontend; then
  cleanup_on_failure
  exit 1
fi

trap - INT TERM HUP

cat <<INFO
[DONE] sample_google_oauth 전체 기동 완료
- Redis URL: ${APP_REDISSON_ADDRESS}
- Frontend URL: http://localhost:${FRONTEND_PORT}
- Backend URL: http://localhost:${BACKEND_PORT}
- Google Redirect URI: http://localhost:${BACKEND_PORT}/login/oauth2/code/google
- H2 Console: http://localhost:${BACKEND_PORT}/h2-console
  - JDBC URL: jdbc:h2:file:./runtime/data/google-oauth
  - Username: sa
  - Password: (비어 있음)

- 테스트용 안내
  - Google Cloud Console OAuth 테스트 사용자 계정을 사용한다.
  - 루트 .env 파일에 GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, APP_JWT_SECRET 값을 채운다.
  - 최초 로그인 후 표시 이름, 조직, 직무를 입력해 회원가입을 완료한다.
  - OAuth 승인 요청과 refresh token은 Redis에 저장된다.
  - 승인된 JavaScript 원본: http://localhost:${FRONTEND_PORT}
  - 승인된 리디렉션 URI: http://localhost:${BACKEND_PORT}/login/oauth2/code/google

- 로그 경로
  - Redis: ${REDIS_LOG_FILE}
  - Backend: ${BACKEND_LOG_FILE}
  - Frontend: ${FRONTEND_LOG_FILE}
INFO
