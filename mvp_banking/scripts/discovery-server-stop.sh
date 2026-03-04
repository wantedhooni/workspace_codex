#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

PID_FILE="$ROOT_DIR/.runtime/pids/discovery-server.pid"

stop_pid_file "$PID_FILE" "Discovery server" 8761
print_info_block \
  "Discovery server stopped" \
  "Closed URL: http://localhost:8761"
