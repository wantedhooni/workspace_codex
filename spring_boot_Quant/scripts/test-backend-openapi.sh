#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
API_BASE="http://127.0.0.1:8088"
LOG_DIR="$ROOT_DIR/doc/test/artifacts"
BACKEND_PROFILE="${BACKEND_PROFILE:-local}"
mkdir -p "$LOG_DIR"

kill_port_listener() {
  local port="$1"
  local pids
  pids="$(lsof -tiTCP:${port} -sTCP:LISTEN 2>/dev/null || true)"
  if [[ -n "$pids" ]]; then
    kill $pids 2>/dev/null || true
    sleep 1
    pids="$(lsof -tiTCP:${port} -sTCP:LISTEN 2>/dev/null || true)"
    if [[ -n "$pids" ]]; then
      kill -9 $pids 2>/dev/null || true
    fi
  fi
}

cleanup() {
  if [[ -f /tmp/backend-openapi.pid ]]; then
    kill "$(cat /tmp/backend-openapi.pid)" 2>/dev/null || true
    rm -f /tmp/backend-openapi.pid
  fi
  kill_port_listener 8088
}
trap cleanup EXIT

cd "$ROOT_DIR"
./scripts/infra-up.sh
./scripts/db-import-demo.sh

cd "$ROOT_DIR/backend-mvp"
gradle test > "$LOG_DIR/backend-gradle-test.log"

kill_port_listener 8088
SPRING_PROFILES_ACTIVE="$BACKEND_PROFILE" gradle bootRun > /tmp/backend-openapi.log 2>&1 &
echo $! > /tmp/backend-openapi.pid

for i in {1..90}; do
  if curl -fsS "$API_BASE/actuator/health" >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

curl -fsS "$API_BASE/v3/api-docs" > "$LOG_DIR/openapi.json"

login_token() {
  local email="$1"
  local password="$2"
  curl -fsS -X POST "$API_BASE/api/auth/login" \
    -H 'Content-Type: application/json' \
    -d "{\"email\":\"$email\",\"password\":\"$password\"}" | jq -r '.accessToken'
}

ADMIN_TOKEN="$(login_token 'admin@quant.io' 'demo1234')"
TRADER_TOKEN="$(login_token 'trader@quant.io' 'trader1234')"
VIEWER_TOKEN="$(login_token 'viewer@quant.io' 'viewer1234')"

ADMIN_AUTH=(-H "Authorization: Bearer $ADMIN_TOKEN")
TRADER_AUTH=(-H "Authorization: Bearer $TRADER_TOKEN")
VIEWER_AUTH=(-H "Authorization: Bearer $VIEWER_TOKEN")

ORDER_JSON=$(curl -fsS -X POST "$API_BASE/api/orders" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"symbol":"TSLA","side":"BUY","orderType":"LIMIT","timeInForce":"DAY","limitPrice":210.50,"quantity":4}')
ORDER_ID=$(echo "$ORDER_JSON" | jq -r '.orderId')
TRADE_JSON=$(curl -fsS -X POST "$API_BASE/api/trades/events" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d "{\"orderId\":$ORDER_ID,\"tradeQuantity\":2,\"tradePrice\":210.50}")
TRADE_ID=$(echo "$TRADE_JSON" | jq -r '.tradeId')

# order-health 검증용 오픈 주문
curl -fsS -X POST "$API_BASE/api/orders" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"symbol":"IWM","side":"BUY","orderType":"LIMIT","timeInForce":"DAY","limitPrice":204.10,"quantity":3}' > "$LOG_DIR/api-orders-order-health-open.json"

DELETE_ORDER_JSON=$(curl -fsS -X POST "$API_BASE/api/orders" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"symbol":"AMZN","side":"BUY","orderType":"MARKET","timeInForce":"DAY","quantity":1}')
DELETE_ORDER_ID=$(echo "$DELETE_ORDER_JSON" | jq -r '.orderId')
curl -fsS -X DELETE "$API_BASE/api/orders/$DELETE_ORDER_ID" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-orders-delete.json"

CANCEL_ORDER_JSON=$(curl -fsS -X POST "$API_BASE/api/orders" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"symbol":"NVDA","side":"BUY","orderType":"MARKET","timeInForce":"DAY","quantity":2}')
CANCEL_ORDER_ID=$(echo "$CANCEL_ORDER_JSON" | jq -r '.orderId')
curl -fsS -X POST "$API_BASE/api/orders/$CANCEL_ORDER_ID/cancel" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"reason":"manual cancel test"}' > "$LOG_DIR/api-orders-cancel.json"

REJECT_ORDER_JSON=$(curl -fsS -X POST "$API_BASE/api/orders" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"symbol":"GOOG","side":"BUY","orderType":"LIMIT","timeInForce":"DAY","limitPrice":130.10,"quantity":1}')
REJECT_ORDER_ID=$(echo "$REJECT_ORDER_JSON" | jq -r '.orderId')
curl -fsS -X POST "$API_BASE/api/orders/$REJECT_ORDER_ID/reject" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"reason":"risk reject test"}' > "$LOG_DIR/api-orders-reject.json"

BULK_CANCEL_ORDER1_JSON=$(curl -fsS -X POST "$API_BASE/api/orders" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"symbol":"ADBE","side":"BUY","orderType":"LIMIT","timeInForce":"DAY","limitPrice":520.00,"quantity":1}')
BULK_CANCEL_ORDER1_ID=$(echo "$BULK_CANCEL_ORDER1_JSON" | jq -r '.orderId')
BULK_CANCEL_ORDER2_JSON=$(curl -fsS -X POST "$API_BASE/api/orders" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"symbol":"CRM","side":"BUY","orderType":"LIMIT","timeInForce":"DAY","limitPrice":310.00,"quantity":1}')
BULK_CANCEL_ORDER2_ID=$(echo "$BULK_CANCEL_ORDER2_JSON" | jq -r '.orderId')
curl -fsS -X POST "$API_BASE/api/orders/bulk/cancel" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d "{\"orderIds\":[$BULK_CANCEL_ORDER1_ID,$BULK_CANCEL_ORDER2_ID],\"reason\":\"bulk cancel test\"}" > "$LOG_DIR/api-orders-bulk-cancel.json"

BULK_REJECT_ORDER1_JSON=$(curl -fsS -X POST "$API_BASE/api/orders" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"symbol":"INTU","side":"BUY","orderType":"LIMIT","timeInForce":"DAY","limitPrice":620.00,"quantity":1}')
BULK_REJECT_ORDER1_ID=$(echo "$BULK_REJECT_ORDER1_JSON" | jq -r '.orderId')
BULK_REJECT_ORDER2_JSON=$(curl -fsS -X POST "$API_BASE/api/orders" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"symbol":"SNOW","side":"BUY","orderType":"LIMIT","timeInForce":"DAY","limitPrice":220.00,"quantity":1}')
BULK_REJECT_ORDER2_ID=$(echo "$BULK_REJECT_ORDER2_JSON" | jq -r '.orderId')
curl -fsS -X POST "$API_BASE/api/orders/bulk/reject" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d "{\"orderIds\":[$BULK_REJECT_ORDER1_ID,$BULK_REJECT_ORDER2_ID],\"reason\":\"bulk reject test\"}" > "$LOG_DIR/api-orders-bulk-reject.json"

curl -fsS -X PUT "$API_BASE/api/risk-limits" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"maxOrderNotional":2000000,"maxPositionNotionalPerSymbol":5000000,"maxDailyTurnover":8000000,"maxOpenOrdersPerSymbol":30,"commissionBps":2.0,"slippageBps":1.0}' > "$LOG_DIR/api-risk-limits-upsert.json"
curl -fsS "$API_BASE/api/risk-limits/trading-controls?portfolioId=1" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-trading-controls-before.json"

curl -fsS -X PUT "$API_BASE/api/risk-limits/trading-controls" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"tradingEnabled":false,"reason":"openapi kill switch test"}' > "$LOG_DIR/api-trading-controls-disable.json"

KILL_SWITCH_BLOCK_HTTP=$(curl -sS -o "$LOG_DIR/api-orders-kill-switch-blocked.json" -w "%{http_code}" -X POST "$API_BASE/api/orders" \
  "${ADMIN_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"portfolioId":1,"symbol":"QQQ","side":"BUY","orderType":"MARKET","timeInForce":"DAY","quantity":1}')
if [[ "$KILL_SWITCH_BLOCK_HTTP" != "400" ]]; then
  echo "[backend-openapi] expected 400 when kill switch is disabled, got $KILL_SWITCH_BLOCK_HTTP"
  exit 1
fi

curl -fsS "$API_BASE/api/risk-alerts?portfolioId=1" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-risk-alerts-kill-switch.json"
TRADING_HALTED_ALERT_KEY=$(jq -r '.items[] | select(.code=="TRADING_HALTED") | .alertKey' "$LOG_DIR/api-risk-alerts-kill-switch.json" | head -n1)
if [[ -z "$TRADING_HALTED_ALERT_KEY" || "$TRADING_HALTED_ALERT_KEY" == "null" ]]; then
  echo "[backend-openapi] expected TRADING_HALTED alert key after kill switch disable"
  exit 1
fi

curl -fsS -X POST "$API_BASE/api/risk-alerts/ack" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d "{\"portfolioId\":1,\"alertKey\":\"$TRADING_HALTED_ALERT_KEY\",\"note\":\"openapi ack test\"}" > "$LOG_DIR/api-risk-alerts-ack.json"
jq -e '.item.acknowledged == true' "$LOG_DIR/api-risk-alerts-ack.json" >/dev/null || {
  echo "[backend-openapi] expected acknowledged=true after risk alert ack"
  exit 1
}

curl -fsS -X POST "$API_BASE/api/risk-alerts/unack" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d "{\"portfolioId\":1,\"alertKey\":\"$TRADING_HALTED_ALERT_KEY\"}" > "$LOG_DIR/api-risk-alerts-unack.json"
jq -e '.item.acknowledged == false' "$LOG_DIR/api-risk-alerts-unack.json" >/dev/null || {
  echo "[backend-openapi] expected acknowledged=false after risk alert unack"
  exit 1
}

# workflow 전이 검증(ACK -> IN_PROGRESS -> RESOLVED -> OPEN)
curl -fsS -X POST "$API_BASE/api/risk-alerts/ack" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d "{\"portfolioId\":1,\"alertKey\":\"$TRADING_HALTED_ALERT_KEY\",\"note\":\"openapi workflow setup\"}" > /dev/null
curl -fsS -X POST "$API_BASE/api/risk-alerts/workflow" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d "{\"portfolioId\":1,\"alertKey\":\"$TRADING_HALTED_ALERT_KEY\",\"workflowStatus\":\"IN_PROGRESS\",\"note\":\"triage started\",\"assignee\":\"risk@quant.io\"}" > "$LOG_DIR/api-risk-alerts-workflow-in-progress.json"
jq -e '.item.workflowStatus == "IN_PROGRESS" and .item.assignee == "risk@quant.io"' "$LOG_DIR/api-risk-alerts-workflow-in-progress.json" >/dev/null || {
  echo "[backend-openapi] expected IN_PROGRESS workflow state"
  exit 1
}

curl -fsS -X POST "$API_BASE/api/risk-alerts/workflow" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d "{\"portfolioId\":1,\"alertKey\":\"$TRADING_HALTED_ALERT_KEY\",\"workflowStatus\":\"RESOLVED\",\"note\":\"resolved by script\",\"assignee\":\"risk@quant.io\"}" > "$LOG_DIR/api-risk-alerts-workflow-resolved.json"
jq -e '.item.workflowStatus == "RESOLVED" and .item.resolvedBy == "admin@quant.io"' "$LOG_DIR/api-risk-alerts-workflow-resolved.json" >/dev/null || {
  echo "[backend-openapi] expected RESOLVED workflow state"
  exit 1
}

curl -fsS -X POST "$API_BASE/api/risk-alerts/workflow" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d "{\"portfolioId\":1,\"alertKey\":\"$TRADING_HALTED_ALERT_KEY\",\"workflowStatus\":\"OPEN\",\"note\":\"reopen after verification\",\"assignee\":\"\"}" > "$LOG_DIR/api-risk-alerts-workflow-open.json"
jq -e '.item.workflowStatus == "OPEN"' "$LOG_DIR/api-risk-alerts-workflow-open.json" >/dev/null || {
  echo "[backend-openapi] expected OPEN workflow state"
  exit 1
}

curl -fsS -X PUT "$API_BASE/api/risk-limits/trading-controls" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"tradingEnabled":true,"reason":"openapi resume test"}' > "$LOG_DIR/api-trading-controls-enable.json"
curl -fsS "$API_BASE/api/risk-limits/trading-controls/history?portfolioId=1&limit=20" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-trading-controls-history.json"

ROLE_JSON=$(curl -fsS -X POST "$API_BASE/api/roles" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"roleCode":"AUDITOR","roleName":"Auditor","description":"audit role","systemRole":false}')
ROLE_ID=$(echo "$ROLE_JSON" | jq -r '.roleId')
MENU_JSON=$(curl -fsS -X POST "$API_BASE/api/menus" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"parentMenuId":null,"menuKey":"auditMenu","menuLabel":"감사메뉴","path":"/#/audit","icon":"fact_check","sortOrder":210,"enabled":true}')
MENU_ID=$(echo "$MENU_JSON" | jq -r '.menuId')
USER_JSON=$(curl -fsS -X POST "$API_BASE/api/users" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"email":"api-test-user@quant.io","name":"API User","status":"ACTIVE","roleCodes":["VIEWER"]}')
USER_ID=$(echo "$USER_JSON" | jq -r '.userId')
SAVED_VIEW_JSON=$(curl -fsS -X POST "$API_BASE/api/saved-views" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"resourceKey":"orders","viewName":"OpenAPI Focus","description":"api test view","shared":true,"filters":{"portfolioId":"1","status":"NEW"}}')
SAVED_VIEW_ID=$(echo "$SAVED_VIEW_JSON" | jq -r '.viewId')
curl -fsS -X POST "$API_BASE/api/saved-views/default" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d "{\"resourceKey\":\"orders\",\"viewId\":$SAVED_VIEW_ID}" > "$LOG_DIR/api-saved-views-default-pin.json"
curl -fsS "$API_BASE/api/saved-views/default?resourceKey=orders" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-saved-views-default.json"

curl -fsS -X PUT "$API_BASE/api/users/$USER_ID/status" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"status":"LOCKED"}' > "$LOG_DIR/api-users-status.json"
curl -fsS -X PUT "$API_BASE/api/users/$USER_ID/roles" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"roleCodes":["VIEWER","RISK"]}' > "$LOG_DIR/api-users-roles.json"
curl -fsS -X POST "$API_BASE/api/users/$USER_ID/reset-password" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{}' > "$LOG_DIR/api-users-reset-password.json"

MENU_PERMISSION_JSON=$(curl -fsS -X PUT "$API_BASE/api/menu-permissions" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d "{\"menuId\":$MENU_ID,\"roleId\":$ROLE_ID,\"canRead\":true,\"canCreate\":false,\"canUpdate\":false,\"canDelete\":false}")
MENU_PERMISSION_ID=$(echo "$MENU_PERMISSION_JSON" | jq -r '.menuPermissionId')
echo "$MENU_PERMISSION_JSON" > "$LOG_DIR/api-menu-permissions-upsert.json"

curl -fsS "$API_BASE/api/account/me" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-account-me.json"
curl -fsS "$API_BASE/api/account/menus" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-account-menus-admin.json"
curl -fsS "$API_BASE/api/account/menus" "${TRADER_AUTH[@]}" > "$LOG_DIR/api-account-menus-trader.json"
curl -fsS "$API_BASE/api/account/work-queue?portfolioId=1&staleMinutes=0&topN=8" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-account-work-queue-admin.json"
curl -fsS "$API_BASE/api/account/work-queue?portfolioId=1&staleMinutes=0&topN=8" "${VIEWER_AUTH[@]}" > "$LOG_DIR/api-account-work-queue-viewer.json"
curl -fsS "$API_BASE/api/account/activity-feed?portfolioId=1&limit=8" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-account-activity-feed-admin.json"
curl -fsS "$API_BASE/api/account/activity-feed?portfolioId=1&limit=8" "${VIEWER_AUTH[@]}" > "$LOG_DIR/api-account-activity-feed-viewer.json"

# work-queue 액션 검증용 오픈 주문 생성
curl -fsS -X POST "$API_BASE/api/orders" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"symbol":"WQAA","side":"BUY","orderType":"LIMIT","timeInForce":"DAY","limitPrice":110.25,"quantity":1}' > "$LOG_DIR/api-orders-work-queue-open.json"
curl -fsS -X POST "$API_BASE/api/account/work-queue/actions/remediate-stale-orders" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"staleMinutes":0,"reason":"openapi work-queue remediation"}' > "$LOG_DIR/api-account-work-queue-remediate.json"
curl -fsS -X POST "$API_BASE/api/account/work-queue/actions/revoke-other-sessions" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"reason":"openapi session hardening"}' > "$LOG_DIR/api-account-work-queue-revoke-sessions.json"
curl -fsS -X POST "$API_BASE/api/account/work-queue/actions/post-approved-vouchers" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"limit":20,"reason":"openapi approved voucher posting"}' > "$LOG_DIR/api-account-work-queue-post-approved-vouchers.json"
curl -fsS -X POST "$API_BASE/api/account/work-queue/actions/approve-draft-vouchers" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"limit":20,"reason":"openapi draft voucher approval"}' > "$LOG_DIR/api-account-work-queue-approve-draft-vouchers.json"
curl -fsS -X POST "$API_BASE/api/account/work-queue/actions/pause-trading" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"reason":"openapi critical risk pause"}' > "$LOG_DIR/api-account-work-queue-pause-trading.json"
curl -fsS -X PUT "$API_BASE/api/risk-limits/trading-controls" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"tradingEnabled":true,"reason":"openapi resume after pause-trading"}' > "$LOG_DIR/api-account-work-queue-pause-trading-resume.json"
EMERGENCY_ORDER_JSON=$(curl -fsS -X POST "$API_BASE/api/orders" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"symbol":"EMRX","side":"BUY","orderType":"LIMIT","timeInForce":"DAY","limitPrice":120.10,"quantity":2}')
EMERGENCY_ORDER_ID=$(echo "$EMERGENCY_ORDER_JSON" | jq -r '.orderId')
curl -fsS -X POST "$API_BASE/api/account/work-queue/actions/emergency-risk-response" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"reason":"openapi emergency action","cancelOpenOrders":true}' > "$LOG_DIR/api-account-work-queue-emergency-risk-response.json"
curl -fsS "$API_BASE/api/orders?portfolioId=1&symbol=EMRX" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-account-work-queue-emergency-order-after.json"
curl -fsS -X POST "$API_BASE/api/account/work-queue/actions/resume-trading" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"reason":"openapi resume after emergency-risk-response","force":true}' > "$LOG_DIR/api-account-work-queue-resume-trading.json"

# trader 계정은 users 메뉴를 보면 안 된다.
if jq -e '.items[] | select(.menuKey == "users")' "$LOG_DIR/api-account-menus-trader.json" >/dev/null; then
  echo "[backend-openapi] trader menu should not contain users"
  exit 1
fi

TRADER_USERS_HTTP=$(curl -sS -o "$LOG_DIR/api-users-trader-forbidden.json" -w "%{http_code}" "$API_BASE/api/users" "${TRADER_AUTH[@]}")
if [[ "$TRADER_USERS_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for trader on /api/users, got $TRADER_USERS_HTTP"
  exit 1
fi
jq -e '.code == "FORBIDDEN"' "$LOG_DIR/api-users-trader-forbidden.json" >/dev/null || {
  echo "[backend-openapi] invalid forbidden payload for trader /api/users"
  exit 1
}

VIEWER_ORDER_HTTP=$(curl -sS -o "$LOG_DIR/api-orders-viewer-forbidden.json" -w "%{http_code}" -X POST "$API_BASE/api/orders" \
  "${VIEWER_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"portfolioId":1,"symbol":"AAPL","side":"BUY","orderType":"MARKET","timeInForce":"DAY","quantity":1}')
if [[ "$VIEWER_ORDER_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for viewer on POST /api/orders, got $VIEWER_ORDER_HTTP"
  exit 1
fi

TRADER_CANCEL_HTTP=$(curl -sS -o "$LOG_DIR/api-orders-trader-cancel-forbidden.json" -w "%{http_code}" -X POST "$API_BASE/api/orders/$ORDER_ID/cancel" \
  "${TRADER_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"reason":"not allowed"}')
if [[ "$TRADER_CANCEL_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for trader on POST /api/orders/{orderId}/cancel, got $TRADER_CANCEL_HTTP"
  exit 1
fi

TRADER_BULK_REJECT_HTTP=$(curl -sS -o "$LOG_DIR/api-orders-bulk-reject-trader-forbidden.json" -w "%{http_code}" -X POST "$API_BASE/api/orders/bulk/reject" \
  "${TRADER_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"orderIds":[1001,1002],"reason":"not allowed"}')
if [[ "$TRADER_BULK_REJECT_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for trader on POST /api/orders/bulk/reject, got $TRADER_BULK_REJECT_HTTP"
  exit 1
fi

VIEWER_AUDIT_HTTP=$(curl -sS -o "$LOG_DIR/api-order-audits-viewer-forbidden.json" -w "%{http_code}" "$API_BASE/api/orders/audit-logs" "${VIEWER_AUTH[@]}")
if [[ "$VIEWER_AUDIT_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for viewer on /api/orders/audit-logs, got $VIEWER_AUDIT_HTTP"
  exit 1
fi

VIEWER_AUDIT_SUMMARY_HTTP=$(curl -sS -o "$LOG_DIR/api-order-audits-summary-viewer-forbidden.json" -w "%{http_code}" "$API_BASE/api/orders/audit-logs/summary?portfolioId=1" "${VIEWER_AUTH[@]}")
if [[ "$VIEWER_AUDIT_SUMMARY_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for viewer on /api/orders/audit-logs/summary, got $VIEWER_AUDIT_SUMMARY_HTTP"
  exit 1
fi

TRADER_RISK_HTTP=$(curl -sS -o "$LOG_DIR/api-risk-limits-trader-forbidden.json" -w "%{http_code}" -X PUT "$API_BASE/api/risk-limits" \
  "${TRADER_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"portfolioId":1,"maxOrderNotional":2100000,"maxPositionNotionalPerSymbol":5200000,"maxDailyTurnover":8100000,"maxOpenOrdersPerSymbol":31,"commissionBps":2.0,"slippageBps":1.0}')
if [[ "$TRADER_RISK_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for trader on PUT /api/risk-limits, got $TRADER_RISK_HTTP"
  exit 1
fi

TRADER_TRADING_CONTROL_HTTP=$(curl -sS -o "$LOG_DIR/api-trading-controls-trader-forbidden.json" -w "%{http_code}" -X PUT "$API_BASE/api/risk-limits/trading-controls" \
  "${TRADER_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"portfolioId":1,"tradingEnabled":false,"reason":"not allowed"}')
if [[ "$TRADER_TRADING_CONTROL_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for trader on PUT /api/risk-limits/trading-controls, got $TRADER_TRADING_CONTROL_HTTP"
  exit 1
fi

VIEWER_RISK_ALERT_ACK_HTTP=$(curl -sS -o "$LOG_DIR/api-risk-alerts-ack-viewer-forbidden.json" -w "%{http_code}" -X POST "$API_BASE/api/risk-alerts/ack" \
  "${VIEWER_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"portfolioId":1,"alertKey":"RISK_OK_1","note":"viewer forbidden"}')
if [[ "$VIEWER_RISK_ALERT_ACK_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for viewer on POST /api/risk-alerts/ack, got $VIEWER_RISK_ALERT_ACK_HTTP"
  exit 1
fi

VIEWER_RISK_ALERT_WORKFLOW_HTTP=$(curl -sS -o "$LOG_DIR/api-risk-alerts-workflow-viewer-forbidden.json" -w "%{http_code}" -X POST "$API_BASE/api/risk-alerts/workflow" \
  "${VIEWER_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"portfolioId":1,"alertKey":"TRADING_HALTED","workflowStatus":"IN_PROGRESS","note":"viewer forbidden","assignee":"viewer@quant.io"}')
if [[ "$VIEWER_RISK_ALERT_WORKFLOW_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for viewer on POST /api/risk-alerts/workflow, got $VIEWER_RISK_ALERT_WORKFLOW_HTTP"
  exit 1
fi

VIEWER_ORDER_HEALTH_REMEDIATE_HTTP=$(curl -sS -o "$LOG_DIR/api-order-health-remediate-viewer-forbidden.json" -w "%{http_code}" -X POST "$API_BASE/api/order-health/remediate-stale" \
  "${VIEWER_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"portfolioId":1,"symbol":"IWM","staleMinutes":0,"reason":"viewer should be forbidden"}')
if [[ "$VIEWER_ORDER_HEALTH_REMEDIATE_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for viewer on POST /api/order-health/remediate-stale, got $VIEWER_ORDER_HEALTH_REMEDIATE_HTTP"
  exit 1
fi

VIEWER_SAVED_VIEW_CREATE_HTTP=$(curl -sS -o "$LOG_DIR/api-saved-views-viewer-forbidden.json" -w "%{http_code}" -X POST "$API_BASE/api/saved-views" \
  "${VIEWER_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"resourceKey":"orders","viewName":"viewer-forbidden","description":"forbidden","shared":false,"filters":{"status":"NEW"}}')
if [[ "$VIEWER_SAVED_VIEW_CREATE_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for viewer on POST /api/saved-views, got $VIEWER_SAVED_VIEW_CREATE_HTTP"
  exit 1
fi

VIEWER_REMEDIATE_HTTP=$(curl -sS -o "$LOG_DIR/api-account-work-queue-remediate-viewer-forbidden.json" -w "%{http_code}" -X POST "$API_BASE/api/account/work-queue/actions/remediate-stale-orders" \
  "${VIEWER_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"portfolioId":1,"staleMinutes":0,"reason":"viewer forbidden"}')
if [[ "$VIEWER_REMEDIATE_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for viewer on POST /api/account/work-queue/actions/remediate-stale-orders, got $VIEWER_REMEDIATE_HTTP"
  exit 1
fi

VIEWER_POST_APPROVED_VOUCHERS_HTTP=$(curl -sS -o "$LOG_DIR/api-account-work-queue-post-approved-vouchers-viewer-forbidden.json" -w "%{http_code}" -X POST "$API_BASE/api/account/work-queue/actions/post-approved-vouchers" \
  "${VIEWER_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"portfolioId":1,"limit":20,"reason":"viewer forbidden"}')
if [[ "$VIEWER_POST_APPROVED_VOUCHERS_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for viewer on POST /api/account/work-queue/actions/post-approved-vouchers, got $VIEWER_POST_APPROVED_VOUCHERS_HTTP"
  exit 1
fi

VIEWER_APPROVE_DRAFT_VOUCHERS_HTTP=$(curl -sS -o "$LOG_DIR/api-account-work-queue-approve-draft-vouchers-viewer-forbidden.json" -w "%{http_code}" -X POST "$API_BASE/api/account/work-queue/actions/approve-draft-vouchers" \
  "${VIEWER_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"portfolioId":1,"limit":20,"reason":"viewer forbidden"}')
if [[ "$VIEWER_APPROVE_DRAFT_VOUCHERS_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for viewer on POST /api/account/work-queue/actions/approve-draft-vouchers, got $VIEWER_APPROVE_DRAFT_VOUCHERS_HTTP"
  exit 1
fi

VIEWER_PAUSE_TRADING_HTTP=$(curl -sS -o "$LOG_DIR/api-account-work-queue-pause-trading-viewer-forbidden.json" -w "%{http_code}" -X POST "$API_BASE/api/account/work-queue/actions/pause-trading" \
  "${VIEWER_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"portfolioId":1,"reason":"viewer forbidden"}')
if [[ "$VIEWER_PAUSE_TRADING_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for viewer on POST /api/account/work-queue/actions/pause-trading, got $VIEWER_PAUSE_TRADING_HTTP"
  exit 1
fi

VIEWER_EMERGENCY_RESPONSE_HTTP=$(curl -sS -o "$LOG_DIR/api-account-work-queue-emergency-risk-response-viewer-forbidden.json" -w "%{http_code}" -X POST "$API_BASE/api/account/work-queue/actions/emergency-risk-response" \
  "${VIEWER_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"portfolioId":1,"reason":"viewer forbidden","cancelOpenOrders":true}')
if [[ "$VIEWER_EMERGENCY_RESPONSE_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for viewer on POST /api/account/work-queue/actions/emergency-risk-response, got $VIEWER_EMERGENCY_RESPONSE_HTTP"
  exit 1
fi

VIEWER_RESUME_TRADING_HTTP=$(curl -sS -o "$LOG_DIR/api-account-work-queue-resume-trading-viewer-forbidden.json" -w "%{http_code}" -X POST "$API_BASE/api/account/work-queue/actions/resume-trading" \
  "${VIEWER_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"portfolioId":1,"reason":"viewer forbidden","force":false}')
if [[ "$VIEWER_RESUME_TRADING_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for viewer on POST /api/account/work-queue/actions/resume-trading, got $VIEWER_RESUME_TRADING_HTTP"
  exit 1
fi

TRADER_SHARED_VIEW_CREATE_HTTP=$(curl -sS -o "$LOG_DIR/api-saved-views-trader-shared-forbidden.json" -w "%{http_code}" -X POST "$API_BASE/api/saved-views" \
  "${TRADER_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"resourceKey":"orders","viewName":"trader-shared-forbidden","description":"forbidden","shared":true,"filters":{"status":"NEW"}}')
if [[ "$TRADER_SHARED_VIEW_CREATE_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for trader shared POST /api/saved-views, got $TRADER_SHARED_VIEW_CREATE_HTTP"
  exit 1
fi

TRADER_DELETE_ADMIN_VIEW_HTTP=$(curl -sS -o "$LOG_DIR/api-saved-views-trader-delete-foreign-forbidden.json" -w "%{http_code}" -X DELETE "$API_BASE/api/saved-views/$SAVED_VIEW_ID" \
  "${TRADER_AUTH[@]}")
if [[ "$TRADER_DELETE_ADMIN_VIEW_HTTP" != "403" ]]; then
  echo "[backend-openapi] expected 403 for trader DELETE /api/saved-views/{viewId} on foreign view, got $TRADER_DELETE_ADMIN_VIEW_HTTP"
  exit 1
fi

VIEWER_DEFAULT_PRIVATE_PIN_HTTP=$(curl -sS -o "$LOG_DIR/api-saved-views-default-private-forbidden.json" -w "%{http_code}" -X POST "$API_BASE/api/saved-views/default" \
  "${VIEWER_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"resourceKey":"orders","viewId":3}')
if [[ "$VIEWER_DEFAULT_PRIVATE_PIN_HTTP" != "400" ]]; then
  echo "[backend-openapi] expected 400 for viewer POST /api/saved-views/default with private view, got $VIEWER_DEFAULT_PRIVATE_PIN_HTTP"
  exit 1
fi

UNAUTHORIZED_HTTP=$(curl -sS -o "$LOG_DIR/api-users-unauthorized.json" -w "%{http_code}" "$API_BASE/api/users")
if [[ "$UNAUTHORIZED_HTTP" != "401" ]]; then
  echo "[backend-openapi] expected 401 without token on /api/users, got $UNAUTHORIZED_HTTP"
  exit 1
fi

curl -fsS -X POST "$API_BASE/api/account/change-password" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"currentPassword":"demo1234","newPassword":"Demo12345!"}' > "$LOG_DIR/api-account-change-password.json"
ACCOUNT_SESSIONS_JSON=$(curl -fsS "$API_BASE/api/account/sessions" "${ADMIN_AUTH[@]}")
echo "$ACCOUNT_SESSIONS_JSON" > "$LOG_DIR/api-account-sessions.json"
SESSION_ID=$(echo "$ACCOUNT_SESSIONS_JSON" | jq -r '.items[] | select(.active == true) | .sessionId' | head -n 1)
curl -fsS -X POST "$API_BASE/api/account/sessions/$SESSION_ID/revoke" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{}' > "$LOG_DIR/api-account-session-revoke.json"

VOUCHER_JSON=$(curl -fsS -X POST "$API_BASE/api/journal-vouchers" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d "{\"portfolioId\":1,\"tradeId\":$TRADE_ID,\"description\":\"backend-openapi-test\",\"entries\":[{\"accountCode\":\"STOCK_ASSET\",\"drCr\":\"DR\",\"amount\":421.00,\"symbol\":\"TSLA\"},{\"accountCode\":\"CASH\",\"drCr\":\"CR\",\"amount\":421.00}]}")
VOUCHER_ID=$(echo "$VOUCHER_JSON" | jq -r '.voucherId')

curl -fsS -X POST "$API_BASE/api/journal-vouchers/$VOUCHER_ID/approve" "${ADMIN_AUTH[@]}" > /dev/null
curl -fsS -X POST "$API_BASE/api/journal-vouchers/$VOUCHER_ID/post" "${ADMIN_AUTH[@]}" > /dev/null

VOUCHER_CANCEL_JSON=$(curl -fsS -X POST "$API_BASE/api/journal-vouchers" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"tradeId":null,"description":"cancel-test","entries":[{"accountCode":"STOCK_ASSET","drCr":"DR","amount":10.00,"symbol":"AAPL"},{"accountCode":"CASH","drCr":"CR","amount":10.00}]}')
VOUCHER_CANCEL_ID=$(echo "$VOUCHER_CANCEL_JSON" | jq -r '.voucherId')
curl -fsS -X POST "$API_BASE/api/journal-vouchers/$VOUCHER_CANCEL_ID/cancel" "${ADMIN_AUTH[@]}" > /dev/null

curl -fsS -X DELETE "$API_BASE/api/users/$USER_ID" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-users-delete.json"
curl -fsS -X DELETE "$API_BASE/api/menu-permissions/$MENU_PERMISSION_ID" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-menu-permissions-delete.json"
curl -fsS -X DELETE "$API_BASE/api/menus/$MENU_ID" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-menus-delete.json"
curl -fsS -X DELETE "$API_BASE/api/roles/$ROLE_ID" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-roles-delete.json"
curl -fsS -X DELETE "$API_BASE/api/saved-views/$SAVED_VIEW_ID" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-saved-views-delete.json"

ADV_FILTER_ORDER_JSON=$(curl -fsS -X POST "$API_BASE/api/orders" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"symbol":"ADVF","side":"BUY","orderType":"LIMIT","timeInForce":"DAY","limitPrice":140.25,"quantity":5}')
ADV_FILTER_ORDER_ID=$(echo "$ADV_FILTER_ORDER_JSON" | jq -r '.orderId')
curl -fsS -X POST "$API_BASE/api/trades/events" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d "{\"orderId\":$ADV_FILTER_ORDER_ID,\"tradeQuantity\":2,\"tradePrice\":140.25}" > "$LOG_DIR/api-orders-advanced-filter-trade.json"
curl -fsS "$API_BASE/api/orders" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-orders.json"
curl -fsS "$API_BASE/api/orders?portfolioId=1&symbol=ADVF&status=PARTIAL&side=BUY&orderType=LIMIT&timeInForce=DAY&minQuantity=5&maxQuantity=5&minRemainingQuantity=3&maxRemainingQuantity=3" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-orders-advanced-filter.json"
curl -fsS "$API_BASE/api/orders/$ORDER_ID/insight?staleMinutes=0" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-orders-insight-admin.json"
curl -fsS "$API_BASE/api/orders/$ORDER_ID/insight?staleMinutes=0" "${TRADER_AUTH[@]}" > "$LOG_DIR/api-orders-insight-trader.json"
curl -fsS "$API_BASE/api/orders/audit-logs?portfolioId=1" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-order-audits.json"
curl -fsS "$API_BASE/api/orders/audit-logs?portfolioId=1&actor=admin@quant.io" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-order-audits-admin-filter.json"
curl -fsS "$API_BASE/api/orders/audit-logs/summary?portfolioId=1&recentMinutes=180" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-order-audits-summary.json"
curl -fsS "$API_BASE/api/trades" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-trades.json"
curl -fsS "$API_BASE/api/positions?portfolioId=1" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-positions.json"
curl -fsS "$API_BASE/api/risk-limits" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-risk-limits.json"
curl -fsS "$API_BASE/api/risk-limits/trading-controls?portfolioId=1" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-trading-controls.json"
curl -fsS "$API_BASE/api/risk-alerts?portfolioId=1" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-risk-alerts.json"
curl -fsS "$API_BASE/api/risk-alerts/overview?portfolioId=1" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-risk-alerts-overview.json"
curl -fsS "$API_BASE/api/portfolios" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-portfolios.json"
curl -fsS "$API_BASE/api/search/global?q=order&limit=20" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-search-global-admin.json"
curl -fsS "$API_BASE/api/search/global?q=order&limit=20" "${VIEWER_AUTH[@]}" > "$LOG_DIR/api-search-global-viewer.json"
curl -fsS "$API_BASE/api/orders/workbench?portfolioId=1&staleMinutes=0&topN=6" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-orders-workbench.json"
curl -fsS "$API_BASE/api/saved-views?resourceKey=orders" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-saved-views.json"
curl -fsS "$API_BASE/api/order-health?portfolioId=1&staleMinutes=0" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-order-health.json"
curl -fsS -X POST "$API_BASE/api/order-health/remediate-stale" "${ADMIN_AUTH[@]}" -H 'Content-Type: application/json' -d '{"portfolioId":1,"symbol":"IWM","staleMinutes":0,"reason":"openapi stale remediation"}' > "$LOG_DIR/api-order-health-remediate.json"
curl -fsS "$API_BASE/api/execution-qualities?portfolioId=1" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-execution-qualities.json"
curl -fsS "$API_BASE/api/portfolio-summaries" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-portfolio-summaries.json"
curl -fsS "$API_BASE/api/portfolio-summaries/insight?portfolioId=1" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-portfolio-summaries-insight.json"
curl -fsS "$API_BASE/api/portfolio-summaries/profit-playbook?portfolioId=1" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-portfolio-profit-playbook.json"
curl -fsS "$API_BASE/api/portfolio-summaries/profit-playbook/feedback?portfolioId=1&limit=5" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-portfolio-profit-playbook-feedback.json"
curl -fsS "$API_BASE/api/users" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-users.json"
curl -fsS "$API_BASE/api/roles" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-roles.json"
curl -fsS "$API_BASE/api/menus" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-menus.json"
curl -fsS "$API_BASE/api/menu-permissions" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-menu-permissions.json"
curl -fsS "$API_BASE/api/journal-vouchers" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-journal-vouchers.json"
curl -fsS "$API_BASE/api/ledgers/entries?portfolioId=1" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-ledger-entries.json"
curl -fsS -X POST "$API_BASE/api/ledgers/validate" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-ledger-validate.json"
curl -fsS "$API_BASE/api/ledgers/validate/last" "${ADMIN_AUTH[@]}" > "$LOG_DIR/api-ledger-validate-last.json"

jq -e '.items | length > 0' "$LOG_DIR/api-order-audits.json" >/dev/null || {
  echo "[backend-openapi] expected order audit logs"
  exit 1
}

jq -e '.items[] | select((.actor // "") != "")' "$LOG_DIR/api-order-audits.json" >/dev/null || {
  echo "[backend-openapi] expected non-empty actor in audit logs"
  exit 1
}

jq -e '.items | length > 0' "$LOG_DIR/api-order-audits-admin-filter.json" >/dev/null || {
  echo "[backend-openapi] expected actor filtered order audit logs"
  exit 1
}

jq -e '.totalCount >= 1 and .recentMinutes == 180 and (.actionCounters | length >= 1) and (.transitionCounters | length >= 1)' "$LOG_DIR/api-order-audits-summary.json" >/dev/null || {
  echo "[backend-openapi] expected order audit summary payload"
  exit 1
}

jq -e '.items[] | select(.symbol == "ADVF" and .status == "PARTIAL" and .side == "BUY" and .orderType == "LIMIT" and .timeInForce == "DAY" and (.remainingQuantity | tonumber) == 3)' "$LOG_DIR/api-orders-advanced-filter.json" >/dev/null || {
  echo "[backend-openapi] expected advanced order filter response"
  exit 1
}

jq -e '.action == "CANCEL" and .requestedCount == 2 and .failedCount == 0' "$LOG_DIR/api-orders-bulk-cancel.json" >/dev/null || {
  echo "[backend-openapi] expected bulk cancel success response"
  exit 1
}

jq -e '.action == "REJECT" and .requestedCount == 2 and .failedCount == 0' "$LOG_DIR/api-orders-bulk-reject.json" >/dev/null || {
  echo "[backend-openapi] expected bulk reject success response"
  exit 1
}

jq -e 'all(.items[]; .actor == "admin@quant.io")' "$LOG_DIR/api-order-audits-admin-filter.json" >/dev/null || {
  echo "[backend-openapi] actor filter mismatch in order audit logs"
  exit 1
}

jq -e '.items[0].tradingEnabled == false' "$LOG_DIR/api-trading-controls-disable.json" >/dev/null || {
  echo "[backend-openapi] expected tradingEnabled=false after disable"
  exit 1
}

jq -e '.items[0].tradingEnabled == true' "$LOG_DIR/api-trading-controls-enable.json" >/dev/null || {
  echo "[backend-openapi] expected tradingEnabled=true after enable"
  exit 1
}

jq -e '.items | length > 0' "$LOG_DIR/api-risk-alerts.json" >/dev/null || {
  echo "[backend-openapi] expected risk alerts"
  exit 1
}

jq -e '(.items | length > 0) and (.items[0].totalCount >= 0) and (.items[0].criticalCount >= 0) and (.items[0].slaBreachedCount >= 0)' "$LOG_DIR/api-risk-alerts-overview.json" >/dev/null || {
  echo "[backend-openapi] expected risk alert overview rows"
  exit 1
}

jq -e '.items | length > 0' "$LOG_DIR/api-portfolios.json" >/dev/null || {
  echo "[backend-openapi] expected portfolio catalog"
  exit 1
}

jq -e '.sections | length > 0' "$LOG_DIR/api-search-global-admin.json" >/dev/null || {
  echo "[backend-openapi] expected global search sections for admin"
  exit 1
}

jq -e '.sections | all(.[]; .resourceKey != "orders")' "$LOG_DIR/api-search-global-viewer.json" >/dev/null || {
  echo "[backend-openapi] viewer global search should not include orders section"
  exit 1
}

jq -e '.orderId > 0 and (.trades | length >= 1) and (.audits | length >= 1)' "$LOG_DIR/api-orders-insight-admin.json" >/dev/null || {
  echo "[backend-openapi] expected order insight payload for admin"
  exit 1
}

jq -e '.orderId > 0 and (.riskAlerts | type == "array")' "$LOG_DIR/api-orders-insight-trader.json" >/dev/null || {
  echo "[backend-openapi] expected order insight payload for trader"
  exit 1
}

jq -e '.summary.openOrderCount >= 0 and (.statusCounters | length >= 1)' "$LOG_DIR/api-orders-workbench.json" >/dev/null || {
  echo "[backend-openapi] expected order workbench summary"
  exit 1
}

jq -e '.items | length > 0' "$LOG_DIR/api-saved-views.json" >/dev/null || {
  echo "[backend-openapi] expected saved views"
  exit 1
}

jq -e '.viewId > 0 and .resourceKey == "orders"' "$LOG_DIR/api-saved-views-default-pin.json" >/dev/null || {
  echo "[backend-openapi] expected saved view default pin response"
  exit 1
}

jq -e '.viewId > 0 and .resourceKey == "orders"' "$LOG_DIR/api-saved-views-default.json" >/dev/null || {
  echo "[backend-openapi] expected saved view default read response"
  exit 1
}

jq -e '.summary.portfolioId == 1 and (.tasks | type == "array") and (.alerts | type == "array")' "$LOG_DIR/api-account-work-queue-admin.json" >/dev/null || {
  echo "[backend-openapi] expected work queue response for admin"
  exit 1
}

jq -e '.summary.portfolioId == 1 and (.tasks | type == "array")' "$LOG_DIR/api-account-work-queue-viewer.json" >/dev/null || {
  echo "[backend-openapi] expected work queue response for viewer"
  exit 1
}

jq -e '.req.portfolioId == 1 and (.items | type == "array")' "$LOG_DIR/api-account-activity-feed-admin.json" >/dev/null || {
  echo "[backend-openapi] expected activity feed response for admin"
  exit 1
}

jq -e '.req.portfolioId == 1 and (.items | type == "array")' "$LOG_DIR/api-account-activity-feed-viewer.json" >/dev/null || {
  echo "[backend-openapi] expected activity feed response for viewer"
  exit 1
}

jq -e '.portfolioId == 1 and .staleThresholdMinutes >= 0 and .canceledCount >= 0' "$LOG_DIR/api-account-work-queue-remediate.json" >/dev/null || {
  echo "[backend-openapi] expected work queue stale remediation response"
  exit 1
}

jq -e '.revokedCount >= 0 and (.revokedSessionIds | type == "array")' "$LOG_DIR/api-account-work-queue-revoke-sessions.json" >/dev/null || {
  echo "[backend-openapi] expected work queue revoke-sessions response"
  exit 1
}

jq -e '.portfolioId == 1 and .attemptedCount >= 0 and .postedCount >= 0 and (.postedVoucherIds | type == "array") and (.failedReasons | type == "array")' "$LOG_DIR/api-account-work-queue-post-approved-vouchers.json" >/dev/null || {
  echo "[backend-openapi] expected work queue post-approved-vouchers response"
  exit 1
}

jq -e '.portfolioId == 1 and .attemptedCount >= 0 and .approvedCount >= 0 and (.approvedVoucherIds | type == "array") and (.failedReasons | type == "array")' "$LOG_DIR/api-account-work-queue-approve-draft-vouchers.json" >/dev/null || {
  echo "[backend-openapi] expected work queue approve-draft-vouchers response"
  exit 1
}

jq -e '.portfolioId == 1 and .tradingEnabled == false and (.killSwitchReason | length > 0)' "$LOG_DIR/api-account-work-queue-pause-trading.json" >/dev/null || {
  echo "[backend-openapi] expected work queue pause-trading response"
  exit 1
}

jq -e '.portfolioId == 1 and .tradingEnabled == false and .canceledCount >= 1 and (.canceledOrderIds | type == "array")' "$LOG_DIR/api-account-work-queue-emergency-risk-response.json" >/dev/null || {
  echo "[backend-openapi] expected work queue emergency-risk-response payload"
  exit 1
}

jq -e --argjson oid "$EMERGENCY_ORDER_ID" '.items[] | select(.orderId == $oid and .status == "CANCELED")' "$LOG_DIR/api-account-work-queue-emergency-order-after.json" >/dev/null || {
  echo "[backend-openapi] expected emergency order to be canceled"
  exit 1
}

jq -e '.portfolioId == 1 and .tradingEnabled == true and .resumed == true and .blockedCriticalCount == 0 and (.blockedCodes | type == "array") and (.blockedMessages | type == "array")' "$LOG_DIR/api-account-work-queue-resume-trading.json" >/dev/null || {
  echo "[backend-openapi] expected work queue resume-trading payload"
  exit 1
}

jq -e '.items | length > 0' "$LOG_DIR/api-order-health.json" >/dev/null || {
  echo "[backend-openapi] expected order health rows"
  exit 1
}

jq -e '.canceledCount >= 0' "$LOG_DIR/api-order-health-remediate.json" >/dev/null || {
  echo "[backend-openapi] expected remediation response"
  exit 1
}

jq -e '.items | length > 0' "$LOG_DIR/api-execution-qualities.json" >/dev/null || {
  echo "[backend-openapi] expected execution quality rows"
  exit 1
}

jq -e '(.items | length > 0) and (.items[0].healthScore >= 0) and (.items[0].healthScore <= 100) and ((.items[0].topExposures | type) == "array")' "$LOG_DIR/api-portfolio-summaries-insight.json" >/dev/null || {
  echo "[backend-openapi] expected portfolio summary insight rows"
  exit 1
}

jq -e '(.items | length > 0) and (.items[0].objective | type == "string") and ((.items[0].actions | type) == "array")' "$LOG_DIR/api-portfolio-profit-playbook.json" >/dev/null || {
  echo "[backend-openapi] expected portfolio profit playbook rows"
  exit 1
}

jq -e '(.items | length > 0) and (.items[0].actionKey | type == "string") and (.items[0].beforeSnapshot | type == "object") and (.items[0].afterSnapshot | type == "object") and (.items[0].outcomeEvaluation | type == "string")' "$LOG_DIR/api-portfolio-profit-playbook-feedback.json" >/dev/null || {
  echo "[backend-openapi] expected portfolio profit playbook feedback rows"
  exit 1
}

OPENAPI_ENDPOINTS=$(jq -r '.paths | to_entries[] | .key as $p | .value | to_entries[] | "\(.key|ascii_upcase) \($p)"' "$LOG_DIR/openapi.json" | sort)
TESTED_ENDPOINTS=$(cat <<TXT
POST /api/auth/login
GET /api/orders
POST /api/orders
GET /api/orders/{orderId}/insight
GET /api/orders/workbench
GET /api/orders/audit-logs/summary
POST /api/orders/bulk/cancel
POST /api/orders/bulk/reject
DELETE /api/orders/{orderId}
POST /api/orders/{orderId}/cancel
POST /api/orders/{orderId}/reject
GET /api/orders/audit-logs
GET /api/trades
POST /api/trades/events
GET /api/positions
GET /api/risk-limits
PUT /api/risk-limits
GET /api/risk-limits/trading-controls
PUT /api/risk-limits/trading-controls
GET /api/risk-limits/trading-controls/history
GET /api/risk-alerts
GET /api/risk-alerts/overview
POST /api/risk-alerts/ack
POST /api/risk-alerts/unack
POST /api/risk-alerts/workflow
GET /api/portfolios
GET /api/search/global
GET /api/order-health
POST /api/order-health/remediate-stale
GET /api/execution-qualities
GET /api/portfolio-summaries
GET /api/portfolio-summaries/insight
GET /api/portfolio-summaries/profit-playbook
GET /api/portfolio-summaries/profit-playbook/feedback
GET /api/users
POST /api/users
PUT /api/users/{userId}/status
PUT /api/users/{userId}/roles
POST /api/users/{userId}/reset-password
DELETE /api/users/{userId}
GET /api/roles
POST /api/roles
DELETE /api/roles/{roleId}
GET /api/menus
POST /api/menus
DELETE /api/menus/{menuId}
GET /api/saved-views
POST /api/saved-views
DELETE /api/saved-views/{viewId}
GET /api/saved-views/default
POST /api/saved-views/default
GET /api/menu-permissions
PUT /api/menu-permissions
DELETE /api/menu-permissions/{menuPermissionId}
GET /api/account/me
GET /api/account/menus
GET /api/account/work-queue
GET /api/account/activity-feed
POST /api/account/work-queue/actions/remediate-stale-orders
POST /api/account/work-queue/actions/revoke-other-sessions
POST /api/account/work-queue/actions/post-approved-vouchers
POST /api/account/work-queue/actions/approve-draft-vouchers
POST /api/account/work-queue/actions/pause-trading
POST /api/account/work-queue/actions/emergency-risk-response
POST /api/account/work-queue/actions/resume-trading
POST /api/account/change-password
GET /api/account/sessions
POST /api/account/sessions/{sessionId}/revoke
GET /api/journal-vouchers
POST /api/journal-vouchers
POST /api/journal-vouchers/{voucherId}/approve
POST /api/journal-vouchers/{voucherId}/post
POST /api/journal-vouchers/{voucherId}/cancel
GET /api/ledgers/entries
POST /api/ledgers/validate
GET /api/ledgers/validate/last
TXT
)

MISSING=$(comm -23 <(echo "$OPENAPI_ENDPOINTS") <(echo "$TESTED_ENDPOINTS" | sort) || true)
if [[ -n "$MISSING" ]]; then
  echo "[backend-openapi] missing endpoint tests:"
  echo "$MISSING"
  exit 1
fi

echo "[backend-openapi] PASS"
