#!/usr/bin/env sh
set -eu

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"
COMPOSE_FILE="$ROOT_DIR/docker-compose.yml"
RUNTIME_DIR="$ROOT_DIR/runtime"
PID_DIR="$RUNTIME_DIR/pids"
LOG_DIR="$RUNTIME_DIR/logs"

BACKEND_PID_FILE="$PID_DIR/backend.pid"
FRONTEND_PID_FILE="$PID_DIR/frontend.pid"
OLLAMA_LOG_FILE="$LOG_DIR/ollama.log"
BACKEND_LOG_FILE="$LOG_DIR/backend.log"
FRONTEND_LOG_FILE="$LOG_DIR/frontend.log"

if [ -f "$ENV_FILE" ]; then
  set -a
  # shellcheck disable=SC1090
  . "$ENV_FILE"
  set +a
fi

OLLAMA_PORT="${OLLAMA_PORT:-11434}"
OLLAMA_MODEL="${OLLAMA_MODEL:-llama3.2}"
OLLAMA_BASE_URL="${OLLAMA_BASE_URL:-http://localhost:${OLLAMA_PORT}}"
OLLAMA_WARMUP="${OLLAMA_WARMUP:-true}"
OLLAMA_WARMUP_PROMPT="${OLLAMA_WARMUP_PROMPT:-안녕}"
BACKEND_PORT="${BACKEND_PORT:-8088}"
FRONTEND_PORT="${FRONTEND_PORT:-5173}"
FRONTEND_ORIGIN="${FRONTEND_ORIGIN:-http://localhost:${FRONTEND_PORT}}"
VITE_API_BASE_URL="${VITE_API_BASE_URL:-http://localhost:${BACKEND_PORT}}"
VITE_WS_URL="${VITE_WS_URL:-ws://localhost:${BACKEND_PORT}/ws/chat}"

export OLLAMA_PORT
export OLLAMA_MODEL
export OLLAMA_BASE_URL
export OLLAMA_WARMUP
export OLLAMA_WARMUP_PROMPT
export BACKEND_PORT
export FRONTEND_PORT
export FRONTEND_ORIGIN
export VITE_API_BASE_URL
export VITE_WS_URL

BACKEND_HEALTH_URL="http://localhost:${BACKEND_PORT}/actuator/health"
FRONTEND_URL="http://localhost:${FRONTEND_PORT}"
OLLAMA_TAGS_URL="${OLLAMA_BASE_URL%/}/api/tags"

mkdir -p "$PID_DIR" "$LOG_DIR"

if ! command -v curl >/dev/null 2>&1; then
  echo "[ERROR] curl 명령이 필요합니다." >&2
  exit 1
fi

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

docker_compose() {
  if [ -f "$ENV_FILE" ]; then
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"
  else
    docker compose -f "$COMPOSE_FILE" "$@"
  fi
}

is_pid_running() {
  pid="$1"
  if [ -z "$pid" ]; then
    return 1
  fi
  kill -0 "$pid" 2>/dev/null
}

is_http_ready() {
  url="$1"
  curl -fsS "$url" >/dev/null 2>&1
}

wait_for_http() {
  name="$1"
  url="$2"
  retries="$3"

  i=0
  while [ "$i" -lt "$retries" ]; do
    if is_http_ready "$url"; then
      return 0
    fi
    i=$((i + 1))
    sleep 1
  done

  echo "[ERROR] $name 준비 실패: $url" >&2
  return 1
}

is_pid_in_tree() {
  candidate_pid="$1"
  root_pid="$2"

  current_pid="$candidate_pid"
  while [ -n "$current_pid" ] && [ "$current_pid" != "0" ]; do
    if [ "$current_pid" = "$root_pid" ]; then
      return 0
    fi

    if ! ps -p "$current_pid" >/dev/null 2>&1; then
      return 1
    fi

    parent_pid="$(ps -o ppid= -p "$current_pid" 2>/dev/null | tr -d ' ')"
    if [ -z "$parent_pid" ] || [ "$parent_pid" = "$current_pid" ]; then
      return 1
    fi
    current_pid="$parent_pid"
  done

  return 1
}

is_port_owned_by_process_tree() {
  root_pid="$1"
  port="$2"

  if ! command -v lsof >/dev/null 2>&1; then
    return 1
  fi

  listener_pids="$(lsof -nP -iTCP:"$port" -sTCP:LISTEN -t 2>/dev/null | sort -u || true)"
  if [ -z "$listener_pids" ]; then
    return 1
  fi

  for listener_pid in $listener_pids; do
    if is_pid_in_tree "$listener_pid" "$root_pid"; then
      return 0
    fi
  done

  return 1
}

format_port_owners() {
  port="$1"
  owners="$(lsof -nP -iTCP:"$port" -sTCP:LISTEN -t 2>/dev/null | sort -u || true)"
  if [ -z "$owners" ]; then
    echo "none"
    return 0
  fi

  for owner_pid in $owners; do
    owner_cmd="$(ps -o command= -p "$owner_pid" 2>/dev/null || true)"
    echo "PID=$owner_pid CMD=$owner_cmd"
  done
}

ensure_frontend_port_available() {
  port="$1"
  listener_pids="$(lsof -nP -iTCP:"$port" -sTCP:LISTEN -t 2>/dev/null | sort -u || true)"
  if [ -z "$listener_pids" ]; then
    return 0
  fi

  for listener_pid in $listener_pids; do
    listener_cmd="$(ps -o command= -p "$listener_pid" 2>/dev/null || true)"
    case "$listener_cmd" in
      *"$ROOT_DIR/frontend"*)
        echo "[WARN] Frontend 포트($port)를 점유한 stale 프로세스를 종료합니다. (PID: $listener_pid)"
        kill "$listener_pid" 2>/dev/null || true
        sleep 1
        if ps -p "$listener_pid" >/dev/null 2>&1; then
          kill -9 "$listener_pid" 2>/dev/null || true
        fi
        ;;
      *)
        echo "[ERROR] Frontend 포트($port)가 다른 프로세스에서 사용 중입니다." >&2
        echo "        PID=$listener_pid CMD=$listener_cmd" >&2
        echo "        포트를 비우거나 .env에서 FRONTEND_PORT를 변경하세요." >&2
        return 1
        ;;
    esac
  done

  remaining="$(lsof -nP -iTCP:"$port" -sTCP:LISTEN -t 2>/dev/null | sort -u || true)"
  if [ -n "$remaining" ]; then
    echo "[ERROR] Frontend 포트($port)를 비우지 못했습니다." >&2
    format_port_owners "$port" >&2
    return 1
  fi

  return 0
}

ensure_backend_port_available() {
  port="$1"
  listener_pids="$(lsof -nP -iTCP:"$port" -sTCP:LISTEN -t 2>/dev/null | sort -u || true)"
  if [ -z "$listener_pids" ]; then
    return 0
  fi

  for listener_pid in $listener_pids; do
    listener_cmd="$(ps -o command= -p "$listener_pid" 2>/dev/null || true)"
    case "$listener_cmd" in
      *"$ROOT_DIR/backend"*)
        echo "[WARN] Backend 포트($port)를 점유한 stale 프로세스를 종료합니다. (PID: $listener_pid)"
        kill "$listener_pid" 2>/dev/null || true
        sleep 1
        if ps -p "$listener_pid" >/dev/null 2>&1; then
          kill -9 "$listener_pid" 2>/dev/null || true
        fi
        ;;
      *)
        echo "[ERROR] Backend 포트($port)가 다른 프로세스에서 사용 중입니다." >&2
        echo "        PID=$listener_pid CMD=$listener_cmd" >&2
        echo "        포트를 비우거나 .env에서 BACKEND_PORT를 변경하세요." >&2
        return 1
        ;;
    esac
  done

  remaining="$(lsof -nP -iTCP:"$port" -sTCP:LISTEN -t 2>/dev/null | sort -u || true)"
  if [ -n "$remaining" ]; then
    echo "[ERROR] Backend 포트($port)를 비우지 못했습니다." >&2
    format_port_owners "$port" >&2
    return 1
  fi

  return 0
}

wait_for_service_with_port() {
  name="$1"
  url="$2"
  pid_file="$3"
  port="$4"
  retries="$5"

  stable_ready=0
  i=0
  while [ "$i" -lt "$retries" ]; do
    if [ ! -f "$pid_file" ]; then
      echo "[ERROR] $name PID 파일이 없습니다: $pid_file" >&2
      return 1
    fi

    pid="$(cat "$pid_file")"
    if ! is_pid_running "$pid"; then
      echo "[ERROR] $name 프로세스가 비정상 종료되었습니다. (PID: $pid)" >&2
      echo "[ERROR] 포트 점유 상태: $(format_port_owners "$port" | tr '\n' ' ')" >&2
      return 1
    fi

    if is_http_ready "$url" && is_port_owned_by_process_tree "$pid" "$port"; then
      stable_ready=$((stable_ready + 1))
      if [ "$stable_ready" -ge 2 ]; then
        return 0
      fi
    else
      stable_ready=0
    fi

    i=$((i + 1))
    sleep 1
  done

  echo "[ERROR] $name 준비 실패: $url (port: $port)" >&2
  return 1
}

wait_for_backend_service() {
  name="$1"
  url="$2"
  pid_file="$3"
  port="$4"
  retries="$5"

  i=0
  while [ "$i" -lt "$retries" ]; do
    if is_http_ready "$url"; then
      listener_pid="$(lsof -nP -iTCP:"$port" -sTCP:LISTEN -t 2>/dev/null | head -n 1 || true)"
      if [ -n "$listener_pid" ]; then
        echo "$listener_pid" > "$pid_file"
      fi
      return 0
    fi
    i=$((i + 1))
    sleep 1
  done

  echo "[ERROR] $name 준비 실패: $url (port: $port)" >&2
  return 1
}

stop_pid_if_running() {
  pid_file="$1"
  if [ ! -f "$pid_file" ]; then
    return 0
  fi

  pid="$(cat "$pid_file")"
  if ! is_pid_running "$pid"; then
    rm -f "$pid_file"
    return 0
  fi

  kill "$pid" 2>/dev/null || true
  retries=0
  while [ "$retries" -lt 10 ]; do
    if ! is_pid_running "$pid"; then
      rm -f "$pid_file"
      return 0
    fi
    retries=$((retries + 1))
    sleep 1
  done

  kill -9 "$pid" 2>/dev/null || true
  rm -f "$pid_file"
}

start_process() {
  name="$1"
  pid_file="$2"
  log_file="$3"
  shift 3

  if [ -f "$pid_file" ]; then
    pid="$(cat "$pid_file")"
    if is_pid_running "$pid"; then
      echo "[SKIP] $name 이미 실행 중 (PID: $pid)"
      return 0
    fi
    rm -f "$pid_file"
  fi

  echo "[INFO] $name 시작"
  nohup "$@" >>"$log_file" 2>&1 &
  pid="$!"
  echo "$pid" > "$pid_file"
  echo "[INFO] $name PID: $pid"
}

ensure_backend_service() {
  name="$1"
  pid_file="$2"
  log_file="$3"
  health_url="$4"
  port="$5"
  wait_retries="$6"
  shift 6

  if [ -f "$pid_file" ]; then
    pid="$(cat "$pid_file")"
    if is_pid_running "$pid"; then
      if is_http_ready "$health_url"; then
        echo "[SKIP] $name 이미 실행 중 (PID: $pid, URL: $health_url, PORT: $port)"
        return 0
      fi
      echo "[WARN] $name PID는 살아있지만 헬스체크가 실패합니다. 재시작합니다. (PID: $pid)"
      stop_pid_if_running "$pid_file"
    else
      rm -f "$pid_file"
    fi
  fi

  ensure_backend_port_available "$port"
  start_process "$name" "$pid_file" "$log_file" "$@"
  wait_for_backend_service "$name" "$health_url" "$pid_file" "$port" "$wait_retries"
}

ensure_frontend_service() {
  name="$1"
  pid_file="$2"
  log_file="$3"
  health_url="$4"
  port="$5"
  wait_retries="$6"
  shift 6

  if [ -f "$pid_file" ]; then
    pid="$(cat "$pid_file")"
    if is_pid_running "$pid"; then
      if is_http_ready "$health_url" && is_port_owned_by_process_tree "$pid" "$port"; then
        echo "[SKIP] $name 이미 실행 중 (PID: $pid, URL: $health_url, PORT: $port)"
        return 0
      fi
      echo "[WARN] $name PID는 살아있지만 응답/포트 상태가 비정상입니다. 재시작합니다. (PID: $pid)"
      stop_pid_if_running "$pid_file"
    else
      rm -f "$pid_file"
    fi
  fi

  ensure_frontend_port_available "$port"
  start_process "$name" "$pid_file" "$log_file" "$@"
  wait_for_service_with_port "$name" "$health_url" "$pid_file" "$port" "$wait_retries"
}

echo "[INFO] Ollama 컨테이너 시작"
docker_compose up -d ollama >>"$OLLAMA_LOG_FILE" 2>&1
wait_for_http "Ollama" "$OLLAMA_TAGS_URL" 60

echo "[INFO] Ollama 모델 준비: $OLLAMA_MODEL"
docker_compose exec -T ollama ollama pull "$OLLAMA_MODEL" >>"$OLLAMA_LOG_FILE" 2>&1

if [ "$OLLAMA_WARMUP" = "true" ]; then
  echo "[INFO] Ollama 모델 워밍업 실행"
  docker_compose exec -T ollama ollama run "$OLLAMA_MODEL" "$OLLAMA_WARMUP_PROMPT" >>"$OLLAMA_LOG_FILE" 2>&1 || true
fi

ensure_backend_service "Backend" "$BACKEND_PID_FILE" "$BACKEND_LOG_FILE" "$BACKEND_HEALTH_URL" "$BACKEND_PORT" 120 \
  sh -c "cd '$ROOT_DIR/backend' && ./gradlew bootJar && BACKEND_JAR=\$(ls build/libs/*SNAPSHOT.jar | grep -v -- '-plain.jar' | head -n 1) && exec java -jar \"\$BACKEND_JAR\""

echo "[INFO] Frontend 의존성 설치 확인"
if [ ! -d "$ROOT_DIR/frontend/node_modules" ]; then
  (cd "$ROOT_DIR/frontend" && npm install)
fi

ensure_frontend_service "Frontend" "$FRONTEND_PID_FILE" "$FRONTEND_LOG_FILE" "$FRONTEND_URL" "$FRONTEND_PORT" 45 \
  sh -c "cd '$ROOT_DIR/frontend' && npm run dev -- --host 0.0.0.0 --port '${FRONTEND_PORT}' --strictPort"

echo "[DONE] 전체 서비스 시작 완료"
echo "- Frontend: $FRONTEND_URL"
echo "- Backend : http://localhost:${BACKEND_PORT}"
echo "- Ollama  : ${OLLAMA_BASE_URL}"
echo "- Model   : ${OLLAMA_MODEL}"
echo "- Logs    : $LOG_DIR"
