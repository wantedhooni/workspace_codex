# 도메인/상태값 표준 (v0.1)

## 1) 공통 원칙

- 모든 상태 전이는 이벤트 로그를 남긴다.
- 주문(Order)과 체결(Trade)은 분리 저장한다.
- 포지션(Position)은 체결 누적 결과이며, 원장(Ledger)은 회계 정합성의 기준 원본이다.
- 전표(JournalVoucher)는 승인/전기 흐름을 따르며, 전기 후 원장 엔트리를 생성한다.

## 2) 핵심 엔티티 정의

- `Order`: 주문 의도와 수명주기 관리
- `Trade`: 브로커 체결 이벤트(부분체결 포함) 관리
- `Position`: 종목별 보유 수량/평단/평가손익 스냅샷
- `Ledger`: 계정 과목별 원장 엔트리(차/대)
- `JournalVoucher`: 전표 헤더(승인/전기 상태)
- `JournalEntry`: 전표 라인(분개 라인)

## 3) 상태값 표준

### 3.1 OrderStatus

- `NEW`: 생성됨
- `SENT`: 브로커 전송됨
- `PARTIAL`: 부분 체결
- `FILLED`: 전체 체결
- `CANCELED`: 취소 완료
- `REJECTED`: 거부

### 3.2 TradeStatus

- `EXECUTED`: 체결 반영 완료
- `CORRECTED`: 정정 반영 완료
- `CANCELED`: 체결 취소 반영 완료

### 3.3 VoucherStatus

- `DRAFT`: 임시저장
- `APPROVED`: 승인됨
- `POSTED`: 전기 완료(원장 반영)
- `CANCELED`: 전표 취소

## 4) 상태 전이 규칙(요약)

- 주문: `NEW -> SENT -> PARTIAL -> FILLED`
- 주문 예외: `NEW|SENT|PARTIAL -> CANCELED|REJECTED`
- 전표: `DRAFT -> APPROVED -> POSTED`
- 전표 취소: `DRAFT|APPROVED -> CANCELED` (`POSTED`는 역분개 전표로 처리)

## 5) 정합성 규칙

- 주문 수량 = 체결 수량 누적 + 미체결 수량
- 포지션 수량은 체결 이벤트 기준으로만 변경
- 전표 1건의 차변 합계 = 대변 합계
- 원장 잔액은 전기 완료(`POSTED`) 전표 라인만 반영

## 6) 용어/네이밍 규칙

- API DTO는 `{Feature}Payload.Req`, `{Feature}Payload.Res`
- 검색 DTO는 `{Feature}SearchPayload.Req` 형식으로 필드별 조건 선언
- 엔티티명은 단수형(`Order`, `Trade`, `Position`)
