# Backend

Spring Cloud MSA backend for the MVP banking platform.

Modules:
- `platform-core`: shared domain, persistence, security, Flyway migrations
- `discovery-server`: Eureka registry
- `api-gateway`: entry point for `/api/admin/**`, `/api/user/**`
- `admin-api`: admin-only API server
- `user-api`: user-facing API server

Operational additions:
- admin operations overview snapshot
- user dashboard insight aggregation
- admin / user notification center endpoints

Default ports:
- `8761`: discovery server
- `8080`: API gateway
- `8081`: admin API
- `8082`: user API

Run from this directory:

```bash
./gradlew :discovery-server:bootRun --args='--spring.profiles.active=discovery-server'
./gradlew :admin-api:bootRun --args='--spring.profiles.active=admin-api'
./gradlew :user-api:bootRun --args='--spring.profiles.active=user-api'
./gradlew :api-gateway:bootRun --args='--spring.profiles.active=api-gateway'
```

Test:

```bash
./gradlew test
```

Local dependencies:
- PostgreSQL
- Redis

Recommended start path from repo root:

```bash
./scripts/all-start.sh
./scripts/e2e-smoke.sh
```

Related docs:
- `../docs/e2e-testing.md`
