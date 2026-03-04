# Discovery Server

Spring Cloud Eureka server.

Role:
- service registry for `admin-api`, `user-api`, `api-gateway`

Defaults:
- app name: `mvp-banking-discovery-server`
- port: `8761`
- URL: `http://localhost:8761`

Run:

```bash
cd /Users/revy/workspace_codex/mvp_banking/backend
./gradlew :discovery-server:bootRun --args='--spring.profiles.active=discovery-server'
```

Health:

```bash
curl http://localhost:8761/actuator/health
```
