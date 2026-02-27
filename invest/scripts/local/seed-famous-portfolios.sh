#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

API_BASE_URL="${API_BASE_URL:-http://127.0.0.1:8080/api/v1}"
API_USER="${API_USER:-${APP_ADMIN_USERNAME:-admin}}"
API_PASSWORD="${API_PASSWORD:-${APP_ADMIN_PASSWORD:-admin1234}}"
SEED_CAPITAL="${SEED_CAPITAL:-1000000}"
TOP_N="${TOP_N:-10}"

SOURCE_BUFFETT_URL="https://www.dataroma.com/m/holdings.php?m=BRK"
SOURCE_ARKK_URL="https://stockanalysis.com/etf/arkk/holdings/"
SOURCE_PELOSI_URL="https://www.quiverquant.com/get_politician_page_tab_data/P000197"
SOURCE_PRICE_URL_BASE="https://stooq.com/q/l/"

TMP_DIR=""
BUFFETT_DATE=""
ARKK_DATE=""
PELOSI_DATE=""

cleanup() {
  if [ -n "${TMP_DIR}" ] && [ -d "${TMP_DIR}" ]; then
    rm -rf "${TMP_DIR}"
  fi
}

trap cleanup EXIT

log() {
  printf '[seed-famous] %s\n' "$1"
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Required command not found: $1" >&2
    exit 1
  fi
}

api_get() {
  local path="$1"
  shift
  curl -fsS -u "${API_USER}:${API_PASSWORD}" "${API_BASE_URL}${path}" "$@"
}

api_post_json() {
  local path="$1"
  local payload="$2"
  curl -fsS -u "${API_USER}:${API_PASSWORD}" \
    -H "Content-Type: application/json" \
    -X POST \
    "${API_BASE_URL}${path}" \
    -d "${payload}"
}

to_iso_date() {
  local raw_date="$1"
  node -e '
    const raw = process.argv[1];
    const date = new Date(raw + " UTC");
    if (Number.isNaN(date.getTime())) {
      process.exit(1);
    }
    process.stdout.write(date.toISOString().slice(0, 10));
  ' "${raw_date}"
}

fetch_buffett_holdings() {
  local out_file="${TMP_DIR}/buffett.html"
  local raw_date

  curl -fsS "${SOURCE_BUFFETT_URL}" > "${out_file}"

  raw_date="$(
    perl -0777 -ne 'if(/Portfolio date:\s*<span>([^<]+)<\/span>/s){print $1; exit}' "${out_file}"
  )"
  BUFFETT_DATE="$(to_iso_date "${raw_date}")"

  perl -0777 -ne '
    while(/<td class="stock"><a href="\/m\/stock\.php\?sym=([A-Z.\-]+)">[^<]*<span>\s*-\s*([^<]+)<\/span><\/a><\/td>\s*<td>([0-9.]+)<\/td>/sg){
      print "$1\t$2\t$3\n";
    }
  ' "${out_file}" | head -n "${TOP_N}"
}

fetch_arkk_holdings() {
  local out_file="${TMP_DIR}/arkk.html"
  local raw_date

  curl -fsS "${SOURCE_ARKK_URL}" > "${out_file}"

  raw_date="$(
    perl -0777 -ne 'if(/date:"([A-Za-z]{3}\s+[0-9]{1,2},\s+[0-9]{4})"/){print $1; exit}' "${out_file}"
  )"
  ARKK_DATE="$(to_iso_date "${raw_date}")"

  perl -0777 -ne '
    while(/n:"([^"]+)",s:"\$([A-Z.]+)",as:"([0-9.]+)%"/g){
      print "$2\t$1\t$3\n";
    }
  ' "${out_file}" | head -n "${TOP_N}"
}

fetch_pelosi_holdings() {
  local out_file="${TMP_DIR}/pelosi.json"

  curl -fsS "${SOURCE_PELOSI_URL}" \
    -H "X-Requested-With: XMLHttpRequest" \
    -H "Accept: application/json" \
    > "${out_file}"

  PELOSI_DATE="$(date +%F)"

  jq -r --argjson topN "${TOP_N}" '
    .live_stock_portfolio.live_stock_portfolio
    | fromjson
    | sort_by(.[2]) | reverse
    | .[:$topN]
    | .[]
    | "\(.[0])\t\(.[0])\t\((.[2] * 100))"
  ' "${out_file}"
}

find_portfolio_id_by_name() {
  local portfolio_name="$1"
  local filter_json
  local response

  filter_json="$(jq -cn --arg keyword "${portfolio_name}" '{keyword: $keyword}')"
  response="$(
    api_get "/portfolios" \
      --get \
      --data-urlencode "page=1" \
      --data-urlencode "perPage=100" \
      --data-urlencode "filter=${filter_json}"
  )"

  echo "${response}" | jq -r --arg name "${portfolio_name}" '.data[] | select(.name == $name) | .id' | head -n 1
}

ensure_portfolio() {
  local portfolio_name="$1"
  local existing_id
  local payload
  local response

  existing_id="$(find_portfolio_id_by_name "${portfolio_name}")"
  if [ -n "${existing_id}" ]; then
    echo "${existing_id}"
    return
  fi

  payload="$(jq -cn --arg name "${portfolio_name}" '{name: $name, baseCurrency: "USD"}')"
  response="$(api_post_json "/portfolios" "${payload}")"
  echo "${response}" | jq -r '.data.id'
}

find_instrument_id_by_ticker() {
  local ticker="$1"
  local filter_json
  local response

  filter_json="$(jq -cn --arg keyword "${ticker}" --arg marketCode "US" '{keyword: $keyword, marketCode: $marketCode}')"
  response="$(
    api_get "/instruments" \
      --get \
      --data-urlencode "page=1" \
      --data-urlencode "perPage=100" \
      --data-urlencode "filter=${filter_json}"
  )"

  echo "${response}" | jq -r --arg ticker "${ticker}" '.data[] | select(.ticker == $ticker) | .id' | head -n 1
}

ensure_instrument() {
  local ticker="$1"
  local instrument_name="$2"
  local existing_id
  local payload
  local response

  existing_id="$(find_instrument_id_by_ticker "${ticker}")"
  if [ -n "${existing_id}" ]; then
    echo "${existing_id}"
    return
  fi

  payload="$(
    jq -cn \
      --arg ticker "${ticker}" \
      --arg name "${instrument_name}" \
      '{
        ticker: $ticker,
        name: $name,
        marketCode: "US",
        currencyCode: "USD"
      }'
  )"
  response="$(api_post_json "/instruments" "${payload}")"
  echo "${response}" | jq -r '.data.id'
}

portfolio_transaction_total() {
  local portfolio_id="$1"
  local filter_json
  local response

  filter_json="$(jq -cn --argjson portfolioId "${portfolio_id}" '{portfolioId: $portfolioId}')"
  response="$(
    api_get "/transactions" \
      --get \
      --data-urlencode "page=1" \
      --data-urlencode "perPage=1" \
      --data-urlencode "filter=${filter_json}"
  )"
  echo "${response}" | jq -r '.total'
}

fetch_stooq_close_price() {
  local ticker="$1"
  local ticker_lower
  local csv
  local close_price

  ticker_lower="$(printf '%s' "${ticker}" | tr '[:upper:]' '[:lower:]')"
  csv="$(curl -fsS "${SOURCE_PRICE_URL_BASE}?s=${ticker_lower}.us&i=d" || true)"
  close_price="$(printf '%s\n' "${csv}" | awk -F',' 'NR==1 {print $7}')"

  if [ -z "${close_price}" ] || [ "${close_price}" = "N/D" ]; then
    return 1
  fi

  printf '%s' "${close_price}"
}

create_deposit_transaction() {
  local portfolio_id="$1"
  local trade_date="$2"
  local memo="$3"
  local payload

  payload="$(
    jq -cn \
      --argjson portfolioId "${portfolio_id}" \
      --arg tradeDate "${trade_date}" \
      --arg memo "${memo}" \
      --argjson amount "${SEED_CAPITAL}" \
      '{
        portfolioId: $portfolioId,
        transactionType: "DEPOSIT",
        tradeDate: $tradeDate,
        amount: $amount,
        fee: 0,
        tax: 0,
        currencyCode: "USD",
        memo: $memo
      }'
  )"
  api_post_json "/transactions" "${payload}" >/dev/null
}

create_buy_transaction() {
  local portfolio_id="$1"
  local instrument_id="$2"
  local trade_date="$3"
  local quantity="$4"
  local unit_price="$5"
  local memo="$6"
  local payload

  payload="$(
    jq -cn \
      --argjson portfolioId "${portfolio_id}" \
      --argjson instrumentId "${instrument_id}" \
      --arg tradeDate "${trade_date}" \
      --arg quantity "${quantity}" \
      --arg unitPrice "${unit_price}" \
      --arg memo "${memo}" \
      '{
        portfolioId: $portfolioId,
        instrumentId: $instrumentId,
        transactionType: "BUY",
        tradeDate: $tradeDate,
        quantity: ($quantity | tonumber),
        unitPrice: ($unitPrice | tonumber),
        fee: 0,
        tax: 0,
        currencyCode: "USD",
        memo: $memo
      }'
  )"
  api_post_json "/transactions" "${payload}" >/dev/null
}

seed_portfolio() {
  local portfolio_name="$1"
  local trade_date="$2"
  local source_label="$3"
  local source_url="$4"
  local holdings_lines="$5"
  local portfolio_id
  local existing_transactions

  portfolio_id="$(ensure_portfolio "${portfolio_name}")"
  existing_transactions="$(portfolio_transaction_total "${portfolio_id}")"
  if [ "${existing_transactions}" != "0" ]; then
    log "skip ${portfolio_name}: transactions already exist (total=${existing_transactions})"
    return
  fi

  create_deposit_transaction \
    "${portfolio_id}" \
    "${trade_date}" \
    "[seed] ${source_label} source=${source_url} deposit=${SEED_CAPITAL}USD"

  printf '%s\n' "${holdings_lines}" | while IFS=$'\t' read -r ticker instrument_name weight_percent; do
    local instrument_id
    local close_price
    local quantity
    local memo

    if [ -z "${ticker}" ] || [ -z "${weight_percent}" ]; then
      continue
    fi

    instrument_id="$(ensure_instrument "${ticker}" "${instrument_name}")"

    close_price="$(fetch_stooq_close_price "${ticker}" || true)"
    if [ -z "${close_price}" ]; then
      log "skip ${portfolio_name}/${ticker}: no close price from stooq"
      continue
    fi

    quantity="$(
      awk -v capital="${SEED_CAPITAL}" -v weight="${weight_percent}" -v price="${close_price}" '
        BEGIN {
          if (price <= 0) {
            print "0";
            exit;
          }
          q = (capital * weight / 100.0) / price;
          if (q <= 0) {
            print "0";
          } else {
            printf "%.8f", q;
          }
        }
      '
    )"

    if ! awk -v q="${quantity}" 'BEGIN { exit (q > 0) ? 0 : 1 }'; then
      log "skip ${portfolio_name}/${ticker}: computed quantity is zero"
      continue
    fi

    memo="[seed] ${source_label} weight=${weight_percent}% source=${source_url} priceSource=stooq"
    create_buy_transaction "${portfolio_id}" "${instrument_id}" "${trade_date}" "${quantity}" "${close_price}" "${memo}"
    log "inserted ${portfolio_name}: ${ticker} qty=${quantity} price=${close_price}"
  done
}

main() {
  require_cmd curl
  require_cmd jq
  require_cmd perl
  require_cmd node
  require_cmd awk

  TMP_DIR="$(mktemp -d)"

  api_get "/public/ping" >/dev/null

  local buffett_holdings
  local arkk_holdings
  local pelosi_holdings
  local buffett_holdings_file="${TMP_DIR}/buffett_holdings.tsv"
  local arkk_holdings_file="${TMP_DIR}/arkk_holdings.tsv"
  local pelosi_holdings_file="${TMP_DIR}/pelosi_holdings.tsv"

  fetch_buffett_holdings > "${buffett_holdings_file}"
  fetch_arkk_holdings > "${arkk_holdings_file}"
  fetch_pelosi_holdings > "${pelosi_holdings_file}"

  buffett_holdings="$(cat "${buffett_holdings_file}")"
  arkk_holdings="$(cat "${arkk_holdings_file}")"
  pelosi_holdings="$(cat "${pelosi_holdings_file}")"

  log "source dates: Buffett=${BUFFETT_DATE}, ARKK=${ARKK_DATE}, PelosiLive=${PELOSI_DATE}"

  seed_portfolio \
    "Warren Buffett (Dataroma)" \
    "${BUFFETT_DATE}" \
    "Warren Buffett - Berkshire Hathaway" \
    "${SOURCE_BUFFETT_URL}" \
    "${buffett_holdings}"

  seed_portfolio \
    "Cathie Wood - ARKK (StockAnalysis)" \
    "${ARKK_DATE}" \
    "Cathie Wood - ARKK Holdings" \
    "${SOURCE_ARKK_URL}" \
    "${arkk_holdings}"

  seed_portfolio \
    "Nancy Pelosi (QuiverQuant Live)" \
    "${PELOSI_DATE}" \
    "Nancy Pelosi Live Stock Portfolio" \
    "https://www.quiverquant.com/congresstrading/politician/Nancy%20Pelosi-P000197" \
    "${pelosi_holdings}"

  log "done"
}

main "$@"
