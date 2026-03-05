# User API

Spring Boot server for user web application traffic.

Responsibilities:
- user signup and login
- announcement lookup and service banner source
- own account and transaction lookup
- linked bank account registration, verification, resend cooldown, and primary withdrawal switching
- funding request creation and request history lookup with cutoff / daily limit / manual review policy snapshot
- FX rates and exchange requests
- stock orders and stock positions

Server-local services/controllers:
- `com.revy.mvpbanking.auth.application.UserAuthService`
- `com.revy.mvpbanking.user.application.UserDashboardInsightService`
- `com.revy.mvpbanking.auth.presentation.UserAuthController`
- `com.revy.mvpbanking.user.presentation.UserDashboardController`

Defaults:
- app name: `mvp-banking-user-api`
- port: `8082`
- profile: `user-api`

Run:

```bash
cd /Users/revy/workspace_codex/mvp_banking/backend
./gradlew :user-api:bootRun --args='--spring.profiles.active=user-api'
```

Main entry:
- `src/main/java/com/revy/mvpbanking/userapi/UserApiApplication.java`

Seed login:
- `user@mvpbanking.local / User1234!`

Typical direct checks:

```bash
curl http://localhost:8082/actuator/health
curl http://localhost:8082/api/system/ping
```
