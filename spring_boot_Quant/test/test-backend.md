# Backend API 테스트 결과

- 실행 시각: 2026-02-10 (Asia/Seoul)
- 기준: SpringDoc OpenAPI v3 (`/v3/api-docs`)
- 실행 스크립트: `./scripts/test-backend-openapi.sh`

## 실행 명령

```bash
./scripts/test-backend-openapi.sh
```

## 사전 조건

- MariaDB 인프라 기동: `./scripts/infra-up.sh`
- 스키마/데모데이터 import: `./scripts/db-import-demo.sh`
- backend 단위 테스트: `cd backend-mvp && gradle test`

## OpenAPI 경로 전수 테스트 결과

| Method | Path | 결과 |
|---|---|---|
| GET | /api/orders | PASS |
| POST | /api/orders | PASS |
| GET | /api/trades | PASS |
| POST | /api/trades/events | PASS |
| GET | /api/positions | PASS |
| GET | /api/journal-vouchers | PASS |
| POST | /api/journal-vouchers | PASS |
| POST | /api/journal-vouchers/{voucherId}/approve | PASS |
| POST | /api/journal-vouchers/{voucherId}/post | PASS |
| POST | /api/journal-vouchers/{voucherId}/cancel | PASS |
| GET | /api/ledgers/entries | PASS |
| POST | /api/ledgers/validate | PASS |
| GET | /api/ledgers/validate/last | PASS |

## 검증 로그/아티팩트

- OpenAPI json: `/Users/revy/workspace_codex/spring_boot_Quant/test/artifacts/openapi.json`
- API 응답 샘플:
  - `/Users/revy/workspace_codex/spring_boot_Quant/test/artifacts/api-orders.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/test/artifacts/api-trades.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/test/artifacts/api-positions.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/test/artifacts/api-journal-vouchers.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/test/artifacts/api-ledger-entries.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/test/artifacts/api-ledger-validate.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/test/artifacts/api-ledger-validate-last.json`

## 테스트 중 수정한 오류

1. MariaDB 스키마 호환 오류
- 증상: `bigserial`, `timestamptz` 등 PostgreSQL 타입으로 import 실패
- 조치: MariaDB 전용 스키마 추가
  - `/Users/revy/workspace_codex/spring_boot_Quant/db/migration/V1__init_quant_core_mariadb.sql`
- 조치: import 스크립트가 MariaDB 스키마를 사용하도록 변경
  - `/Users/revy/workspace_codex/spring_boot_Quant/scripts/db-import-demo.sh`

2. 스키마 재적용 시 중복 인덱스 오류
- 증상: `Duplicate key name` 발생
- 조치: 스키마 실행 전 테이블 drop 후 재생성(멱등)
  - `/Users/revy/workspace_codex/spring_boot_Quant/db/migration/V1__init_quant_core_mariadb.sql`

3. 프론트/리스트 검증을 위한 조회 API 누락
- 조치: 아래 목록 API 추가
  - `GET /api/orders`
  - `GET /api/trades`
  - `GET /api/journal-vouchers`
- 변경 파일:
  - `/Users/revy/workspace_codex/spring_boot_Quant/backend-mvp/src/main/java/com/quant/mvp/api/OrderController.java`
  - `/Users/revy/workspace_codex/spring_boot_Quant/backend-mvp/src/main/java/com/quant/mvp/api/TradeController.java`
  - `/Users/revy/workspace_codex/spring_boot_Quant/backend-mvp/src/main/java/com/quant/mvp/api/JournalVoucherController.java`

## 결론

- OpenAPI v3에 노출된 백엔드 API 전체 테스트 PASS
- 치명 오류 없음
