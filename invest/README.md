# quant-portal

Initial scaffold based on `agents.md`:
- Java 21
- Spring Boot multi-module backend (Gradle Kotlin DSL)
- Next.js + react-admin frontend (`frontend-admin`)
- Local MariaDB via Docker Compose

## Modules
- `apps:api`
- `apps:batch`
- `libs:common`
- `libs:domain`
- `libs:infra`

## Local DB
```bash
docker compose up -d mariadb
```

## Build Backend
```bash
gradle build
```

## Run API
```bash
gradle :apps:api:bootRun
```

## Run Frontend Admin
```bash
cd frontend-admin
npm install
npm run dev
```

## Frontend Test
```bash
cd frontend-admin
npm test
```

## Frontend E2E Test
```bash
cd frontend-admin
npx playwright install chromium
npm run test:e2e
```

Set API URL if needed:
```bash
export NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1
```

## Default Local Auth (HTTP Basic)
- `admin / admin1234` (`ROLE_ADMIN`, `ROLE_USER`)
- `demo / demo1234` (`ROLE_USER`)

Credentials can be overridden with:
- `APP_ADMIN_USERNAME`
- `APP_ADMIN_PASSWORD`
- `APP_USER_USERNAME`
- `APP_USER_PASSWORD`

## Local Helper Scripts
```bash
# start/stop local mariadb
./scripts/local/db-up.sh
./scripts/local/db-down.sh

# run backend/frontend for local dev
./scripts/local/run-backend.sh
./scripts/local/run-frontend.sh
./scripts/local/run-all.sh
./scripts/local/stop-all.sh

# run all sample seeders
./scripts/local/seed-all.sh

# run backend + frontend tests
./scripts/local/test-all.sh
```

## Sample Seed Policy (Auto)
- `./scripts/local/run-all.sh` runs `./scripts/local/seed-all.sh` by default.
- Disable auto seeding:
  - `AUTO_SEED_LOCAL=false ./scripts/local/run-all.sh`
- Seed orchestration target path:
  - `/Users/revy/workspace_codex/invest/scripts/local/seeds/*.sh`
- For future domain features, add a dedicated script in `scripts/local/seeds/` to include related sample data automatically.
