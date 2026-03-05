# Admin Portal

React + Refine admin application.

Responsibilities:
- admin login
- customer, account, transaction operations view
- linked bank account registry with pending / stale verification queue, stale-first review order, ops override activation, and block action
- funding request operations view with manual review, daily limit, and expected settlement window visibility
- approval queue
- operations inbox / notification center
- service announcement draft / publish / archive
- audit log view
- operations overview with backlog, market freshness, funding volume, and alert board
- FX, exchange source/destination leg monitoring with gross / fee / net settlement visibility
- stock order execution / partial fill / remaining fill completion monitoring with fee / tax / net settlement visibility
- stock position mark-to-market monitoring

Defaults:
- URL: `http://localhost:5173`
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

Domain/page template scaffolding:

```bash
cd /Users/revy/workspace_codex/mvp_banking
./scripts/scaffold-frontend-domain.sh --domain transfer-limit --target admin
```

Guide:
- `/Users/revy/workspace_codex/mvp_banking/docs/frontend-domain-template.md`

Seed login:
- `admin@mvpbanking.local / Admin1234!`

Important routes:
- `/`
- `/customers`
- `/accounts`
- `/transactions`
- `/fx-rates`
- `/linked-bank-accounts`
- `/funding-requests`
- `/exchange-requests`
- `/stock-orders`
- `/stock-positions`
- `/announcements`
- `/approvals`
- `/notifications`
- `/audit-logs`
