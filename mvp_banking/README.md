# MVP Banking Platform

Banking / securities MVP workspace with:
- `backend/platform-core`: shared Spring Boot domain, persistence, security, and business modules
- `backend/discovery-server`: Spring Cloud service registry
- `backend/api-gateway`: Spring Cloud API gateway
- `backend/admin-api`: Spring Boot admin API server
- `backend/user-api`: Spring Boot user API server
- `frontend/admin-portal`: React + Refine admin application
- `frontend/user-web-app`: React user-facing web application
- `infra`: local PostgreSQL / Redis runtime

Module READMEs:
- `backend/README.md`
- `backend/platform-core/README.md`
- `backend/discovery-server/README.md`
- `backend/api-gateway/README.md`
- `backend/admin-api/README.md`
- `backend/user-api/README.md`
- `frontend/admin-portal/README.md`
- `frontend/user-web-app/README.md`
- `infra/README.md`

Docs:
- `docs/system-architecture.md`
- `docs/e2e-testing.md`
- `docs/frontend-domain-template.md`

## Run Local Infra

```bash
cd /Users/revy/workspace_codex/mvp_banking
docker compose -f infra/docker-compose.yml up -d
```

## Run Backend

```bash
cd /Users/revy/workspace_codex/mvp_banking/backend
./gradlew :discovery-server:bootRun --args='--spring.profiles.active=discovery-server'
./gradlew :admin-api:bootRun --args='--spring.profiles.active=admin-api'
./gradlew :user-api:bootRun --args='--spring.profiles.active=user-api'
./gradlew :api-gateway:bootRun --args='--spring.profiles.active=api-gateway'
```

Backend defaults:
- Discovery Server: `http://localhost:8761`
- API Gateway HTTP: `http://localhost:8080`
- API Gateway Health: `http://localhost:8080/actuator/health`
- Admin API HTTP: `http://localhost:8081`
- Admin API Health: `http://localhost:8081/actuator/health`
- Admin API Ping: `http://localhost:8081/api/system/ping`
- User API HTTP: `http://localhost:8082`
- User API Health: `http://localhost:8082/actuator/health`
- User API Ping: `http://localhost:8082/api/system/ping`
- Shared persistence: PostgreSQL + Redis
- Gateway routes:
  - `/api/admin/** -> mvp-banking-admin-api`
  - `/api/user/** -> mvp-banking-user-api`

Seed accounts:
- Admin: `admin@mvpbanking.local` / `Admin1234!`
- User: `user@mvpbanking.local` / `User1234!`
- Demo dataset: `100` customers with seeded accounts, transactions, and approval queue data
- Primary demo user banking accounts: `KRW` + `USD`

Initial API surface:
- `POST /api/admin/auth/login`
- `POST /api/admin/auth/refresh`
- `POST /api/admin/auth/logout`
- `GET /api/admin/me`
- `GET /api/admin/overview`
- `GET /api/admin/announcements`
- `POST /api/admin/announcements`
- `POST /api/admin/announcements/{announcementId}/publish`
- `POST /api/admin/announcements/{announcementId}/archive`
- `GET /api/admin/customers`
- `GET /api/admin/accounts`
- `GET /api/admin/transactions`
- `GET /api/admin/fx-rates`
- `GET /api/admin/linked-bank-accounts`
- `POST /api/admin/linked-bank-accounts/{linkedBankAccountId}/activate`
- `POST /api/admin/linked-bank-accounts/{linkedBankAccountId}/block`
- `GET /api/admin/funding-requests`
- `GET /api/admin/exchange-requests`
- `GET /api/admin/stock-orders`
- `GET /api/admin/stock-positions`
- `GET /api/admin/approvals`
- `POST /api/admin/approvals/{approvalRequestId}/approve`
- `POST /api/admin/approvals/{approvalRequestId}/reject`
- `GET /api/admin/notifications`
- `POST /api/admin/notifications/{notificationId}/read`
- `GET /api/admin/audit-logs`
- `POST /api/user/auth/signup`
- `POST /api/user/auth/login`
- `POST /api/user/auth/refresh`
- `POST /api/user/auth/logout`
- `GET /api/user/me`
- `GET /api/user/dashboard/insights`
- `GET /api/user/announcements`
- `GET /api/user/accounts`
- `GET /api/user/linked-bank-accounts`
- `POST /api/user/linked-bank-accounts`
- `POST /api/user/linked-bank-accounts/{linkedBankAccountId}/primary`
- `POST /api/user/linked-bank-accounts/{linkedBankAccountId}/resend-verification`
- `POST /api/user/linked-bank-accounts/{linkedBankAccountId}/verify`
- `GET /api/user/funding-requests`
- `POST /api/user/funding-requests`
- `POST /api/user/funding-requests/{requestId}/cancel`
- `GET /api/user/transactions`
- `GET /api/user/fx-rates`
- `GET /api/user/exchange-requests`
- `POST /api/user/exchange-requests`
- `POST /api/user/exchange-requests/{requestId}/cancel`
- `GET /api/user/stock-orders`
- `GET /api/user/stock-positions`
- `POST /api/user/stock-orders`
- `POST /api/user/stock-orders/{orderId}/cancel`
- `GET /api/user/notifications`
- `POST /api/user/notifications/{notificationId}/read`

Admin list query options:
- `GET /api/admin/customers?query=&status=&createdFrom=&createdTo=&page=&size=`
- `GET /api/admin/customers/summary?query=&createdFrom=&createdTo=`
- `GET /api/admin/accounts?query=&status=&accountType=&minBalance=&maxBalance=&page=&size=`
- `GET /api/admin/transactions?query=&status=&transactionType=&minAmount=&maxAmount=&occurredFrom=&occurredTo=&page=&size=`
- `sortBy` / `sortDir` supported for admin lists
- Example: `GET /api/admin/customers?sortBy=fullName&sortDir=asc`
- Example: `GET /api/admin/accounts?sortBy=balance&sortDir=desc&minBalance=100000000`
- Example: `GET /api/admin/transactions?sortBy=amount&sortDir=desc&minAmount=2000000&occurredFrom=2026-03-01`

Masking policy:
- Admin customer name / email responses are masked
- Admin account numbers are masked

## Run Admin Portal

```bash
cd /Users/revy/workspace_codex/mvp_banking/frontend/admin-portal
npm install
npm run dev
```

Admin Portal defaults:
- URL: `http://localhost:5173`
- Seed account: `admin@mvpbanking.local / Admin1234!`
- Routes: `/`, `/customers`, `/accounts`, `/transactions`, `/fx-rates`, `/linked-bank-accounts`, `/funding-requests`, `/exchange-requests`, `/stock-orders`, `/stock-positions`, `/approvals`, `/announcements`, `/notifications`, `/audit-logs`
- Page-level lazy loading enabled for admin routes
- Advanced filters: balance range, amount range, transaction date range
- Customer page: status summary cards, customer created date range
- Markets: FX rates, funding queue, exchange queue, stock order queue
- Overview: 승인 backlog, 심사 필요 고객, 시세 freshness, 운영 alerts, funding queue, pending instruction volume
- Operations inbox: unread badge, severity별 운영 알림, 읽음 처리, 도착 시각과 액션 경로 확인
- Linked bank accounts: 사용자 외부 출금 계좌 목록, stale verification 우선 점검, 운영 차단 액션
- Funding operations: 수동 심사 플래그, 일일 한도 초과, 당일/다음 영업일 정산 윈도우를 큐에서 바로 확인
- Announcements: draft / publish / archive, severity, audience, pin, service banner management
- Stock operations: execution history, partial fill / remaining quantity, manual fill completion, settlement transaction number, fee / tax / net settlement breakdown, stock position inventory, mark-to-market valuation

## Run User Web Application

```bash
cd /Users/revy/workspace_codex/mvp_banking/frontend/user-web-app
npm install
npm run dev
```

User Web Application defaults:
- URL: `http://localhost:5174`
- Seed account: `user@mvpbanking.local / User1234!`
- Routes: `/`, `/announcements`, `/accounts`, `/linked-bank-accounts`, `/funding-requests`, `/transactions`, `/fx-rates`, `/exchange-requests`, `/stock-orders`, `/stock-positions`, `/notifications`
- Built-in filters: account text filter, transaction text filter, transaction status filter
- Dashboard: total assets, 현금/투자 자산 분리, 통화 노출, action board, 상위 보유 종목, funding/exchange/order service queue, stock valuation reflected
- Service banner: published announcement 중 critical/pinned 우선 공지를 상단 배너로 노출
- Notification center: unread badge, 최근 이벤트 프리뷰, 환전/주식/포트폴리오 알림 읽음 처리
- Account cards: linked recent transactions per account
- Linked bank accounts: 외부 출금 계좌 등록, 검증 대기 티켓, 만료/재발송 cooldown, 다음 재발송 가능 시각, 소액이체 인증 문구 제출, 기본 출금 전환, 활성/차단 상태 확인
- Funding: account 기반 입금/출금 요청 티켓, 기본 외부 목적지 자동 선택, 승인 후 예상 잔액 프리뷰, 컷오프/일일 한도/수동 심사 프리뷰, 요청 내역 추적
- Markets: FX rate calculator, FX rate board, source/destination account exchange ticket with expected gross receive / fee / net receive, stock order ticket with estimated notional / fee / tax / cash impact
- Portfolio extension: stock positions, current price, market value, unrealized/realized P/L, stock execution history, partial fill progress, remaining quantity, settled exchange/order details, fee and net settlement breakdown

Dev note:
- `frontend/admin-portal` proxies `/api` and `/actuator` to `http://localhost:8080`
- `frontend/user-web-app` proxies `/api` and `/actuator` to `http://localhost:8080`
- backend start order: `discovery-server -> admin-api / user-api -> api-gateway`

## Script Commands

```bash
cd /Users/revy/workspace_codex/mvp_banking
./scripts/all-start.sh
./scripts/status.sh
./scripts/all-stop.sh
```

Runtime artifacts:
- PID files: `/Users/revy/workspace_codex/mvp_banking/.runtime/pids`
- Logs: `/Users/revy/workspace_codex/mvp_banking/.runtime/logs`
- `all-start.sh` stops any stale legacy monolith listener on `8080` before starting the Spring Cloud stack
- start / stop scripts print service URLs, demo accounts, and log file locations to stdout

Recommended script flow:

```bash
cd /Users/revy/workspace_codex/mvp_banking
./scripts/all-start.sh
./scripts/e2e-smoke.sh
./scripts/all-stop.sh
```

Available scripts:
- `./scripts/infra-start.sh`
- `./scripts/infra-stop.sh`
- `./scripts/discovery-server-start.sh`
- `./scripts/discovery-server-stop.sh`
- `./scripts/api-gateway-start.sh`
- `./scripts/api-gateway-stop.sh`
- `./scripts/admin-api-start.sh`
- `./scripts/admin-api-stop.sh`
- `./scripts/user-api-start.sh`
- `./scripts/user-api-stop.sh`
- `./scripts/backend-start.sh`
- `./scripts/backend-stop.sh`
- `./scripts/admin-portal-start.sh`
- `./scripts/admin-portal-stop.sh`
- `./scripts/user-web-app-start.sh`
- `./scripts/user-web-app-stop.sh`
- `./scripts/status.sh`
- `./scripts/all-start.sh`
- `./scripts/all-stop.sh`
- `./scripts/e2e-smoke.sh`
- `./scripts/scaffold-frontend-domain.sh`

E2E smoke test:
- Default: starts the full stack, verifies health endpoints, performs admin/user login, checks core admin/user APIs including linked bank account create/resend/verify, funding request create/cancel, and validates notification read endpoints
- Reuse already running services: `START_STACK=0 ./scripts/e2e-smoke.sh`
- Stop stack after test: `STOP_STACK=1 ./scripts/e2e-smoke.sh`
- Detailed guide: `docs/e2e-testing.md`

Frontend domain scaffolding:
- Generate user/admin frontend domain templates: `./scripts/scaffold-frontend-domain.sh --domain transfer-limit --target both`
- Detailed guide: `docs/frontend-domain-template.md`
