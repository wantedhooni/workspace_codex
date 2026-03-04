#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

"$SCRIPT_DIR/api-gateway-stop.sh"
"$SCRIPT_DIR/user-api-stop.sh"
"$SCRIPT_DIR/admin-api-stop.sh"
"$SCRIPT_DIR/discovery-server-stop.sh"

echo "Backend MSA services stopped."
print_info_block \
  "Backend summary" \
  "Closed discovery: http://localhost:8761" \
  "Closed gateway: http://localhost:8080" \
  "Closed admin API: http://localhost:8081" \
  "Closed user API: http://localhost:8082"
