#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

PID_FILE="$PID_DIR/api-gateway.pid"
LOG_FILE="$LOG_DIR/api-gateway.log"

if ensure_pid_stopped "$PID_FILE" "API gateway"; then
  exit 0
fi

stop_listener_on_port "API gateway" 8080 >/dev/null 2>&1 || true
require_free_port 8080 "API gateway"
cd "$ROOT_DIR/backend"
./gradlew --no-daemon :api-gateway:bootJar >/dev/null
start_background_process \
  "API gateway" \
  "$PID_FILE" \
  "$LOG_FILE" \
  "http://localhost:8080/actuator/health" \
  java -jar "$ROOT_DIR/backend/api-gateway/build/libs/api-gateway-0.0.1-SNAPSHOT.jar" --spring.profiles.active=api-gateway

print_info_block \
  "API gateway access" \
  "URL: http://localhost:8080" \
  "Health: http://localhost:8080/actuator/health" \
  "Admin login: POST http://localhost:8080/api/admin/auth/login" \
  "User login: POST http://localhost:8080/api/user/auth/login" \
  "Log: $LOG_FILE"
