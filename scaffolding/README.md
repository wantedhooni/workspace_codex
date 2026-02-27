# Scaffolding

## Prerequisites
- Java 21+
- Node.js 18+

## Backend (backand)

### Build all modules
```bash
cd backand
./gradlew build
```

### Run api-admin
```bash
cd backand
./gradlew :api-admin:bootRun
```

### Run api-service
```bash
cd backand
./gradlew :api-service:bootRun
```

### Port configuration
Default ports: api-admin `8082`, api-service `8083`.

`backand/api-admin/src/main/resources/application.yml`
```yaml
server:
  port: 8082
```

`backand/api-service/src/main/resources/application.yml`
```yaml
server:
  port: 8083
```

## Frontend

### Admin UI (react-admin)
```bash
cd frontend/admin-ui
npm install
npm run dev
```

Optional API base URL:
```bash
# frontend/admin-ui/.env
VITE_ADMIN_API_BASE_URL=http://localhost:8081
```

### Service UI (placeholder)
```bash
cd frontend/service-ui
npm install
npm run dev
```
