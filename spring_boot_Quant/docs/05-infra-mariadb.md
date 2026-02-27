# MariaDB Infra 실행 가이드

## 구성

- MariaDB 11.4 (`quant-mariadb`)
- Adminer (`quant-adminer`, http://localhost:18080)

접속 정보:
- DB: `quant`
- User: `quant`
- Password: `quant1234`
- Root Password: `root1234`

## 실행/정지

```bash
./scripts/infra-up.sh
./scripts/infra-status.sh
./scripts/infra-down.sh
```

## 전체 검증

```bash
./scripts/test-all.sh
```

검증 범위:
- backend-mvp: `gradle test`
- backend-mvp + actuator/ledger smoke
- market-data-adapter health/quote
- frontend-admin build
