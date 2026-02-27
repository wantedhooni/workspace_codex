# Frontend UI/기능 테스트 스크린샷 결과

- 기준 스크립트: `./scripts/test-frontend-e2e.sh`
- 테스트 시각: 2026-02-10 02:04 (Asia/Seoul)
- 결과: PASS

## 1) 주문 화면 (`/#/orders`)
- 검증 포인트: 주문 목록 API 연동, 컬럼 렌더링

![orders](/Users/revy/workspace_codex/spring_boot_Quant/test/artifacts/frontend-orders.png)

## 2) 체결 화면 (`/#/trades`)
- 검증 포인트: 체결 목록 API 연동, 체결 수량/체결가 표시

![trades](/Users/revy/workspace_codex/spring_boot_Quant/test/artifacts/frontend-trades.png)

## 3) 포지션 화면 (`/#/positions`)
- 검증 포인트: 포지션 목록 API 연동, 보유수량/평균단가/실현손익 표시

![positions](/Users/revy/workspace_codex/spring_boot_Quant/test/artifacts/frontend-positions.png)

## 4) 전표 화면 (`/#/journalVouchers`)
- 검증 포인트: 전표 목록 API 연동, 상태(DRAFT/APPROVED/POSTED 등) 표시

![journal-vouchers](/Users/revy/workspace_codex/spring_boot_Quant/test/artifacts/frontend-journal-vouchers.png)

## 5) 원장 화면 (`/#/ledgerEntries`)
- 검증 포인트: 원장 엔트리 API 연동, 계정/차대/금액 표시

![ledger-entries](/Users/revy/workspace_codex/spring_boot_Quant/test/artifacts/frontend-ledger-entries.png)
