# API Gateway

Spring Cloud Gateway entry point for frontend clients.

Routes:
- `/api/admin/** -> mvp-banking-admin-api`
- `/api/user/** -> mvp-banking-user-api`

Defaults:
- app name: `mvp-banking-api-gateway`
- port: `8080`
- health: `http://localhost:8080/actuator/health`

Run:

```bash
cd /Users/revy/workspace_codex/mvp_banking/backend
./gradlew :api-gateway:bootRun --args='--spring.profiles.active=api-gateway'
```

Prerequisites:
- `discovery-server` must be running
- `admin-api` and `user-api` should be registered in Eureka

Frontend dev proxy target:
- `admin-portal -> http://localhost:8080`
- `user-web-app -> http://localhost:8080`
