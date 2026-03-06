#!/usr/bin/env sh
set -eu

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"
COMPOSE_FILE="$ROOT_DIR/docker-compose.yml"
PID_DIR="$ROOT_DIR/runtime/pids"

BACKEND_PID_FILE="$PID_DIR/backend.pid"
FRONTEND_PID_FILE="$PID_DIR/frontend.pid"

if [ -f "$ENV_FILE" ]; then
  set -a
  # shellcheck disable=SC1090
  . "$ENV_FILE"
  set +a
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

stop_by_pid_file() {
  name="$1"
  pid_file="$2"

  if [ ! -f "$pid_file" ]; then
    echo "[SKIP] $name PID 파일 없음"
    return 0
  fi

  pid="$(cat "$pid_file")"
  if ! is_pid_running "$pid"; then
    echo "[SKIP] $name 이미 중지됨 (PID: $pid)"
    rm -f "$pid_file"
    return 0
  fi

  echo "[INFO] $name 중지 (PID: $pid)"
  kill "$pid" 2>/dev/null || true

  retries=0
  while [ "$retries" -lt 10 ]; do
    if ! is_pid_running "$pid"; then
      rm -f "$pid_file"
      echo "[OK] $name 중지 완료"
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

if command -v docker >/dev/null 2>&1 && [ -f "$COMPOSE_FILE" ]; then
  if docker compose version >/dev/null 2>&1; then
    ollama_id="$(docker_compose ps -q ollama 2>/dev/null || true)"
    if [ -n "$ollama_id" ]; then
      echo "[INFO] Ollama 컨테이너 중지"
      docker_compose stop ollama >/dev/null 2>&1 || true
      echo "[OK] Ollama 컨테이너 중지 완료"
    else
      echo "[SKIP] Ollama 컨테이너 없음"
    fi
  else
    echo "[SKIP] docker compose 사용 불가"
  fi
else
  echo "[SKIP] docker/compose 파일 없음"
fi

echo "[DONE] 종료 스크립트 실행 완료"
