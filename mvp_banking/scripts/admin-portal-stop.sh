#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

PID_FILE="$ROOT_DIR/.runtime/pids/admin-portal.pid"

stop_pid_file "$PID_FILE" "Admin portal" 5173
print_info_block \
  "Admin portal stopped" \
  "Closed URL: http://localhost:5173"
