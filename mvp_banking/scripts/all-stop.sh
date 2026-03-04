#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

"$SCRIPT_DIR/user-web-app-stop.sh"
"$SCRIPT_DIR/admin-portal-stop.sh"
"$SCRIPT_DIR/api-gateway-stop.sh"
"$SCRIPT_DIR/user-api-stop.sh"
"$SCRIPT_DIR/admin-api-stop.sh"
"$SCRIPT_DIR/discovery-server-stop.sh"
"$SCRIPT_DIR/infra-stop.sh"

echo "All services stopped."
print_info_block \
  "Service access summary" \
  "Closed admin portal: http://localhost:5173" \
  "Closed user web app: http://localhost:5174" \
  "Closed API gateway: http://localhost:8080" \
  "Closed discovery server: http://localhost:8761"
