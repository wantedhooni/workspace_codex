#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

PID_FILE="$ROOT_DIR/.runtime/pids/api-gateway.pid"

stop_pid_file "$PID_FILE" "API gateway" 8080
print_info_block \
  "API gateway stopped" \
  "Closed URL: http://localhost:8080"
