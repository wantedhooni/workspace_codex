#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="$ROOT_DIR/logs"
PID_DIR="$ROOT_DIR/pids"

mkdir -p "$LOG_DIR" "$PID_DIR"

SERVICES=(
  "discovery-service|8761|/actuator/health"
  "config-server|8888|/actuator/health"
  "api-gateway|8000|/actuator/health"
  "auth-server|9000|/actuator/health"
  "user-service|8081|/actuator/health"
  "order-service|8082|/actuator/health"
)

build_if_needed() {
  echo "[all-start] building boot jars"
  (cd "$ROOT_DIR" && ./gradlew build)
}

jar_path() {
  local service="$1"
  find "$ROOT_DIR/services/$service/build/libs" -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | sort | head -n 1
}

is_port_open() {
  local port="$1"
  lsof -tiTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1
}

wait_for_health() {
  local service="$1"
  local port="$2"
  local path="$3"

  for _ in {1..60}; do
    if curl -fsS "http://127.0.0.1:${port}${path}" >/dev/null 2>&1; then
      echo "[all-start] ${service} is healthy on ${port}"
      return 0
    fi
    sleep 2
  done

  echo "[all-start] ${service} failed health check. see ${LOG_DIR}/${service}.log" >&2
  return 1
}

wait_for_http_200() {
  local name="$1"
  local url="$2"

  for _ in {1..30}; do
    if curl -fsS "$url" >/dev/null 2>&1; then
      echo "[all-start] ${name} is reachable"
      return 0
    fi
    sleep 2
  done

  echo "[all-start] ${name} did not become reachable: ${url}" >&2
  return 1
}

start_service() {
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
  (
    cd "$ROOT_DIR"
    if command -v setsid >/dev/null 2>&1; then
      env "${env_args[@]}" setsid java -jar "$jar" >"$log_file" 2>&1 < /dev/null &
    else
      env "${env_args[@]}" nohup java -jar "$jar" >"$log_file" 2>&1 < /dev/null &
    fi
    echo $! >"$pid_file"
  )

  wait_for_health "$service" "$port" "$health_path"
}

build_if_needed

for item in "${SERVICES[@]}"; do
  IFS='|' read -r service port health_path <<<"$item"
  start_service "$service" "$port" "$health_path"
done

wait_for_http_200 "gateway user route" "http://127.0.0.1:8000/api/v1/users"
wait_for_http_200 "gateway order route" "http://127.0.0.1:8000/api/v1/orders?userId=1"

echo "[all-start] all services started"
echo "[all-start] logs directory: $LOG_DIR"
echo "[all-start] pid directory: $PID_DIR"
