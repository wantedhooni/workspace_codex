#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.run"
mkdir -p "$RUN_DIR"

is_running() {
  local pid="$1"
  kill -0 "$pid" >/dev/null 2>&1
}

start_with_pidfile() {
  local name="$1"
  local workdir="$2"
  local cmd="$3"
  local pid_file="$RUN_DIR/${name}.pid"
  local log_file="$RUN_DIR/${name}.log"

  if [[ -f "$pid_file" ]]; then
    local pid
    pid="$(cat "$pid_file")"
    if [[ -n "$pid" ]] && is_running "$pid"; then
      echo "[$name] already running (pid=$pid)"
      return
    fi
  fi

  echo "[$name] starting..."
  (
    cd "$workdir"
    nohup bash -lc "$cmd" >"$log_file" 2>&1 &
    echo "$!" >"$pid_file"
  )
  local pid
  pid="$(cat "$pid_file")"
  echo "$pid" >"$pid_file"
  echo "[$name] started (pid=$pid, log=$log_file)"
}

wait_for_port() {
  local port="$1"
  local name="$2"
  local max_retry=90

  for ((i=1; i<=max_retry; i++)); do
    if nc -z localhost "$port" >/dev/null 2>&1; then
      echo "[$name] ready on port $port"
      return 0
    fi
    sleep 1
  done

  echo "[$name] failed to become ready on port $port"
  return 1
}

assert_port_free() {
  local port="$1"
  local name="$2"
  if lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "[$name] port $port is already in use:"
    lsof -nP -iTCP:"$port" -sTCP:LISTEN
    return 1
  fi
}

echo "[db] starting postgres..."
docker compose --project-name derivops_mvp -f "$ROOT_DIR/docker-compose.yml" up -d postgres >/dev/null

assert_port_free 8080 "backend"
assert_port_free 5173 "frontend"

echo "[backend/frontend] starting services..."
start_with_pidfile "backend" "$ROOT_DIR/backend" "mvn spring-boot:run"
start_with_pidfile "frontend" "$ROOT_DIR/frontend" "if [[ ! -d node_modules ]]; then npm install; fi && npm run dev -- --host 0.0.0.0 --port 5173 --strictPort"

wait_for_port 8080 "backend"
wait_for_port 5173 "frontend"

echo "All services started."
echo "- Backend:  http://localhost:8080"
echo "- Frontend: http://localhost:5173"
echo "Demo login: opsadmin / admin123!"
