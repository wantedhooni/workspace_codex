# Admin Portal

React + Refine admin application.

Responsibilities:
- admin login
- customer, account, transaction operations view
- funding request operations view
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

Seed login:
- `admin@mvpbanking.local / Admin1234!`

Important routes:
- `/`
- `/customers`
- `/accounts`
- `/transactions`
- `/fx-rates`
- `/funding-requests`
- `/exchange-requests`
- `/stock-orders`
- `/stock-positions`
- `/announcements`
- `/approvals`
- `/notifications`
- `/audit-logs`
