# RSQL Filter 가이드

## 1. 개요
리스트 API는 `filter` 쿼리 파라미터로 RSQL 조건을 받을 수 있습니다.

예시:
- `/api/v1/accounts?filter=status==ACTIVE;broker=='CME'`
- `/api/v1/cash-requests?filter=status==PENDING;amount=ge=10000`

## 2. 지원 연산자
백엔드(Querydsl RSQL 파서) 지원:
- `==`, `!=`
- `=like=`
- `=gt=`, `=ge=`, `=lt=`, `=le=`
- `=in=`, `=out=`

프론트 공통 빌더 기본 지원:
- `==`, `!=`, `=like=`, `=gt=`, `=ge=`, `=lt=`, `=le=`

## 3. 표현 규칙
- AND: `;`
- OR: `,`
- 문자열은 따옴표 권장: `broker=='CME'`
- Enum 값은 대문자 권장: `status==PENDING`
- 날짜/시간은 ISO 포맷 권장
  - 날짜: `2026-02-27`
  - 일시: `2026-02-27T00:00:00Z`

## 4. 엔드포인트별 필터 필드

## Accounts (`/accounts`)
- `id`, `accountNo`, `broker`, `status`, `ownerName`, `openedAt`, `closedAt`

## Cash Requests (`/cash-requests`)
- 요청 필드
  - `id`, `status`, `type`, `currency`, `amount`, `priority`, `valueDate`
  - `manualReviewRequired`, `controlReason`, `controlPolicyId`, `controlPolicySource`
  - `controlLimitPolicyId`, `controlLimitPolicySource`, `projectedDailyExposure`, `slaDueAt`
  - `requestedBy`, `reviewedBy`, `requestedAt`, `reviewedAt`
  - `reason`, `reviewReason`
- 조인 계좌 필드
  - `accountId`, `accountNo`, `accountBroker`, `accountOwnerName`, `accountStatus`

## FX Requests (`/fx-requests`)
- 요청 필드
  - `id`, `status`, `fromCurrency`, `toCurrency`, `amount`, `priority`, `valueDate`
  - `manualReviewRequired`, `controlReason`, `controlPolicyId`, `controlPolicySource`
  - `controlLimitPolicyId`, `controlLimitPolicySource`, `projectedDailyExposure`, `slaDueAt`
  - `requestedBy`, `reviewedBy`, `requestedAt`, `reviewedAt`
  - `reason`, `reviewReason`
- 조인 계좌 필드
  - `accountId`, `accountNo`, `accountBroker`, `accountOwnerName`, `accountStatus`

## Batch Runs (`/batches/runs`)
- `id`, `batchName`, `status`, `startedAt`, `finishedAt`, `errorMessage`, `retryCount`

## Audit Logs (`/audit-logs`)
- `id`, `actor`, `action`, `targetType`, `targetId`, `details`, `createdAt`

## Menus (`/menus`)
- `id`, `menuKey`, `title`, `path`, `resourceName`, `icon`, `sortOrder`, `enabled`, `rolesCsv`, `createdAt`

## Approval Policies (`/approval-policies`)
- `id`, `brokerCode`, `domain`
- `highThreshold`, `urgentThreshold`, `manualReviewThreshold`
- `sameDayAutoReview`, `enabled`
- `effectiveFrom`, `effectiveTo`, `description`, `createdAt`

## Risk Limits (`/risk-limits`)
- `id`, `brokerCode`, `domain`, `currencyCode`
- `maxPerRequest`, `dailySoftLimit`, `dailyHardLimit`
- `enabled`, `effectiveFrom`, `effectiveTo`, `description`, `createdAt`

## Ops Cases (`/ops-cases`)
- `id`, `caseNo`, `category`, `severity`, `status`
- `title`, `description`, `assignee`, `dueAt`
- `linkedType`, `linkedId`, `accountId`
- `resolutionSummary`
- `createdBy`, `updatedBy`, `resolvedBy`, `resolvedAt`, `closedBy`, `closedAt`
- `createdAt`, `updatedAt`

## 5. 예시
- 계좌: `status==ACTIVE;broker=='CME'`
- 입출금: `status==PENDING;priority=in=(HIGH,URGENT);manualReviewRequired==true`
- 환전: `fromCurrency=='USD';toCurrency=='KRW';amount=ge=100000`
- 배치: `status==FAILED;retryCount=ge=1`
- 감사로그: `actor=='opsadmin';action=like='APPROVE'`
