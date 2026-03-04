#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source "$SCRIPT_DIR/_common.sh"

START_STACK="${START_STACK:-1}"
STOP_STACK="${STOP_STACK:-0}"

extract_json_value() {
  local json="$1"
  local expression="$2"
  JSON_PAYLOAD="$json" python3 -c '
import json
import os
import sys

expr = sys.argv[1].split(".")
data = json.loads(os.environ["JSON_PAYLOAD"])
for part in expr:
    if part.isdigit():
        data = data[int(part)]
    else:
        data = data[part]
print(data)
' "$expression"
}

cleanup() {
  if [[ "$STOP_STACK" == "1" ]]; then
    "$SCRIPT_DIR/all-stop.sh"
  fi
}

trap cleanup EXIT

if [[ "$START_STACK" == "1" ]]; then
  "$SCRIPT_DIR/all-start.sh"
fi

curl -fsS http://localhost:8761/actuator/health >/dev/null
curl -fsS http://localhost:8080/actuator/health >/dev/null
curl -fsS http://localhost:5173 >/dev/null
curl -fsS http://localhost:5174 >/dev/null

admin_login_response=$(curl -fsS \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@mvpbanking.local","password":"Admin1234!"}' \
  http://localhost:8080/api/admin/auth/login)
admin_token=$(extract_json_value "$admin_login_response" "data.accessToken")

curl -fsS -H "Authorization: Bearer $admin_token" http://localhost:8080/api/admin/me >/dev/null
curl -fsS -H "Authorization: Bearer $admin_token" http://localhost:8080/api/admin/overview >/dev/null
curl -fsS -H "Authorization: Bearer $admin_token" http://localhost:8080/api/admin/announcements >/dev/null
curl -fsS -H "Authorization: Bearer $admin_token" "http://localhost:8080/api/admin/customers?page=0&size=5" >/dev/null
curl -fsS -H "Authorization: Bearer $admin_token" http://localhost:8080/api/admin/funding-requests >/dev/null
curl -fsS -H "Authorization: Bearer $admin_token" http://localhost:8080/api/admin/stock-orders >/dev/null
curl -fsS -H "Authorization: Bearer $admin_token" http://localhost:8080/api/admin/stock-positions >/dev/null
curl -fsS -H "Authorization: Bearer $admin_token" http://localhost:8080/api/admin/exchange-requests >/dev/null
admin_notifications_response=$(curl -fsS -H "Authorization: Bearer $admin_token" http://localhost:8080/api/admin/notifications)
admin_notification_id=$(extract_json_value "$admin_notifications_response" "data.items.0.id")
curl -fsS -X POST -H "Authorization: Bearer $admin_token" "http://localhost:8080/api/admin/notifications/$admin_notification_id/read" >/dev/null

user_login_response=$(curl -fsS \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@mvpbanking.local","password":"User1234!"}' \
  http://localhost:8080/api/user/auth/login)
user_token=$(extract_json_value "$user_login_response" "data.accessToken")

curl -fsS -H "Authorization: Bearer $user_token" http://localhost:8080/api/user/me >/dev/null
curl -fsS -H "Authorization: Bearer $user_token" http://localhost:8080/api/user/dashboard/insights >/dev/null
curl -fsS -H "Authorization: Bearer $user_token" http://localhost:8080/api/user/announcements >/dev/null
curl -fsS -H "Authorization: Bearer $user_token" http://localhost:8080/api/user/accounts >/dev/null
curl -fsS -H "Authorization: Bearer $user_token" http://localhost:8080/api/user/funding-requests >/dev/null
curl -fsS -H "Authorization: Bearer $user_token" http://localhost:8080/api/user/stock-orders >/dev/null
curl -fsS -H "Authorization: Bearer $user_token" http://localhost:8080/api/user/stock-positions >/dev/null
curl -fsS -H "Authorization: Bearer $user_token" http://localhost:8080/api/user/exchange-requests >/dev/null
user_notifications_response=$(curl -fsS -H "Authorization: Bearer $user_token" http://localhost:8080/api/user/notifications)
user_notification_id=$(extract_json_value "$user_notifications_response" "data.items.0.id")
curl -fsS -X POST -H "Authorization: Bearer $user_token" "http://localhost:8080/api/user/notifications/$user_notification_id/read" >/dev/null

echo "E2E smoke test passed."
