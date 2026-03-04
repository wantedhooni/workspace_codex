#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

service_status() {
  local service_name="$1"
  local pid_file="$2"
  local port="$3"
  local url="${4:-}"
  local log_file="${5:-}"

  local pid="n/a"
  local pid_state="stopped"
  local listener_pid="n/a"
  local port_state="closed"
  local http_state="n/a"

  if [[ -f "$pid_file" ]]; then
    pid=$(cat "$pid_file")
    if kill -0 "$pid" 2>/dev/null; then
      pid_state="running"
    else
      pid_state="stale"
    fi
  fi

  local detected_listener_pid
  detected_listener_pid=$(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null | head -n 1 || true)
  if [[ -n "$detected_listener_pid" ]]; then
    listener_pid="$detected_listener_pid"
    port_state="open"
  fi

  if [[ -n "$url" ]]; then
    http_state=$(curl -s -o /dev/null -w '%{http_code}' "$url" 2>/dev/null || true)
    if [[ -z "$http_state" || "$http_state" == "000" ]]; then
      http_state="down"
    fi
  fi

  print_info_block \
    "$service_name" \
    "pid file: $pid" \
    "pid state: $pid_state" \
    "port $port: $port_state" \
    "listener pid: $listener_pid" \
    "http: $http_state${url:+ ($url)}" \
    "log: ${log_file:-n/a}"
}

infra_status() {
  local postgres_state="stopped"
  local redis_state="stopped"

  if docker compose -f "$ROOT_DIR/infra/docker-compose.yml" ps --status running --services 2>/dev/null | grep -qx "postgres"; then
    postgres_state="running"
  fi

  if docker compose -f "$ROOT_DIR/infra/docker-compose.yml" ps --status running --services 2>/dev/null | grep -qx "redis"; then
    redis_state="running"
  fi

  print_info_block \
    "Infra" \
    "PostgreSQL: $postgres_state (localhost:5432)" \
    "Redis: $redis_state (localhost:6379)"
}

infra_status
service_status "Discovery server" "$PID_DIR/discovery-server.pid" 8761 "http://localhost:8761/actuator/health" "$LOG_DIR/discovery-server.log"
service_status "Admin API" "$PID_DIR/admin-api.pid" 8081 "http://localhost:8081/actuator/health" "$LOG_DIR/admin-api.log"
service_status "User API" "$PID_DIR/user-api.pid" 8082 "http://localhost:8082/actuator/health" "$LOG_DIR/user-api.log"
service_status "API gateway" "$PID_DIR/api-gateway.pid" 8080 "http://localhost:8080/actuator/health" "$LOG_DIR/api-gateway.log"
service_status "Admin portal" "$PID_DIR/admin-portal.pid" 5173 "http://localhost:5173" "$LOG_DIR/admin-portal.log"
service_status "User web app" "$PID_DIR/user-web-app.pid" 5174 "http://localhost:5174" "$LOG_DIR/user-web-app.log"

