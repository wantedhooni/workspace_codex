#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="$ROOT_DIR/logs"
PID_DIR="$ROOT_DIR/pids"

mkdir -p "$LOG_DIR" "$PID_DIR"

BACKEND_SERVICES=(
  "discovery-service|8761|/actuator/health"
  "config-server|8888|/actuator/health"
  "api-gateway|8000|/actuator/health"
  "auth-server|9000|/actuator/health"
  "user-service|8081|/actuator/health"
  "order-service|8082|/actuator/health"
)

FRONTEND_APPS=(
  "admin-portal|3001|/dashboard|frontend/apps/admin-portal"
  "web-application|3002|/|frontend/apps/web-application"
)

build_if_needed() {
  echo "[all-start] building backend and frontend artifacts"
  (cd "$ROOT_DIR" && ./scripts/build.sh)
}

jar_path() {
  local service="$1"
  find "$ROOT_DIR/services/$service/build/libs" -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | sort | head -n 1
}

is_port_open() {
  local port="$1"
  lsof -tiTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1
}

wait_for_http_200() {
  local name="$1"
  local url="$2"

  for _ in {1..60}; do
    if curl -fsSL "$url" >/dev/null 2>&1; then
      echo "[all-start] ${name} is reachable"
      return 0
    fi
    sleep 2
  done

  echo "[all-start] ${name} did not become reachable: ${url}" >&2
  return 1
}

wait_for_health() {
  local service="$1"
  local port="$2"
  local path="$3"
  wait_for_http_200 "$service" "http://127.0.0.1:${port}${path}"
}

start_backend_service() {
  local service="$1"
  local port="$2"
  local health_path="$3"
  local pid_file="$PID_DIR/${service}.pid"
  local log_file="$LOG_DIR/${service}.log"
  local jar

  if [[ -f "$pid_file" ]]; then
    local existing_pid
    existing_pid="$(cat "$pid_file")"
    if kill -0 "$existing_pid" >/dev/null 2>&1; then
      echo "[all-start] ${service} already running pid=${existing_pid}"
      return 0
    fi
    rm -f "$pid_file"
  fi

  if is_port_open "$port"; then
    echo "[all-start] port ${port} already in use" >&2
    return 1
  fi

  jar="$(jar_path "$service")"
  if [[ -z "$jar" ]]; then
    echo "[all-start] boot jar not found for ${service}" >&2
    return 1
  fi

  local -a env_args=()
  env_args+=("EUREKA_SERVER_URL=http://127.0.0.1:8761/eureka")
  env_args+=("CONFIG_SERVER_URL=http://127.0.0.1:8888/config")
  env_args+=("CONFIG_REPO_PATH=file:${ROOT_DIR}/config-repo")

  case "$service" in
    config-server)
      env_args+=("CONFIG_SERVER_VAULT_TOKEN=${CONFIG_SERVER_VAULT_TOKEN:-dev-root-token}")
      ;;
    api-gateway|auth-server|user-service|order-service)
      env_args+=("SPRING_CONFIG_IMPORT=optional:configserver:")
      ;;
  esac

  echo "[all-start] starting ${service}"
  env "${env_args[@]}" nohup java -jar "$jar" >"$log_file" 2>&1 < /dev/null &
  echo $! >"$pid_file"

  wait_for_health "$service" "$port" "$health_path"
}

start_frontend_app() {
  local app_name="$1"
  local port="$2"
  local health_path="$3"
  local app_dir="$4"
  local pid_file="$PID_DIR/${app_name}.pid"
  local log_file="$LOG_DIR/${app_name}.log"
  local next_cli="$ROOT_DIR/node_modules/next/dist/bin/next"

  if [[ -f "$pid_file" ]]; then
    local existing_pid
    existing_pid="$(cat "$pid_file")"
    if kill -0 "$existing_pid" >/dev/null 2>&1; then
      echo "[all-start] ${app_name} already running pid=${existing_pid}"
      return 0
    fi
    rm -f "$pid_file"
  fi

  if is_port_open "$port"; then
    echo "[all-start] port ${port} already in use" >&2
    return 1
  fi

  if [[ ! -f "$next_cli" ]]; then
    echo "[all-start] next cli not found. run ./scripts/build.sh first" >&2
    return 1
  fi

  echo "[all-start] starting ${app_name}"
  local current_dir
  current_dir="$(pwd)"
  cd "$ROOT_DIR/$app_dir"
  env \
    GATEWAY_URL="http://127.0.0.1:8000" \
    NEXT_PUBLIC_GATEWAY_URL="http://127.0.0.1:8000" \
    HOSTNAME="127.0.0.1" \
    nohup node "$next_cli" start --hostname 127.0.0.1 --port "$port" >"$log_file" 2>&1 < /dev/null &
  echo $! >"$pid_file"
  cd "$current_dir"

  wait_for_http_200 "$app_name" "http://127.0.0.1:${port}${health_path}"
}

build_if_needed

for item in "${BACKEND_SERVICES[@]}"; do
  IFS='|' read -r service port health_path <<<"$item"
  start_backend_service "$service" "$port" "$health_path"
done

wait_for_http_200 "gateway user route" "http://127.0.0.1:8000/api/v1/users"
wait_for_http_200 "gateway order route" "http://127.0.0.1:8000/api/v1/orders?userId=1"

for item in "${FRONTEND_APPS[@]}"; do
  IFS='|' read -r app_name port health_path app_dir <<<"$item"
  start_frontend_app "$app_name" "$port" "$health_path" "$app_dir"
done

echo "[all-start] all backend services and frontend apps started"
echo "[all-start] logs directory: $LOG_DIR"
echo "[all-start] pid directory: $PID_DIR"

if [[ "${ALL_START_HOLD:-false}" == "true" ]]; then
  echo "[all-start] hold mode enabled"
  trap 'exit 0' INT TERM
  while true; do
    sleep 1
  done
fi
