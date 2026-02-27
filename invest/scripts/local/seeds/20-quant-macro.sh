#!/usr/bin/env bash
set -euo pipefail

API_BASE_URL="${API_BASE_URL:-http://127.0.0.1:8080/api/v1}"
API_USER="${API_USER:-${APP_ADMIN_USERNAME:-admin}}"
API_PASSWORD="${API_PASSWORD:-${APP_ADMIN_PASSWORD:-admin1234}}"
SIGNAL_DATE_OVERRIDE="${SEED_SIGNAL_DATE:-}"
MACRO_DATE_OVERRIDE="${SEED_MACRO_DATE:-}"
SIGNAL_TOP_N="${SEED_SIGNAL_TOP_N:-3}"
REPLACE_SIGNALS="${SEED_REPLACE_SIGNALS:-true}"
REPLACE_MACRO_INDICATORS="${SEED_REPLACE_MACRO_INDICATORS:-true}"

SOURCE_STOOQ_QUOTE_URL_BASE="https://stooq.com/q/l/"
SOURCE_STOOQ_HISTORY_URL_BASE="https://stooq.com/q/d/l/"
SOURCE_FRED_CSV_URL_BASE="https://fred.stlouisfed.org/graph/fredgraph.csv"

TMP_DIR=""

cleanup() {
  if [ -n "${TMP_DIR}" ] && [ -d "${TMP_DIR}" ]; then
    rm -rf "${TMP_DIR}"
  fi
}

trap cleanup EXIT

log() {
  printf '[seed-quant-macro] %s\n' "$1"
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

api_put_json() {
  local path="$1"
  local payload="$2"
  curl -fsS -u "${API_USER}:${API_PASSWORD}" \
    -H "Content-Type: application/json" \
    -X PUT \
    "${API_BASE_URL}${path}" \
    -d "${payload}"
}

api_delete() {
  local path="$1"
  curl -fsS -u "${API_USER}:${API_PASSWORD}" \
    -X DELETE \
    "${API_BASE_URL}${path}" >/dev/null
}

find_strategy_id_by_name() {
  local strategy_name="$1"
  local filter_json
  local response

  filter_json="$(jq -cn --arg keyword "${strategy_name}" '{keyword: $keyword}')"
  response="$(
    api_get "/quant-strategies" \
      --get \
      --data-urlencode "page=1" \
      --data-urlencode "perPage=100" \
      --data-urlencode "filter=${filter_json}"
  )"

  echo "${response}" | jq -r --arg name "${strategy_name}" '.data[] | select(.name == $name) | .id' | head -n 1
}

ensure_strategy() {
  local strategy_name="$1"
  local style="$2"
  local status="$3"
  local cycle_days="$4"
  local description="$5"
  local strategy_id
  local payload
  local response

  strategy_id="$(find_strategy_id_by_name "${strategy_name}")"
  if [ -n "${strategy_id}" ]; then
    payload="$(
      jq -cn \
        --arg name "${strategy_name}" \
        --arg style "${style}" \
        --arg status "${status}" \
        --argjson rebalanceCycleDays "${cycle_days}" \
        --arg description "${description}" \
        '{
          name: $name,
          style: $style,
          status: $status,
          rebalanceCycleDays: $rebalanceCycleDays,
          description: $description
        }'
    )"
    api_put_json "/quant-strategies/${strategy_id}" "${payload}" >/dev/null
    echo "${strategy_id}"
    return
  fi

  payload="$(
    jq -cn \
      --arg name "${strategy_name}" \
      --arg style "${style}" \
      --arg status "${status}" \
      --argjson rebalanceCycleDays "${cycle_days}" \
      --arg description "${description}" \
      '{
        name: $name,
        style: $style,
        status: $status,
        rebalanceCycleDays: $rebalanceCycleDays,
        description: $description
      }'
  )"

  response="$(api_post_json "/quant-strategies" "${payload}")"
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

fetch_top_portfolio_holdings() {
  local portfolio_id="$1"
  local top_n="$2"
  local filter_json
  local response

  filter_json="$(jq -cn --argjson portfolioId "${portfolio_id}" '{portfolioId: $portfolioId}')"
  response="$(
    api_get "/holdings" \
      --get \
      --data-urlencode "page=1" \
      --data-urlencode "perPage=${top_n}" \
      --data-urlencode 'sort=["quantity","DESC"]' \
      --data-urlencode "filter=${filter_json}"
  )"

  echo "${response}" | jq -r '.data[] | "\(.ticker)\t\(.instrumentId)"'
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

resolve_observed_date() {
  local source_date="$1"
  local override_date="$2"

  if [ -n "${override_date}" ]; then
    echo "${override_date}"
    return
  fi

  echo "${source_date}"
}

signal_exists_for_strategy_date() {
  local strategy_id="$1"
  local signal_date="$2"
  local filter_json
  local response

  if [ -z "${strategy_id}" ] || [ -z "${signal_date}" ]; then
    return 1
  fi

  filter_json="$(
    jq -cn \
      --argjson strategyId "${strategy_id}" \
      --arg fromDate "${signal_date}" \
      --arg toDate "${signal_date}" \
      '{strategyId: $strategyId, fromDate: $fromDate, toDate: $toDate}'
  )"

  response="$(
    api_get "/quant-signals" \
      --get \
      --data-urlencode "page=1" \
      --data-urlencode "perPage=1" \
      --data-urlencode "filter=${filter_json}"
  )"

  [ "$(echo "${response}" | jq -r '.total')" != "0" ]
}

list_signal_ids_for_strategy_date() {
  local strategy_id="$1"
  local signal_date="$2"
  local filter_json
  local response

  if [ -z "${strategy_id}" ] || [ -z "${signal_date}" ]; then
    return
  fi

  filter_json="$(
    jq -cn \
      --argjson strategyId "${strategy_id}" \
      --arg fromDate "${signal_date}" \
      --arg toDate "${signal_date}" \
      '{strategyId: $strategyId, fromDate: $fromDate, toDate: $toDate}'
  )"

  response="$(
    api_get "/quant-signals" \
      --get \
      --data-urlencode "page=1" \
      --data-urlencode "perPage=200" \
      --data-urlencode "filter=${filter_json}"
  )"

  echo "${response}" | jq -r '.data[].id'
}

replace_existing_signals_for_date() {
  local strategy_id="$1"
  local signal_date="$2"
  local signal_id

  while IFS= read -r signal_id; do
    if [ -z "${signal_id}" ]; then
      continue
    fi
    api_delete "/quant-signals/${signal_id}"
    log "deleted existing signal id=${signal_id} strategyId=${strategy_id} date=${signal_date}"
  done < <(list_signal_ids_for_strategy_date "${strategy_id}" "${signal_date}")
}

normalize_stooq_date() {
  local raw_date="$1"
  if [ "${#raw_date}" -ne 8 ]; then
    return 1
  fi
  printf '%s-%s-%s' "${raw_date:0:4}" "${raw_date:4:2}" "${raw_date:6:2}"
}

fetch_stooq_history_metrics() {
  local ticker="$1"
  local history_file="${TMP_DIR}/stooq-${ticker}-history.csv"
  local stooq_symbol

  stooq_symbol="$(printf '%s' "${ticker}" | tr '[:upper:]' '[:lower:]').us"
  curl -fsS "${SOURCE_STOOQ_HISTORY_URL_BASE}?s=${stooq_symbol}&i=d" > "${history_file}"

  awk -F',' '
    NR == 1 { next }
    $1 ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/ && $5 != "" && $5 != "N/D" {
      n++
      date[n] = $1
      price[n] = $5 + 0
      vol[n] = ($6 == "" || $6 == "N/D") ? 0 : $6 + 0
    }
    END {
      if (n < 61) {
        exit 1
      }
      latestClose = price[n]
      close20 = price[n - 20]
      close60 = price[n - 60]
      if (close20 <= 0 || close60 <= 0) {
        exit 1
      }
      ret20 = ((latestClose / close20) - 1) * 100
      ret60 = ((latestClose / close60) - 1) * 100

      start = n - 19
      if (start < 1) {
        start = 1
      }
      sumVol = 0
      count = 0
      for (i = start; i <= n; i++) {
        sumVol += vol[i]
        count++
      }
      avgVol = count > 0 ? (sumVol / count) : 0

      printf "%s\t%.6f\t%.6f\t%.2f\t%.6f\n", date[n], ret20, ret60, avgVol, latestClose
    }
  ' "${history_file}"
}

derive_signal_type() {
  local ret20="$1"
  local ret60="$2"

  awk -v r20="${ret20}" -v r60="${ret60}" '
    BEGIN {
      if (r20 >= 8 && r60 >= 15) {
        print "BUY"
      } else if (r20 >= 3 && r60 >= 6) {
        print "OVERWEIGHT"
      } else if (r20 <= -8 && r60 <= -12) {
        print "SELL"
      } else if (r20 <= -3 && r60 <= -6) {
        print "UNDERWEIGHT"
      } else {
        print "HOLD"
      }
    }
  '
}

derive_signal_score() {
  local ret20="$1"
  local ret60="$2"

  awk -v r20="${ret20}" -v r60="${ret60}" '
    BEGIN {
      score = 0.5 + (r20 * 0.015) + (r60 * 0.005)
      if (score < 0.05) score = 0.05
      if (score > 0.99) score = 0.99
      printf "%.4f", score
    }
  '
}

derive_signal_confidence() {
  local ret20="$1"
  local ret60="$2"
  local avg_vol20="$3"

  awk -v r20="${ret20}" -v r60="${ret60}" -v v20="${avg_vol20}" '
    function abs(x) { return x < 0 ? -x : x }
    BEGIN {
      momentum = (abs(r20) * 0.6) + (abs(r60) * 0.4)
      confidence = 0.55 + (momentum / 120.0)
      if (v20 >= 20000000) {
        confidence += 0.15
      } else if (v20 >= 5000000) {
        confidence += 0.08
      } else {
        confidence += 0.03
      }

      if (confidence < 0.55) confidence = 0.55
      if (confidence > 0.95) confidence = 0.95

      printf "%.4f", confidence
    }
  '
}

build_signal_rationale() {
  local ret20="$1"
  local ret60="$2"
  local latest_close="$3"
  local avg_vol20="$4"
  local signal_date="$5"

  awk -v r20="${ret20}" -v r60="${ret60}" -v px="${latest_close}" -v vol20="${avg_vol20}" -v d="${signal_date}" '
    BEGIN {
      printf "Stooq EOD %s 기준: 20D %.2f%% / 60D %.2f%%, 종가 %.2f USD, 20D 평균거래량 %.0f주", d, r20, r60, px, vol20
    }
  '
}

create_signal() {
  local strategy_id="$1"
  local instrument_id="$2"
  local signal_type="$3"
  local signal_date="$4"
  local score="$5"
  local confidence="$6"
  local rationale="$7"
  local payload

  if [ -z "${strategy_id}" ] || [ -z "${instrument_id}" ]; then
    return 1
  fi

  payload="$(
    jq -cn \
      --argjson strategyId "${strategy_id}" \
      --argjson instrumentId "${instrument_id}" \
      --arg signalType "${signal_type}" \
      --arg signalDate "${signal_date}" \
      --arg score "${score}" \
      --arg confidence "${confidence}" \
      --arg rationale "${rationale}" \
      '{
        strategyId: $strategyId,
        instrumentId: $instrumentId,
        signalType: $signalType,
        signalDate: $signalDate,
        score: ($score | tonumber),
        confidence: ($confidence | tonumber),
        rationale: $rationale
      }'
  )"

  api_post_json "/quant-signals" "${payload}" >/dev/null
}

seed_strategy_signals_from_portfolio() {
  local strategy_id="$1"
  local strategy_name="$2"
  local portfolio_name="$3"
  local fallback_rows="$4"
  local holdings_rows
  local portfolio_id
  local signal_rows_file
  local strategy_signal_date=""
  local inserted=0

  signal_rows_file="${TMP_DIR}/signals-${strategy_id}.tsv"
  : > "${signal_rows_file}"

  portfolio_id="$(find_portfolio_id_by_name "${portfolio_name}")"
  if [ -n "${portfolio_id}" ]; then
    holdings_rows="$(fetch_top_portfolio_holdings "${portfolio_id}" "${SIGNAL_TOP_N}" || true)"
  else
    holdings_rows=""
  fi

  if [ -z "${holdings_rows}" ]; then
    log "fallback universe for ${strategy_name}: portfolio holdings unavailable (${portfolio_name})"
    holdings_rows="${fallback_rows}"
  fi

  while IFS=$'\t' read -r ticker instrument_id instrument_name; do
    local metrics
    local latest_date
    local ret20
    local ret60
    local avg_vol20
    local latest_close
    local signal_type
    local score
    local confidence
    local rationale

    if [ -z "${ticker}" ]; then
      continue
    fi

    metrics="$(fetch_stooq_history_metrics "${ticker}" || true)"
    if [ -z "${metrics}" ]; then
      log "skip ${strategy_name}/${ticker}: insufficient stooq history"
      continue
    fi

    latest_date="$(echo "${metrics}" | cut -f1)"
    ret20="$(echo "${metrics}" | cut -f2)"
    ret60="$(echo "${metrics}" | cut -f3)"
    avg_vol20="$(echo "${metrics}" | cut -f4)"
    latest_close="$(echo "${metrics}" | cut -f5)"

    if [ -z "${strategy_signal_date}" ]; then
      strategy_signal_date="$(resolve_observed_date "${latest_date}" "${SIGNAL_DATE_OVERRIDE}")"
    fi

    if [ -z "${instrument_id}" ] || [ "${instrument_id}" = "null" ]; then
      instrument_id="$(ensure_instrument "${ticker}" "${instrument_name:-${ticker}}")"
    fi

    signal_type="$(derive_signal_type "${ret20}" "${ret60}")"
    score="$(derive_signal_score "${ret20}" "${ret60}")"
    confidence="$(derive_signal_confidence "${ret20}" "${ret60}" "${avg_vol20}")"
    rationale="$(build_signal_rationale "${ret20}" "${ret60}" "${latest_close}" "${avg_vol20}" "${strategy_signal_date}")"

    printf '%s\t%s\t%s\t%s\t%s\t%s\n' \
      "${instrument_id}" \
      "${ticker}" \
      "${signal_type}" \
      "${score}" \
      "${confidence}" \
      "${rationale}" >> "${signal_rows_file}"
  done <<< "${holdings_rows}"

  if [ ! -s "${signal_rows_file}" ]; then
    log "skip ${strategy_name}: no signal candidates created"
    return
  fi

  if [ -z "${strategy_signal_date}" ]; then
    strategy_signal_date="$(resolve_observed_date "$(date +%F)" "${SIGNAL_DATE_OVERRIDE}")"
  fi

  if signal_exists_for_strategy_date "${strategy_id}" "${strategy_signal_date}"; then
    if [ "${REPLACE_SIGNALS}" = "true" ]; then
      log "${strategy_name}: replacing existing signals for ${strategy_signal_date}"
      replace_existing_signals_for_date "${strategy_id}" "${strategy_signal_date}"
    else
      log "skip ${strategy_name}: signals already exist for ${strategy_signal_date}"
      return
    fi
  fi

  while IFS=$'\t' read -r instrument_id ticker signal_type score confidence rationale; do
    create_signal "${strategy_id}" "${instrument_id}" "${signal_type}" "${strategy_signal_date}" "${score}" "${confidence}" "${rationale}"
    log "inserted signal ${strategy_name}: ${ticker} ${signal_type} score=${score} conf=${confidence} date=${strategy_signal_date}"
    inserted=$((inserted + 1))
  done < "${signal_rows_file}"

  log "${strategy_name}: inserted signals=${inserted}"
}

macro_exists() {
  local indicator_code="$1"
  local region_code="$2"
  local observed_date="$3"
  local filter_json
  local response

  filter_json="$(
    jq -cn \
      --arg keyword "${indicator_code}" \
      --arg regionCode "${region_code}" \
      --arg fromDate "${observed_date}" \
      --arg toDate "${observed_date}" \
      '{keyword: $keyword, regionCode: $regionCode, fromDate: $fromDate, toDate: $toDate}'
  )"

  response="$(
    api_get "/macro-indicators" \
      --get \
      --data-urlencode "page=1" \
      --data-urlencode "perPage=100" \
      --data-urlencode "filter=${filter_json}"
  )"

  echo "${response}" | jq -e \
    --arg code "${indicator_code}" \
    --arg region "${region_code}" \
    --arg date "${observed_date}" \
    '.data[] | select(.indicatorCode == $code and .regionCode == $region and .observedDate == $date)' \
    >/dev/null
}

list_macro_ids() {
  local indicator_code="$1"
  local region_code="$2"
  local observed_date="$3"
  local filter_json
  local response

  filter_json="$(
    jq -cn \
      --arg keyword "${indicator_code}" \
      --arg regionCode "${region_code}" \
      --arg fromDate "${observed_date}" \
      --arg toDate "${observed_date}" \
      '{keyword: $keyword, regionCode: $regionCode, fromDate: $fromDate, toDate: $toDate}'
  )"

  response="$(
    api_get "/macro-indicators" \
      --get \
      --data-urlencode "page=1" \
      --data-urlencode "perPage=200" \
      --data-urlencode "filter=${filter_json}"
  )"

  echo "${response}" | jq -r \
    --arg code "${indicator_code}" \
    --arg region "${region_code}" \
    --arg date "${observed_date}" \
    '.data[] | select(.indicatorCode == $code and .regionCode == $region and .observedDate == $date) | .id'
}

replace_existing_macro_indicator() {
  local indicator_code="$1"
  local region_code="$2"
  local observed_date="$3"
  local macro_id

  while IFS= read -r macro_id; do
    if [ -z "${macro_id}" ]; then
      continue
    fi
    api_delete "/macro-indicators/${macro_id}"
    log "deleted existing macro id=${macro_id} code=${indicator_code}/${region_code}/${observed_date}"
  done < <(list_macro_ids "${indicator_code}" "${region_code}" "${observed_date}")
}

ensure_macro_indicator() {
  local indicator_code="$1"
  local indicator_name="$2"
  local region_code="$3"
  local observed_date="$4"
  local indicator_value="$5"
  local unit="$6"
  local source="$7"
  local payload

  if macro_exists "${indicator_code}" "${region_code}" "${observed_date}"; then
    if [ "${REPLACE_MACRO_INDICATORS}" = "true" ]; then
      replace_existing_macro_indicator "${indicator_code}" "${region_code}" "${observed_date}"
    else
      log "skip macro ${indicator_code}/${region_code}/${observed_date}: already exists"
      return
    fi
  fi

  payload="$(
    jq -cn \
      --arg indicatorCode "${indicator_code}" \
      --arg indicatorName "${indicator_name}" \
      --arg regionCode "${region_code}" \
      --arg observedDate "${observed_date}" \
      --arg indicatorValue "${indicator_value}" \
      --arg unit "${unit}" \
      --arg source "${source}" \
      '{
        indicatorCode: $indicatorCode,
        indicatorName: $indicatorName,
        regionCode: $regionCode,
        observedDate: $observedDate,
        indicatorValue: ($indicatorValue | tonumber),
        unit: $unit,
        source: $source
      }'
  )"

  api_post_json "/macro-indicators" "${payload}" >/dev/null
  log "inserted macro ${indicator_code}/${region_code}/${observed_date}"
}

fetch_fred_latest_value() {
  local series_id="$1"
  local csv

  csv="$(curl -fsS "${SOURCE_FRED_CSV_URL_BASE}?id=${series_id}")"
  echo "${csv}" | awk -F',' '
    NR > 1 && $2 != "" && $2 != "." {
      latestDate = $1
      latestValue = $2
    }
    END {
      if (latestDate == "") {
        exit 1
      }
      printf "%s\t%s\n", latestDate, latestValue
    }
  '
}

fetch_fred_cpi_yoy() {
  local csv

  csv="$(curl -fsS "${SOURCE_FRED_CSV_URL_BASE}?id=CPIAUCSL")"
  echo "${csv}" | awk -F',' '
    NR > 1 && $2 != "" && $2 != "." {
      n++
      date[n] = $1
      value[n] = $2 + 0
    }
    END {
      if (n < 13 || value[n - 12] <= 0) {
        exit 1
      }
      yoy = ((value[n] / value[n - 12]) - 1) * 100
      printf "%s\t%.4f\n", date[n], yoy
    }
  '
}

fetch_stooq_quote_close() {
  local symbol="$1"
  local csv
  local raw_date
  local close_price
  local normalized_date

  csv="$(curl -fsS "${SOURCE_STOOQ_QUOTE_URL_BASE}?s=${symbol}&i=d")"
  raw_date="$(echo "${csv}" | awk -F',' 'NR==1 {print $2}')"
  close_price="$(echo "${csv}" | awk -F',' 'NR==1 {print $7}')"

  if [ -z "${raw_date}" ] || [ -z "${close_price}" ] || [ "${raw_date}" = "N/D" ] || [ "${close_price}" = "N/D" ]; then
    return 1
  fi

  normalized_date="$(normalize_stooq_date "${raw_date}")"
  printf '%s\t%s\n' "${normalized_date}" "${close_price}"
}

seed_macro_indicators() {
  local cpi_data fed_data us10y_data kr_rate_data krw_usd_data
  local cpi_date cpi_value fed_date fed_value us10y_date us10y_value
  local kr_rate_date kr_rate_value krw_usd_date krw_usd_value

  cpi_data="$(fetch_fred_cpi_yoy || true)"
  fed_data="$(fetch_fred_latest_value "FEDFUNDS" || true)"
  us10y_data="$(fetch_fred_latest_value "DGS10" || true)"
  kr_rate_data="$(fetch_fred_latest_value "IRSTCI01KRM156N" || true)"
  krw_usd_data="$(fetch_stooq_quote_close "usdkrw" || true)"

  cpi_date="$(resolve_observed_date "${cpi_data%%$'\t'*}" "${MACRO_DATE_OVERRIDE}")"
  cpi_value="${cpi_data#*$'\t'}"
  if [ -z "${cpi_data}" ] || [ "${cpi_date}" = "${cpi_value}" ]; then
    cpi_date="$(resolve_observed_date "$(date +%F)" "${MACRO_DATE_OVERRIDE}")"
    cpi_value="2.9000"
    log "fallback macro US_CPI_YOY: using default value ${cpi_value}"
  fi

  fed_date="$(resolve_observed_date "${fed_data%%$'\t'*}" "${MACRO_DATE_OVERRIDE}")"
  fed_value="${fed_data#*$'\t'}"
  if [ -z "${fed_data}" ] || [ "${fed_date}" = "${fed_value}" ]; then
    fed_date="$(resolve_observed_date "$(date +%F)" "${MACRO_DATE_OVERRIDE}")"
    fed_value="4.5000"
    log "fallback macro US_FED_FUNDS: using default value ${fed_value}"
  fi

  us10y_date="$(resolve_observed_date "${us10y_data%%$'\t'*}" "${MACRO_DATE_OVERRIDE}")"
  us10y_value="${us10y_data#*$'\t'}"
  if [ -z "${us10y_data}" ] || [ "${us10y_date}" = "${us10y_value}" ]; then
    us10y_date="$(resolve_observed_date "$(date +%F)" "${MACRO_DATE_OVERRIDE}")"
    us10y_value="4.1200"
    log "fallback macro US10Y_YIELD: using default value ${us10y_value}"
  fi

  kr_rate_date="$(resolve_observed_date "${kr_rate_data%%$'\t'*}" "${MACRO_DATE_OVERRIDE}")"
  kr_rate_value="${kr_rate_data#*$'\t'}"
  if [ -z "${kr_rate_data}" ] || [ "${kr_rate_date}" = "${kr_rate_value}" ]; then
    kr_rate_date="$(resolve_observed_date "$(date +%F)" "${MACRO_DATE_OVERRIDE}")"
    kr_rate_value="3.0000"
    log "fallback macro KR_BASE_RATE: using default value ${kr_rate_value}"
  fi

  krw_usd_date="$(resolve_observed_date "${krw_usd_data%%$'\t'*}" "${MACRO_DATE_OVERRIDE}")"
  krw_usd_value="${krw_usd_data#*$'\t'}"
  if [ -z "${krw_usd_data}" ] || [ "${krw_usd_date}" = "${krw_usd_value}" ]; then
    krw_usd_date="$(resolve_observed_date "$(date +%F)" "${MACRO_DATE_OVERRIDE}")"
    krw_usd_value="1385.2000"
    log "fallback macro KRW_USD: using default value ${krw_usd_value}"
  fi

  ensure_macro_indicator "US_CPI_YOY" "US CPI YoY" "US" "${cpi_date}" "${cpi_value}" "%" "FRED CPIAUCSL (computed YoY)"
  ensure_macro_indicator "US_FED_FUNDS" "US Federal Funds Rate" "US" "${fed_date}" "${fed_value}" "%" "FRED FEDFUNDS"
  ensure_macro_indicator "US10Y_YIELD" "US 10Y Treasury Yield" "US" "${us10y_date}" "${us10y_value}" "%" "FRED DGS10"
  ensure_macro_indicator "KR_BASE_RATE" "Korea Short-Term Interest Rate" "KR" "${kr_rate_date}" "${kr_rate_value}" "%" "FRED IRSTCI01KRM156N (proxy)"
  ensure_macro_indicator "KRW_USD" "KRW per USD" "GLOBAL" "${krw_usd_date}" "${krw_usd_value}" "KRW/USD" "Stooq USDKRW"
}

main() {
  require_cmd curl
  require_cmd jq
  require_cmd awk

  api_get "/public/ping" >/dev/null
  TMP_DIR="$(mktemp -d)"

  if ! api_get "/quant-strategies?page=1&perPage=1" >/dev/null 2>&1; then
    log "quant strategy endpoint unavailable: ${API_BASE_URL}/quant-strategies"
    return 1
  fi

  if ! api_get "/macro-indicators?page=1&perPage=1" >/dev/null 2>&1; then
    log "macro indicator endpoint unavailable: ${API_BASE_URL}/macro-indicators"
    return 1
  fi

  local buffett_strategy_id
  local cathie_strategy_id
  local pelosi_strategy_id

  buffett_strategy_id="$(ensure_strategy \
    "Buffett Value Concentration" \
    "VALUE" \
    "ACTIVE" \
    "30" \
    "Large-cap value concentration with cash-flow durability filters")"

  cathie_strategy_id="$(ensure_strategy \
    "Cathie Disruptive Growth Momentum" \
    "MOMENTUM" \
    "ACTIVE" \
    "14" \
    "High-volatility innovation momentum with thematic conviction")"

  pelosi_strategy_id="$(ensure_strategy \
    "Pelosi Congressional Flow Tracker" \
    "CUSTOM" \
    "ACTIVE" \
    "7" \
    "Congressional flow tracking overlay with event-driven timing")"

  seed_strategy_signals_from_portfolio \
    "${buffett_strategy_id}" \
    "Buffett Value Concentration" \
    "Warren Buffett (Dataroma)" \
    "$(cat <<'ROWS'
AAPL		Apple
BAC		Bank of America
KO		Coca-Cola
ROWS
)"

  seed_strategy_signals_from_portfolio \
    "${cathie_strategy_id}" \
    "Cathie Disruptive Growth Momentum" \
    "Cathie Wood - ARKK (StockAnalysis)" \
    "$(cat <<'ROWS'
TSLA		Tesla
COIN		Coinbase
ROKU		Roku
ROWS
)"

  seed_strategy_signals_from_portfolio \
    "${pelosi_strategy_id}" \
    "Pelosi Congressional Flow Tracker" \
    "Nancy Pelosi (QuiverQuant Live)" \
    "$(cat <<'ROWS'
NVDA		NVIDIA
MSFT		Microsoft
PANW		Palo Alto Networks
ROWS
)"

  seed_macro_indicators

  log "done"
}

main "$@"
