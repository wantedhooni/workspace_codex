# User Web App

React user-facing web application.

Responsibilities:
- user login and signup
- asset summary dashboard with action board, currency exposure, funding/exchange/order queue, and top holdings
- service banner and announcement page
- notification center with unread badge and read action
- own account and transaction lookup
- linked bank account registry with verification ticket, expiry/resend cooldown, next resend time, demo verification memo, and primary withdrawal switching
- account based funding request ticket with withdrawal destination selection, projected balance preview, cutoff / daily limit / manual review policy preview, and request history
- FX rate calculator and source/destination account based exchange request entry with expected gross receive, fee, and net receive preview
- stock order entry and position view with execution history, partial fill progress, remaining quantity, estimated fee / tax / cash impact, and current price / valuation
- domain-based page routing and split API modules

Defaults:
- URL: `http://localhost:5174`
- API proxy target: `http://localhost:8080`

Run:

```bash
npm install
npm run dev
```

Build:

```bash
npm run build
```

Domain template scaffolding:

```bash
cd /Users/revy/workspace_codex/mvp_banking
./scripts/scaffold-frontend-domain.sh --domain transfer-limit --target user
```

Guide:
- `/Users/revy/workspace_codex/mvp_banking/docs/frontend-domain-template.md`

Seed login:
- `user@mvpbanking.local / User1234!`
- Demo banking accounts: `KRW` + `USD`

Routes:
- `/`
- `/announcements`
- `/accounts`
- `/linked-bank-accounts`
- `/funding-requests`
- `/transactions`
- `/fx-rates`
- `/exchange-requests`
- `/stock-orders`
- `/stock-positions`
- `/notifications`
