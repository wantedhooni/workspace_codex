# Trade Auto MVP (Spring Boot + React)

US stock learning + signal timing app (EOD). Includes ticker universe, Polygon data ingest, scoring-based signals, and a simple backtest.

## Features
- Ticker universe seed (large caps)
- Daily market data refresh via Polygon API
- Scoring-based BUY/SELL/HOLD signals
- Risk management: stop loss + take profit
- Backtest with trade log + headline metrics
- React dashboard and management pages

## Prereqs
- Java 21
- Node.js 18+
- PostgreSQL 16 (via docker-compose or local)
- (Optional) Polygon API key

## Setup
1. Database

```bash
docker compose up -d
```

2. Backend

```bash
cd backend
export POLYGON_API_KEY=your_key
gradle bootRun
```

Set admin credentials (for Quartz/Jobs/Strategy admin APIs):

```bash
export ADMIN_USER=admin
export ADMIN_PASSWORD=admin1234
export ADMIN_JWT_SECRET=change-me-change-me-change-me-32
```

For local quick start without Flyway, use:

```bash
SPRING_PROFILES_ACTIVE=dev gradle bootRun
```

3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

## First Run
1. Click **Seed Tickers**
2. Click **Refresh Data** (requires Polygon API key)
3. Click **Generate Signals**
4. Run Backtest

## API (selected)
- `POST /api/v1/tickers/seed`
- `POST /api/v1/marketdata/refresh?days=365`
- `POST /api/v1/signals/generate`
- `GET /api/v1/signals/latest`
- `POST /api/v1/backtest/run`
- `GET /api/v1/backtest/runs`
- `POST /api/v1/jobs/daily/run`
- `GET /api/v1/jobs/daily/runs`
- `GET /api/v1/jobs/runs?jobName=daily-scan`
- `GET /api/v1/strategy`
- `PUT /api/v1/strategy`
- `GET /api/v1/quartz/jobs`
- `POST /api/v1/quartz/jobs`
- `DELETE /api/v1/quartz/jobs/{group}/{name}`
- `GET /api/v1/quartz/triggers`
- `PUT /api/v1/quartz/triggers/{group}/{name}`
- `POST /api/v1/quartz/triggers`
- `DELETE /api/v1/quartz/triggers/{group}/{name}`
- `POST /api/v1/quartz/triggers/{group}/{name}/pause`
- `POST /api/v1/quartz/triggers/{group}/{name}/resume`
- `POST /api/v1/quartz/jobs/{group}/{name}/run`
- `GET /api/v1/quartz/audit`
- `POST /api/v1/auth/login`

## API Examples
Trigger daily job immediately:

```bash
curl -X POST http://localhost:8080/api/v1/jobs/daily/run
```

List Quartz triggers:

```bash
curl http://localhost:8080/api/v1/quartz/triggers
```

Update a Quartz cron trigger and reschedule immediately:

```bash
curl -X PUT http://localhost:8080/api/v1/quartz/triggers/DEFAULT/dailyTrigger \
  -H "Content-Type: application/json" \
  -d '{"cron":"0 0 19 * * ?","timeZone":"America/New_York"}'
```

Create a new Quartz cron trigger:

```bash
curl -X POST http://localhost:8080/api/v1/quartz/triggers \
  -H "Content-Type: application/json" \
  -d '{"jobName":"dailyJob","jobGroup":"DEFAULT","triggerName":"dailyTrigger2","triggerGroup":"DEFAULT","cron":"0 0 20 * * ?","timeZone":"America/New_York"}'
```

Admin login for JWT:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin1234"}'
```

Use the JWT for admin APIs:

```bash
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/v1/quartz/triggers
```

Create a Quartz job (class must be in `com.tradeauto.*` and implement `Job`):

```bash
curl -X POST http://localhost:8080/api/v1/quartz/jobs \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"dailyJob2","group":"DEFAULT","className":"com.tradeauto.service.DailyQuartzJob","description":"Daily scan","durable":true}'
```

## Notes
- Data stored in PostgreSQL `tradeauto` DB
- Quartz scheduler runs daily at 6:20 PM ET (config in `application.yml`)
- Quartz uses JDBC job store (Postgres) for clustering and restart durability
- Strategy parameters are stored in DB (`strategy_config`) and editable via API
- Quartz schedules are controlled via API and rescheduled immediately
- Admin endpoints (`/api/v1/quartz/**`, `/api/v1/jobs/**`, `PUT /api/v1/strategy`) require JWT Bearer auth
- Quartz UI (`/quartz`) includes an Admin Access section to set JWT auth
- Adjust strategy parameters in `application.yml`
