# Admin API

Spring Boot server for admin portal traffic.

Responsibilities:
- admin authentication
- customer, account, transaction search
- linked bank account registry lookup, stale verification monitoring, verification override activation, and block action
- funding request queue lookup with manual review and settlement window policy snapshot
- approvals and audit logs
- announcement management
- FX, exchange, stock order, stock position admin operations
- exchange fee / net settlement and stock fee / tax / net settlement monitoring
- partial fill order monitoring and `complete-fill` execution endpoint

Server-local services/controllers:
- `com.revy.mvpbanking.auth.application.AdminAuthService`
- `com.revy.mvpbanking.admin.application.AdminOverviewService`
- `com.revy.mvpbanking.customer.application.CustomerQueryService`
- `com.revy.mvpbanking.auth.presentation.AdminAuthController`
- `com.revy.mvpbanking.admin.presentation.AdminOverviewController`
- `com.revy.mvpbanking.customer.presentation.AdminCustomerController`
- admin 도메인 컨트롤러 전량(`account`, `transaction`, `fx`, `funding`, `exchange`, `stock`, `approval`, `audit`, `linkedaccount`, `notification`, `announcement`)
- `com.revy.mvpbanking.config.AdminSecurityConfig`

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
