#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

PID_FILE="$ROOT_DIR/.runtime/pids/admin-api.pid"

stop_pid_file "$PID_FILE" "Admin API" 8081
print_info_block \
  "Admin API stopped" \
  "Closed direct URL: http://localhost:8081" \
  "Gateway route affected: http://localhost:8080/api/admin"
