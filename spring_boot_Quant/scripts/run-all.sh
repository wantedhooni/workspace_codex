#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="${ROOT_DIR}/output/run-all"
BACKEND_PID_FILE="/tmp/quant-backend-mvp.pid"
FRONTEND_PID_FILE="/tmp/quant-frontend-admin.pid"
BACKEND_PORT="${BACKEND_PORT:-8088}"
FRONTEND_PORT="${FRONTEND_PORT:-5175}"

BACKEND_HEALTH_URL="${BACKEND_HEALTH_URL:-http://127.0.0.1:${BACKEND_PORT}/actuator/health}"
FRONTEND_HEALTH_URL="${FRONTEND_HEALTH_URL:-http://127.0.0.1:${FRONTEND_PORT}}"
BACKEND_PROFILE="${BACKEND_PROFILE:-local}"
FRONTEND_PROFILE="${FRONTEND_PROFILE:-local}"
FRONTEND_VITE_MODE="${FRONTEND_VITE_MODE:-development}"

mkdir -p "$LOG_DIR"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "[run-all] required command not found: $1"
    exit 1
  fi
}

is_running_by_pid_file() {
  local pid_file="$1"

  if [[ ! -f "$pid_file" ]]; then
    return 1
  fi

  local pid
  pid="$(cat "$pid_file" 2>/dev/null || true)"

  if [[ -z "$pid" ]]; then
    rm -f "$pid_file"
    return 1
  fi

  if kill -0 "$pid" >/dev/null 2>&1; then
    return 0
  fi

  rm -f "$pid_file"
  return 1
}

wait_for_url() {
  local name="$1"
  local url="$2"
  local max_retry="${3:-90}"

  for ((i = 1; i <= max_retry; i++)); do
    if curl -fsS "$url" >/dev/null 2>&1; then
      echo "[run-all] $name is ready: $url"
      return 0
    fi
    sleep 1
  done

  echo "[run-all] $name did not become ready: $url"
  return 1
}

start_backend() {
  if is_running_by_pid_file "$BACKEND_PID_FILE"; then
    echo "[run-all] backend already running (pid: $(cat "$BACKEND_PID_FILE"))"
    return 0
  fi

  echo "[run-all] starting backend"
  (
    cd "$ROOT_DIR/backend-mvp"
    SERVER_PORT="$BACKEND_PORT" SPRING_PROFILES_ACTIVE="$BACKEND_PROFILE" nohup gradle --no-daemon bootRun >"$LOG_DIR/backend.log" 2>&1 &
    echo $! >"$BACKEND_PID_FILE"
  )

  wait_for_url "backend" "$BACKEND_HEALTH_URL" 120
}

start_frontend() {
  if is_running_by_pid_file "$FRONTEND_PID_FILE"; then
    echo "[run-all] frontend already running (pid: $(cat "$FRONTEND_PID_FILE"))"
    return 0
  fi

  echo "[run-all] starting frontend"
  (
    cd "$ROOT_DIR/frontend-admin"
    if [[ ! -d node_modules ]]; then
      echo "[run-all] installing frontend dependencies"
      npm install >"$LOG_DIR/frontend-npm-install.log" 2>&1
    fi
    VITE_APP_PROFILE="$FRONTEND_PROFILE" nohup npm run dev -- --mode "$FRONTEND_VITE_MODE" --host 0.0.0.0 --port "$FRONTEND_PORT" >"$LOG_DIR/frontend.log" 2>&1 &
    echo $! >"$FRONTEND_PID_FILE"
  )

  wait_for_url "frontend" "$FRONTEND_HEALTH_URL" 120
}

main() {
  require_cmd docker
  require_cmd curl
  require_cmd gradle
  require_cmd npm

  echo "[run-all] root: $ROOT_DIR"
  echo "[run-all] backend profile: $BACKEND_PROFILE"
  echo "[run-all] frontend profile: $FRONTEND_PROFILE"
  echo "[run-all] frontend mode   : $FRONTEND_VITE_MODE"
  echo "[run-all] backend port   : $BACKEND_PORT"
  echo "[run-all] frontend port  : $FRONTEND_PORT"
  "$ROOT_DIR/scripts/infra-up.sh"

  start_backend
  start_frontend

  echo "[run-all] all services started"
  echo "[run-all] backend  : http://127.0.0.1:$BACKEND_PORT"
  echo "[run-all] frontend : http://127.0.0.1:$FRONTEND_PORT"
  echo "[run-all] logs"
  echo "  - $LOG_DIR/backend.log"
  echo "  - $LOG_DIR/frontend.log"
  echo "[run-all] pid files"
  echo "  - $BACKEND_PID_FILE"
  echo "  - $FRONTEND_PID_FILE"
}

main "$@"
