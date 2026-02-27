# market-data-adapter

`yahoo-finance2` 기반 내부 시장데이터 어댑터.

## 참고 버전/패턴

- 패키지: `yahoo-finance2@^3.13.0`
- 사용 패턴(v3): `import YahooFinance from "yahoo-finance2"; const yf = new YahooFinance();`
- 브라우저 직접 호출이 아닌 서버(본 어댑터)에서 호출

## 실행

```bash
cd adapter/market-data-adapter
npm install
npm start
```

기본 포트: `4010`

## API

- `GET /health`
- `GET /api/market/quote?symbol=AAPL`
- `GET /api/market/price-bars?symbol=AAPL&interval=1d&from=2025-01-01&to=2025-01-31`
