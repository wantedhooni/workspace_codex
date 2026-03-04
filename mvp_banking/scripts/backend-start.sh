#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

stop_legacy_backend_if_present
"$SCRIPT_DIR/discovery-server-start.sh"
"$SCRIPT_DIR/admin-api-start.sh"
"$SCRIPT_DIR/user-api-start.sh"
"$SCRIPT_DIR/api-gateway-start.sh"

echo "Backend MSA services started."
print_info_block \
  "Backend summary" \
  "Discovery: http://localhost:8761" \
  "Gateway: http://localhost:8080" \
  "Admin API: http://localhost:8081" \
  "User API: http://localhost:8082" \
  "Logs: $LOG_DIR"
