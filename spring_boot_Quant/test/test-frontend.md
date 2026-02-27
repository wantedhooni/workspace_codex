# Frontend 화면/API 테스트 결과

- 실행 시각: 2026-02-10 (Asia/Seoul)
- 실행 스크립트: `./scripts/test-frontend-e2e.sh`

## 테스트 범위

- React-Admin 주요 화면 렌더링
- 각 화면의 백엔드 API 연동
- 헤드리스 Playwright 스크린샷 생성 확인

## 사전 준비

- MariaDB 기동 및 데모데이터 import
- backend-mvp 기동
- 화면 데이터 확인을 위한 in-memory seed API 호출
  - 주문 생성 -> 체결 생성 -> 전표 생성/승인/전기

## 화면 테스트 결과

| 화면(Route) | 연동 API | 결과 | 아티팩트 |
|---|---|---|---|
| `/#/orders` | `GET /api/orders` | PASS | `/Users/revy/workspace_codex/spring_boot_Quant/test/artifacts/frontend-orders.png` |
| `/#/trades` | `GET /api/trades` | PASS | `/Users/revy/workspace_codex/spring_boot_Quant/test/artifacts/frontend-trades.png` |
| `/#/positions` | `GET /api/positions?portfolioId=1` | PASS | `/Users/revy/workspace_codex/spring_boot_Quant/test/artifacts/frontend-positions.png` |
| `/#/journalVouchers` | `GET /api/journal-vouchers` | PASS | `/Users/revy/workspace_codex/spring_boot_Quant/test/artifacts/frontend-journal-vouchers.png` |
| `/#/ledgerEntries` | `GET /api/ledgers/entries?portfolioId=1` | PASS | `/Users/revy/workspace_codex/spring_boot_Quant/test/artifacts/frontend-ledger-entries.png` |

## 테스트 중 수정한 오류

1. 목록 화면 API 미연동
- 증상: orders/trades/journal-vouchers 화면이 빈 데이터만 표시
- 조치: dataProvider에서 실제 목록 API 호출하도록 수정
  - `/Users/revy/workspace_codex/spring_boot_Quant/frontend-admin/src/providers/dataProvider.ts`

2. 백엔드 목록 API 미제공
- 조치: 백엔드 목록 API 추가(backend 보고서와 동일)

## 빌드 검증

- `cd frontend-admin && npm run build` PASS
- 번들 사이즈 경고(500KB 초과) 존재하나 기능 오류는 아님

## 결론

- 전체 대상 화면 렌더링/연동 API 호출 정상
- 기능 오류 없음
