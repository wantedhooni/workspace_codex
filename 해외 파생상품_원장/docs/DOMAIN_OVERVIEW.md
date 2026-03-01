# Domain Overview

해외 파생상품 운영 관리자 MVP의 주요 도메인과 관계를 정리한 문서입니다.

## 1. 도메인 맵

### Accounts
- `Account`
  - 해외 브로커 계좌의 기본 마스터
  - `accountNo`, `broker`, `status`, `ownerName` 관리
- `Balance`
  - 계좌/통화 기준 잔고 스냅샷
- `Position`
  - 파생상품 포지션 스냅샷
- `Margin`
  - 증거금 스냅샷

### Cash / FX Requests
- `CashRequest`
  - 입금/출금 요청
  - 승인정책, 리스크한도, 4-eyes 통제를 함께 받음
- `FxRequest`
  - 환전 요청
  - 브로커 송신 결과에 따라 `PENDING/APPROVED/REJECTED/FAILED`

### Stock Trading / Accounting
- `StockPurchase`
  - 주식 매수 거래 원본 이벤트
  - 거래일, 결제일, 수량, 단가, 수수료, 총액/순액을 보관
- `StockPosition`
  - 계좌 + 종목 단위 현재 보유수량/평균단가/총원가 집계
  - `StockPurchase`가 누적될수록 업서트됨
- `LedgerEntry`
  - 거래 기준 원장
  - 종목별 수량 증감, 금액 증감, 누적 보유량/누적 원가를 기록
- `JournalEntry`
  - 회계 분개 라인
  - 한 거래에 대해 같은 `journalNo` 아래 여러 라인이 생성됨
- `Portfolio`
  - 계좌 기준 현금 잔고, 보유 종목, 통화별 장부원가, 최근 거래를 묶어 보여주는 조회 모델
  - 저장 엔티티가 아니라 운영 화면용 읽기 전용 집계 응답

### Batch / Monitoring
- `BatchRun`
  - 운영 배치 실행 결과
  - Quartz/Spring Batch와 연결되는 실행 추적 데이터
- `OpsCase`
  - 장애/운영 예외 후속 조치 케이스

### Control / Governance
- `ApprovalPolicy`
  - 요청 도메인별 승인 임계치 정책
- `RiskLimitPolicy`
  - 요청 도메인/통화별 노출 한도 정책
- `AuditLog`
  - 사용자 주요 행위 감사 이력
- `MenuEntry`
  - 역할 기반 UI 메뉴 제어

## 2. 주식 도메인 처리 흐름

### 2.1 거래 생성
- 입력: `accountId`, `symbol`, `market`, `currency`, `tradeDate`, `settlementDate`, `quantity`, `price`, `feeAmount`
- 검증:
  - 계좌 존재 및 `ACTIVE` 상태
  - 수량/단가 양수
  - 수수료 0 이상
  - 결제일이 거래일보다 빠르지 않아야 함

### 2.2 포지션 반영
- `StockPurchase` 저장 후 `StockPosition`을 계좌 + 종목 기준으로 조회
- 기존 포지션이 없으면 신규 생성
- 기존 포지션이 있으면:
  - `quantity += purchase.quantity`
  - `totalCost += purchase.netAmount`
  - `averagePrice = totalCost / quantity`

### 2.3 원장 반영
- `LedgerEntry` 1건 생성
- 기록 항목:
  - `quantityChange`
  - `amountChange`
  - `runningQuantity`
  - `runningAmount`

### 2.4 분개 반영
- `JournalEntry`는 거래 1건당 2~3라인 생성
- 기본 분개:
  - 차변 `STOCK_INVENTORY` = 매수 총액
  - 차변 `TRADING_FEE_EXPENSE` = 수수료
  - 대변 `CASH` = 총액 + 수수료
- 수수료가 0이면 수수료 라인은 생략

### 2.5 포트폴리오 집계
- `Portfolio` 조회 시 최신 잔고 스냅샷과 현재 `StockPosition`을 함께 조회
- 현금은 `currency`별로 유지
- 주식 장부원가는 `currency`별 합계로 제공
- 최근 매수 내역은 최신 거래 순으로 조회

## 3. 도메인 관계

### 3.1 계좌 기준
- `Account 1:N Balance`
- `Account 1:N Position`
- `Account 1:N Margin`
- `Account 1:N CashRequest`
- `Account 1:N FxRequest`
- `Account 1:N StockPurchase`
- `Account 1:N StockPosition`
- `Account 1:N LedgerEntry`
- `Account 1:N JournalEntry`
- `Account 1:1 Portfolio(view)`

### 3.2 주식 매수 기준
- `StockPurchase 1:1 LedgerEntry`
  - 현재 구현 기준으로 매수 거래 1건당 원장 1건 생성
- `StockPurchase 1:N JournalEntry`
  - 거래 1건당 분개 여러 라인 생성
- `StockPurchase N:1 StockPosition`
  - 같은 계좌/종목 거래가 하나의 현재 포지션에 누적

## 4. 시드 샘플 데이터

### 4.1 계정
- `opsadmin / admin123!`
- `opsadmin2 / admin234!`
- `opsviewer / viewer123!`
- `auditor / audit123!`

### 4.2 주식 샘플
- 계좌 `CME-77889901`
  - `AAPL` 25주 매수, 단가 `182.450000`, 수수료 `15.5000`
  - `AAPL` 10주 추가 매수, 단가 `184.100000`, 수수료 `7.2500`
  - `NVDA` 12주 매수, 단가 `812.350000`, 수수료 `18.7500`
- 의도:
- 동일 종목 누적 매수에 따른 `StockPosition` 평균단가/총원가 반영 확인
- 거래별 `LedgerEntry`, `JournalEntry` 생성 확인
- `Portfolio` 화면에서 계좌별 잔고/보유/최근 거래 집계 확인

## 5. 현재 범위와 제외 사항
- 포함:
  - 주식 매수
  - 현재 포지션 누적
  - 거래 원장
  - 분개 생성
- 제외:
  - 주식 매도
  - 실현/평가손익
  - 시가평가
  - 결제 실패/정정/취소 처리
