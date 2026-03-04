#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

stop_legacy_backend_if_present
"$SCRIPT_DIR/infra-start.sh"
"$SCRIPT_DIR/discovery-server-start.sh"
"$SCRIPT_DIR/admin-api-start.sh"
"$SCRIPT_DIR/user-api-start.sh"
wait_for_eureka_app "MVP-BANKING-ADMIN-API" 60
wait_for_eureka_app "MVP-BANKING-USER-API" 60
"$SCRIPT_DIR/api-gateway-start.sh"
wait_for_http_status \
  "http://localhost:8080/api/admin/auth/login" \
  "200" \
  60 \
  "Admin gateway route" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@mvpbanking.local","password":"Admin1234!"}'
wait_for_http_status \
  "http://localhost:8080/api/user/auth/login" \
  "200" \
  60 \
  "User gateway route" \
  -H "Content-Type: application/json" \
  -d '{"email":"user@mvpbanking.local","password":"User1234!"}'
"$SCRIPT_DIR/admin-portal-start.sh"
"$SCRIPT_DIR/user-web-app-start.sh"

echo "All services started."
print_info_block \
  "Service access summary" \
  "Admin portal: http://localhost:5173" \
  "User web app: http://localhost:5174" \
  "API gateway: http://localhost:8080" \
  "Discovery server: http://localhost:8761" \
  "Admin account: admin@mvpbanking.local / Admin1234!" \
  "User account: user@mvpbanking.local / User1234!" \
  "Logs: $LOG_DIR"
