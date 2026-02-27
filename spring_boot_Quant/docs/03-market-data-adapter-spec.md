# Market Data Adapter API Spec (v0.1)

## 1) 목적

- `yahoo-finance2`를 감싼 내부 어댑터 API를 제공한다.
- Spring Batch/Backend는 Yahoo 직접 호출 대신 본 어댑터를 호출한다.

## 2) Base URL

- Local: `http://localhost:4010`

## 3) Endpoints

### 3.1 Health

- `GET /health`
- Response
  - `{"status":"ok","provider":"yahoo-finance2"}`

### 3.2 Quote 단건 조회

- `GET /api/market/quote?symbol=AAPL`
- Query
  - `symbol` (required)
- Response
  - `symbol`
  - `currency`
  - `regularMarketPrice`
  - `regularMarketTime`
  - `marketState`

### 3.3 일봉/분봉 차트 조회

- `GET /api/market/price-bars?symbol=AAPL&interval=1d&from=2025-01-01&to=2025-01-31`
- Query
  - `symbol` (required)
  - `interval` (optional, default `1d`)
  - `from` (optional, `yyyy-MM-dd`)
  - `to` (optional, `yyyy-MM-dd`)
- Response
  - `symbol`
  - `interval`
  - `bars[]`
    - `date`
    - `open`
    - `high`
    - `low`
    - `close`
    - `volume`

## 4) 오류 응답

- HTTP 400: 요청 파라미터 오류
- HTTP 500: 공급자 호출 실패
- 형식
  - `{"code":"BAD_REQUEST|PROVIDER_ERROR","message":"..."}`

## 5) Spring 연동 가이드

- Spring Batch 잡에서 `GET /api/market/price-bars` 호출
- 배치는 멱등 업서트(키: `symbol + date + interval`)로 저장
- 공급자 실패 시 지수 백오프 재시도 적용
