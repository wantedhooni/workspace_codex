# Admin API

Spring Boot server for admin portal traffic.

Responsibilities:
- admin authentication
- customer, account, transaction search
- funding request queue lookup
- approvals and audit logs
- announcement management
- FX, exchange, stock order, stock position admin operations
- exchange fee / net settlement and stock fee / tax / net settlement monitoring
- partial fill order monitoring and `complete-fill` execution endpoint

Defaults:
- app name: `mvp-banking-admin-api`
- port: `8081`
- profile: `admin-api`

Run:

```bash
cd /Users/revy/workspace_codex/mvp_banking/backend
./gradlew :admin-api:bootRun --args='--spring.profiles.active=admin-api'
```

Main entry:
- `src/main/java/com/revy/mvpbanking/adminapi/AdminApiApplication.java`

Seed login:
- `admin@mvpbanking.local / Admin1234!`

Typical direct checks:

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8081/api/system/ping
```
