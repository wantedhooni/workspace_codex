#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:8088}"

login_token() {
  local email="$1"
  local password="$2"
  curl -fsS -X POST "$BASE_URL/api/auth/login" \
    -H 'Content-Type: application/json' \
    -d "{\"email\":\"$email\",\"password\":\"$password\"}" | jq -r '.accessToken'
}

echo "[smoke] health"
curl -fsS "$BASE_URL/actuator/health" > /dev/null

echo "[smoke] prometheus"
curl -fsS "$BASE_URL/actuator/prometheus" > /dev/null

echo "[smoke] login"
ADMIN_TOKEN="$(login_token 'admin@quant.io' 'demo1234')"
ADMIN_AUTH=(-H "Authorization: Bearer $ADMIN_TOKEN")

echo "[smoke] create order/trade + voucher flow"
ORDER_JSON=$(curl -fsS -X POST "$BASE_URL/api/orders" \
  "${ADMIN_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"portfolioId":1,"symbol":"AAPL","side":"BUY","quantity":1}')
ORDER_ID=$(echo "$ORDER_JSON" | jq -r '.orderId')

TRADE_JSON=$(curl -fsS -X POST "$BASE_URL/api/trades/events" \
  "${ADMIN_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d "{\"orderId\":$ORDER_ID,\"tradeQuantity\":1,\"tradePrice\":10.00}")
TRADE_ID=$(echo "$TRADE_JSON" | jq -r '.tradeId')

VOUCHER_JSON=$(curl -fsS -X POST "$BASE_URL/api/journal-vouchers" \
  "${ADMIN_AUTH[@]}" \
  -H 'Content-Type: application/json' \
  -d "{\"portfolioId\":1,\"tradeId\":$TRADE_ID,\"description\":\"smoke\",\"entries\":[{\"accountCode\":\"STOCK_ASSET\",\"drCr\":\"DR\",\"amount\":10.00,\"symbol\":\"AAPL\"},{\"accountCode\":\"CASH\",\"drCr\":\"CR\",\"amount\":10.00}]}")
VOUCHER_ID=$(echo "$VOUCHER_JSON" | jq -r '.voucherId')

curl -fsS -X POST "$BASE_URL/api/journal-vouchers/${VOUCHER_ID}/approve" "${ADMIN_AUTH[@]}" > /dev/null
curl -fsS -X POST "$BASE_URL/api/journal-vouchers/${VOUCHER_ID}/post" "${ADMIN_AUTH[@]}" > /dev/null
curl -fsS -X POST "$BASE_URL/api/ledgers/validate" "${ADMIN_AUTH[@]}" > /tmp/validate.json

jq -e '.balanced == true' /tmp/validate.json > /dev/null

echo "[smoke] done"
