#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

PID_FILE="$PID_DIR/admin-api.pid"
LOG_FILE="$LOG_DIR/admin-api.log"

if ensure_pid_stopped "$PID_FILE" "Admin API"; then
  exit 0
fi

stop_listener_on_port "Admin API" 8081 >/dev/null 2>&1 || true
require_free_port 8081 "Admin API"
cd "$ROOT_DIR/backend"
./gradlew --no-daemon :admin-api:bootJar >/dev/null
start_background_process \
  "Admin API" \
  "$PID_FILE" \
  "$LOG_FILE" \
  "http://localhost:8081/actuator/health" \
  java -jar "$ROOT_DIR/backend/admin-api/build/libs/admin-api-0.0.1-SNAPSHOT.jar" --spring.profiles.active=admin-api

print_info_block \
  "Admin API access" \
  "Direct URL: http://localhost:8081" \
  "Health: http://localhost:8081/actuator/health" \
  "Ping: http://localhost:8081/api/system/ping" \
  "Gateway route: http://localhost:8080/api/admin" \
  "Demo account: admin@mvpbanking.local / Admin1234!" \
  "Log: $LOG_FILE"
