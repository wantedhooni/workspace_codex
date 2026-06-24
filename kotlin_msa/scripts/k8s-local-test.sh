#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="${NAMESPACE:-commerce}"
SERVICE="${SERVICE:-api-gateway}"
LOCAL_PORT="${LOCAL_PORT:-18080}"
REMOTE_PORT="${REMOTE_PORT:-8080}"
ROOT_URL="http://127.0.0.1:${LOCAL_PORT}"
PORT_FORWARD_PID=""

usage() {
  cat <<'USAGE'
Usage:
  scripts/k8s-local-test.sh

Environment:
  NAMESPACE   테스트할 네임스페이스. 기본값 commerce
  SERVICE     포트포워딩할 서비스. 기본값 api-gateway
  LOCAL_PORT  로컬 포트. 기본값 18080
USAGE
}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "필수 명령을 찾을 수 없습니다: $1" >&2
    exit 1
  fi
}

cleanup() {
  if [[ -n "$PORT_FORWARD_PID" ]]; then
    kill "$PORT_FORWARD_PID" >/dev/null 2>&1 || true
    wait "$PORT_FORWARD_PID" >/dev/null 2>&1 || true
  fi
}

wait_for_http() {
  local url="$1"
  local attempts="${2:-60}"

  for _ in $(seq 1 "$attempts"); do
    if curl -fsS "$url" >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
  done

  echo "HTTP 응답 대기 실패: $url" >&2
  return 1
}

assert_contains() {
  local value="$1"
  local expected="$2"

  if [[ "$value" != *"$expected"* ]]; then
    echo "응답 검증 실패. 기대 문자열: $expected" >&2
    echo "응답: $value" >&2
    exit 1
  fi
}

start_port_forward() {
  echo "[test] svc/${SERVICE} ${LOCAL_PORT}:${REMOTE_PORT} 포트포워딩 시작"
  kubectl -n "$NAMESPACE" port-forward "svc/${SERVICE}" "${LOCAL_PORT}:${REMOTE_PORT}" >/tmp/kotlin-msa-port-forward.log 2>&1 &
  PORT_FORWARD_PID="$!"
  trap cleanup EXIT
  wait_for_http "${ROOT_URL}/actuator/health/readiness" 60
}

run_api_tests() {
  echo "[test] readiness 확인"
  local health
  health="$(curl -fsS "${ROOT_URL}/actuator/health/readiness")"
  assert_contains "$health" "UP"

  echo "[test] 상품 목록 조회"
  local products
  products="$(curl -fsS "${ROOT_URL}/api/products")"
  assert_contains "$products" "SKU-001"
  assert_contains "$products" "무선 키보드"

  echo "[test] 재고 조회"
  local inventory
  inventory="$(curl -fsS "${ROOT_URL}/api/inventories/SKU-001")"
  assert_contains "$inventory" "availableQuantity"

  echo "[test] 주문 생성 성공"
  local order
  order="$(curl -fsS -X POST "${ROOT_URL}/api/orders" \
    -H 'Content-Type: application/json' \
    -d '{
      "customerId": "CUST-001",
      "items": [
        {
          "sku": "SKU-001",
          "quantity": 1,
          "unitPrice": 129000
        }
      ],
      "paymentMethod": "CARD"
    }')"
  assert_contains "$order" "CREATED"
  assert_contains "$order" "paymentId"

  echo "[test] 주문 생성 실패 케이스 확인"
  local failed_order
  failed_order="$(curl -sS -X POST "${ROOT_URL}/api/orders" \
    -H 'Content-Type: application/json' \
    -d '{
      "customerId": "CUST-001",
      "items": [
        {
          "sku": "SKU-001",
          "quantity": 1,
          "unitPrice": 1000001
        }
      ],
      "paymentMethod": "CARD"
    }')"
  assert_contains "$failed_order" "결제"

  echo "[test] 통과"
}

main() {
  if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    usage
    exit 0
  fi

  require_command kubectl
  require_command curl

  kubectl -n "$NAMESPACE" get svc "$SERVICE" >/dev/null
  start_port_forward
  run_api_tests
}

main "$@"
