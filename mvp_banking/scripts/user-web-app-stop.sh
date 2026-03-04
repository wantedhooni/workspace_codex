#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

PID_FILE="$ROOT_DIR/.runtime/pids/user-web-app.pid"

stop_pid_file "$PID_FILE" "User web app" 5174
print_info_block \
  "User web app stopped" \
  "Closed URL: http://localhost:5174"
