#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

PID_FILE="$ROOT_DIR/.runtime/pids/user-api.pid"

stop_pid_file "$PID_FILE" "User API" 8082
print_info_block \
  "User API stopped" \
  "Closed direct URL: http://localhost:8082" \
  "Gateway route affected: http://localhost:8080/api/user"
