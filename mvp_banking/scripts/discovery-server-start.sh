#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

PID_FILE="$PID_DIR/discovery-server.pid"
LOG_FILE="$LOG_DIR/discovery-server.log"

if ensure_pid_stopped "$PID_FILE" "Discovery server"; then
  exit 0
fi

stop_listener_on_port "Discovery server" 8761 >/dev/null 2>&1 || true
require_free_port 8761 "Discovery server"
cd "$ROOT_DIR/backend"
./gradlew --no-daemon :discovery-server:bootJar >/dev/null
start_background_process \
  "Discovery server" \
  "$PID_FILE" \
  "$LOG_FILE" \
  "http://localhost:8761/actuator/health" \
  java -jar "$ROOT_DIR/backend/discovery-server/build/libs/discovery-server-0.0.1-SNAPSHOT.jar" --spring.profiles.active=discovery-server

print_info_block \
  "Discovery server access" \
  "URL: http://localhost:8761" \
  "Health: http://localhost:8761/actuator/health" \
  "Log: $LOG_FILE"
