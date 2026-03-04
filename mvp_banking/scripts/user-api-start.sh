#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

PID_FILE="$PID_DIR/user-api.pid"
LOG_FILE="$LOG_DIR/user-api.log"

if ensure_pid_stopped "$PID_FILE" "User API"; then
  exit 0
fi

stop_listener_on_port "User API" 8082 >/dev/null 2>&1 || true
require_free_port 8082 "User API"
cd "$ROOT_DIR/backend"
./gradlew --no-daemon :user-api:bootJar >/dev/null
start_background_process \
  "User API" \
  "$PID_FILE" \
  "$LOG_FILE" \
  "http://localhost:8082/actuator/health" \
  java -jar "$ROOT_DIR/backend/user-api/build/libs/user-api-0.0.1-SNAPSHOT.jar" --spring.profiles.active=user-api

print_info_block \
  "User API access" \
  "Direct URL: http://localhost:8082" \
  "Health: http://localhost:8082/actuator/health" \
  "Ping: http://localhost:8082/api/system/ping" \
  "Gateway route: http://localhost:8080/api/user" \
  "Demo account: user@mvpbanking.local / User1234!" \
  "Log: $LOG_FILE"
