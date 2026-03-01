# Backend API 설명

Base URL: `http://localhost:8080/api/v1`

## 1. 인증

### POST `/auth/login`
- 설명: 로그인 후 JWT 발급
- 요청
```json
{ "username": "opsadmin", "password": "admin123!" }
```
- 응답
```json
{ "accessToken": "...", "expiresIn": 3600, "role": "OPS_ADMIN" }
```

## 2. 계좌

### GET `/accounts`
- 권한: `OPS_ADMIN | OPS_VIEWER | AUDITOR`
- 쿼리
  - `status`, `broker`
  - `keyword`
  - `filter` (RSQL)
  - `page`, `size`
  - `unmask` (관리자만 true 허용)

### GET `/accounts/{accountId}/summary`
- 권한: `OPS_ADMIN | OPS_VIEWER | AUDITOR`
- 쿼리: `unmask`
- 응답: 계좌 기본정보 + balances + positions + margin

## 3. 도메인 용어집

### GET `/domain-terms`
- 권한: `OPS_ADMIN | OPS_VIEWER | AUDITOR`
- 설명: Dashboard와 문서에서 공통으로 사용하는 한글 도메인 용어집 조회
- 쿼리:
  - `domainKey` optional
    - 예: `ACCOUNT`, `FUNDING`, `STOCK`, `CONTROL`

## 4. 요청 (입출금/환전)

### GET `/exchange-rates`
- 권한: `OPS_ADMIN | OPS_VIEWER | AUDITOR`
- 설명: 통화쌍별 기준 환율 조회
- 쿼리:
  - `fromCurrency`, `toCurrency`, `rateDate`
  - `page`, `size`

### GET `/exchange-rates/quote`
- 권한: `OPS_ADMIN | OPS_VIEWER | AUDITOR`
- 설명: 요청 금액 기준 환전 예상 금액 계산
- 쿼리:
  - `fromCurrency`, `toCurrency`, `amount`
  - `rateDate` optional
- 응답:
  - `exchangeRate`, `convertedAmount`, `rateDate`, `source`, `quoteMode`

### POST `/exchange-rates`
- 권한: `OPS_ADMIN`
- 설명: 환율 등록 또는 동일 일자 환율 갱신

### POST `/cash-requests`
- 권한: `OPS_ADMIN`
- 설명: 입출금 요청 생성
- 주요 입력: `accountId`, `type`, `amount`, `currency`, `reason`, `priority`, `valueDate`
- 응답 통제 필드:
  - 승인정책: `controlPolicyId`, `controlPolicySource`
  - 리스크한도: `controlLimitPolicyId`, `controlLimitPolicySource`, `projectedDailyExposure`

### GET `/cash-requests`
- 권한: `OPS_ADMIN | OPS_VIEWER | AUDITOR`
- 쿼리: `status`, `accountId`, `keyword`, `filter`, `page`, `size`

### GET `/cash-requests/{requestId}`
- 권한: `OPS_ADMIN | OPS_VIEWER | AUDITOR`

### POST `/fx-requests`
- 권한: `OPS_ADMIN`
- 설명: 환전 요청 생성
- 주요 입력: `accountId`, `fromCurrency`, `toCurrency`, `amount`, `reason`, `priority`, `valueDate`
- 응답 통제 필드:
  - 승인정책: `controlPolicyId`, `controlPolicySource`
  - 리스크한도: `controlLimitPolicyId`, `controlLimitPolicySource`, `projectedDailyExposure`
  - 환율정보: `exchangeRate`, `expectedToAmount`, `exchangeRateDate`, `exchangeRateSource`

### GET `/fx-requests`
- 권한: `OPS_ADMIN | OPS_VIEWER | AUDITOR`
- 쿼리: `status`, `accountId`, `keyword`, `filter`, `page`, `size`

### GET `/fx-requests/{requestId}`
- 권한: `OPS_ADMIN | OPS_VIEWER | AUDITOR`

### POST `/requests/{requestId}/approve`
- 권한: `OPS_ADMIN`
- 요청
```json
{ "reason": "승인 사유" }
```
- 동작: 4-eyes + 수동심사 사유 길이 검증 + 브로커 전송(최대 3회 재시도)

### POST `/requests/{requestId}/reject`
- 권한: `OPS_ADMIN`
- 요청
```json
{ "reason": "반려 사유" }
```

## 5. 주식 매수 / 포지션 / 원장 / 분개

### GET `/portfolios/{accountId}`
- 권한: `OPS_ADMIN | OPS_VIEWER | AUDITOR`
- 설명: 계좌별 포트폴리오 개요 조회
- 쿼리:
  - `unmask` optional, 관리자만 true 허용
- 응답:
  - 현금 잔고 목록
  - 통화별 주식 장부원가
  - 보유 종목 목록
  - 최근 매수 내역

### POST `/stock-purchases`
- 권한: `OPS_ADMIN`
- 설명: 주식 매수 거래 생성 후 포지션, 원장, 분개를 함께 반영
- 주요 입력:
  - `accountId`, `symbol`, `market`, `currency`
  - `tradeDate`, `settlementDate`
  - `quantity`, `price`, `feeAmount`
- 후속 반영:
  - `stock_positions` 업서트
  - `ledger_entries` 1건 생성
  - `journal_entries` 차/대변 라인 생성

### GET `/stock-purchases`
- 권한: `OPS_ADMIN | OPS_VIEWER | AUDITOR`
- 쿼리: `accountId`, `symbol`, `keyword`, `filter`, `page`, `size`

### GET `/stock-positions`
- 권한: `OPS_ADMIN | OPS_VIEWER | AUDITOR`
- 쿼리: `accountId`, `symbol`, `filter`, `page`, `size`

### POST `/stock-recommendations`
- 권한: `OPS_ADMIN | OPS_VIEWER`
- 설명: Ollama + Spring AI 기반 종목 추천 초안 생성
- 주요 입력:
  - `accountId`
  - `riskProfile`: `CONSERVATIVE | BALANCED | AGGRESSIVE`
  - `investmentHorizon`: `SHORT_TERM | MEDIUM_TERM | LONG_TERM`
  - `maxRecommendations` 최대 5
  - `preferredMarkets`, `candidateSymbols`
  - `operatorView`
- 응답:
  - `summary`, `cautionPoints`
  - 추천 종목별 `action`, `confidence`, `allocationHint`, `rationale`, `riskNotes`
  - 포트폴리오 스냅샷과 운영용 disclaimer 포함

### GET `/ledger-entries`
- 권한: `OPS_ADMIN | OPS_VIEWER | AUDITOR`
- 쿼리: `accountId`, `referenceId`, `filter`, `page`, `size`

### GET `/journal-entries`
- 권한: `OPS_ADMIN | OPS_VIEWER | AUDITOR`
- 쿼리: `accountId`, `journalNo`, `referenceId`, `filter`, `page`, `size`

## 6. 배치

### GET `/batches/runs`
- 권한: `OPS_ADMIN | OPS_VIEWER`
- 쿼리: `date`, `status`, `keyword`, `filter`, `page`, `size`

### GET `/batches/runs/{runId}`
- 권한: `OPS_ADMIN | OPS_VIEWER`

### GET `/batches/schedules`
- 권한: `OPS_ADMIN | OPS_VIEWER`
- 설명: Quartz trigger 상태/cron/다음 실행시각 조회

### POST `/batches/schedules/{triggerName}/pause`
- 권한: `OPS_ADMIN`

### POST `/batches/schedules/{triggerName}/resume`
- 권한: `OPS_ADMIN`

### POST `/batches/run-now?batchName=POSITION_SYNC`
- 권한: `OPS_ADMIN`
- 허용 배치명: `POSITION_SYNC`, `MARGIN_RECALC`, `EOD_SETTLEMENT`

## 7. 감사로그

### GET `/audit-logs`
- 권한: `OPS_ADMIN | AUDITOR`
- 쿼리: `actor`, `action`, `keyword`, `filter`, `from`, `to`, `page`, `size`

## 8. 메뉴

### GET `/menus/my`
- 권한: `OPS_ADMIN | OPS_VIEWER | AUDITOR`
- 설명: 현재 로그인 역할 기준 사이드 메뉴 반환

### GET `/menus`
- 권한: `OPS_ADMIN | AUDITOR`
- 쿼리: `keyword`, `filter`, `page`, `size`

## 9. 승인정책

### GET `/approval-policies`
- 권한: `OPS_ADMIN | AUDITOR`
- 쿼리: `keyword`, `filter`, `page`, `size`

### POST `/approval-policies`
- 권한: `OPS_ADMIN`
- 설명: 정책 생성

### PUT `/approval-policies/{id}`
- 권한: `OPS_ADMIN`
- 설명: 정책 수정

## 10. 리스크 한도 정책

### GET `/risk-limits`
- 권한: `OPS_ADMIN | AUDITOR`
- 쿼리: `keyword`, `filter`, `page`, `size`

### POST `/risk-limits`
- 권한: `OPS_ADMIN`
- 설명: 리스크 한도 정책 생성

### PUT `/risk-limits/{id}`
- 권한: `OPS_ADMIN`
- 설명: 리스크 한도 정책 수정

## 11. 운영 예외 케이스 (Ops Cases)

### GET `/ops-cases`
- 권한: `OPS_ADMIN | OPS_VIEWER | AUDITOR`
- 쿼리: `status`, `severity`, `assignee`, `keyword`, `filter`, `page`, `size`

### GET `/ops-cases/{id}`
- 권한: `OPS_ADMIN | OPS_VIEWER | AUDITOR`

### POST `/ops-cases`
- 권한: `OPS_ADMIN`
- 설명: 운영 예외 케이스 수동 등록

### PUT `/ops-cases/{id}`
- 권한: `OPS_ADMIN`
- 설명: 케이스 메타데이터(제목/설명/심각도/담당자/기한) 수정

### POST `/ops-cases/{id}/start`
- 권한: `OPS_ADMIN`
- 설명: `OPEN -> IN_PROGRESS` 전이

### POST `/ops-cases/{id}/resolve`
- 권한: `OPS_ADMIN`
- 설명: `OPEN|IN_PROGRESS -> RESOLVED` 전이 + 해결 요약 기록

### POST `/ops-cases/{id}/close`
- 권한: `OPS_ADMIN`
- 설명: `RESOLVED -> CLOSED` 전이

### POST `/ops-cases/{id}/reopen`
- 권한: `OPS_ADMIN`
- 설명: `RESOLVED|CLOSED -> OPEN` 전이

### 자동 연동
- 요청 승인 처리에서 브로커 전송이 최종 `FAILED`되면 `REQUEST_FAILURE` 케이스가 자동 생성됩니다.

## 12. 표준 오류 응답
- 인증 실패: `401`
- 권한 부족: `403`
- 검증/업무 규칙 위반: `400`
- 미존재 리소스: `404`
- 서버 오류: `500`
