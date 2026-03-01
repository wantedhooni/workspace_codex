# 기능 설명서

## 1. 시스템 목적
해외 파생상품 운영의 핵심 업무를 단일 내부 웹에서 처리하기 위한 MVP입니다.

핵심 업무 범위:
- 계좌/잔고/포지션/증거금 조회
- 입출금/환전 요청 생성 및 승인/반려
- 배치 실행 이력/스케줄 조회 및 제어
- 감사로그/보안로그 기반 통제
- DB 기반 메뉴 제어
- DB 기반 도메인 용어집 제공

## 2. 역할과 권한

| 기능 | OPS_ADMIN | OPS_VIEWER | AUDITOR |
|---|---|---|---|
| 로그인 | O | O | O |
| 계좌 목록/요약 조회 | O | O | O |
| 입출금 요청 생성 | O | X | X |
| 환전 요청 생성 | O | X | X |
| 요청 승인/반려 | O | X | X |
| 요청 목록/상세 조회 | O | O | O |
| 환율 조회 | O | O | O |
| 환율 등록/갱신 | O | X | X |
| 포트폴리오 조회 | O | O | O |
| 주식 매수 생성 | O | X | X |
| 주식 매수/포지션/원장/분개 조회 | O | O | O |
| AI 종목 추천 조회/생성 | O | O | X |
| 배치 실행 이력 조회 | O | O | X |
| 배치 스케줄 조회 | O | O | X |
| 배치 수동실행/일시정지/재개 | O | X | X |
| 감사로그 조회 | O | X | O |
| 리스크 한도 정책 조회 | O | X | O |
| 리스크 한도 정책 생성/수정 | O | X | X |
| 운영 예외 케이스 조회 | O | O | O |
| 운영 예외 케이스 생성/상태전이 | O | X | X |
| 메뉴 관리(조회) | O | X | O |
| 계좌번호 마스킹 해제 | O | X | X |

## 3. 도메인별 핵심 로직

### 3.1 인증/보안
- JWT 기반 인증 (`/api/v1/auth/login`)
- 토큰 만료: 3600초
- 비밀번호 해시: BCrypt
- 인증 실패/권한 실패는 `SECURITY_AUDIT` 로거에 분리 기록
- 로그인 성공/주요 변경행위는 감사로그에 기록

### 3.2 계좌/포지션
- 계좌 목록 조회 + 검색(keyword) + RSQL(filter)
- 계좌 요약에서 잔고/포지션/증거금을 통합 조회
- `unmask=true`는 관리자만 허용

### 3.3 현금/환전 요청 워크플로우
- 상태: `PENDING -> APPROVED/REJECTED/FAILED`
- 4-eyes 통제: 요청자와 승인자 동일 불가
- 중복 요청 통제: 동일 요청이 10분 내 PENDING으로 존재하면 차단
- 잔고 통제: 출금/환전 시 최신 잔고 대비 부족하면 차단
- 컷오프 통제: 브로커별 당일 컷오프 이후 same-day 요청 차단
- 고위험/대규모 요청 자동 수동심사 플래그(`manualReviewRequired`) 부여
- 승인 시 브로커 어댑터 호출 최대 3회 재시도 후 `APPROVED/FAILED` 확정
- 환전 요청 생성 시 최신 환율을 조회해 `exchangeRate`, `expectedToAmount`를 함께 저장

### 3.4 환율(Exchange Rate) 도메인
- `exchange_rates` 테이블로 통화쌍별 기준 환율 관리
- 동일 통화쌍/일자 입력 시 upsert
- `quote` API는 직접 환율이 없으면 역환율을 뒤집어 계산
- 프론트 `Exchange Rates` 화면과 FX 요청 생성 팝업에서 공통 사용
### 3.5 배치 운영 (Spring Batch + Quartz)
- 배치 실행 엔진: `opsBatchJob`
- 스케줄 대상: `POSITION_SYNC`, `MARGIN_RECALC`, `EOD_SETTLEMENT`
- Quartz 스케줄 조회/일시정지/재개 지원
- UI에서 수동 실행(`run-now`) 지원
- 배치 실행 결과는 `batch_runs`에 누적

### 3.6 주식 매수 / 포지션 / 원장 / 분개
- 주식 매수 등록 시 계좌 상태와 수량/단가/수수료 유효성을 검증
- 매수 체결 저장 후 동일 트랜잭션에서 `stock_positions`를 업서트
- 원장(`ledger_entries`)에는 종목별 수량 증감과 누적 잔량/금액을 기록
- 분개(`journal_entries`)에는 재고자산 차변, 수수료비용 차변, 현금 대변을 생성
- 거래 생성 행위는 감사로그 `CREATE_STOCK_PURCHASE`로 남김

### 3.7 포트폴리오(Portfolio) 도메인
- `portfolios/{accountId}`에서 계좌 기준 현금 잔고, 주식 보유, 통화별 장부원가, 최근 매수 내역을 함께 조회
- 다통화 계좌를 고려해 총원가는 단일 합계가 아니라 통화별 리스트로 관리
- 프론트 `Portfolio` 화면에서 증권 운영자가 계좌 단위 자산 구성을 빠르게 점검할 수 있음

### 3.8 AI 종목 추천 도메인
- `stock-recommendations` API가 계좌 포트폴리오 스냅샷과 운용 입력값을 조합해 추천 초안을 생성
- 실행환경은 `Ollama + Spring AI`를 사용하고, 테스트 환경은 stub provider로 고정
- 결과는 실제 주문 지시가 아니라 운영 검토용 추천 초안이며 항상 disclaimer를 포함
- 응답에는 추천 종목, 액션(`BUY/WATCH/HOLD`), 신뢰도, 주의사항, 포트폴리오 스냅샷이 포함됨

### 3.9 메뉴 DB 제어
- 메뉴 테이블(`menus`) 기반으로 좌측 Sider 구성
- `/api/v1/menus/my`에서 현재 사용자 역할에 허용된 메뉴만 반환
- 정렬 기준: `sortOrder`

### 3.10 도메인 용어집(Domain Terms)
- 용어 테이블(`domain_terms`) 기반으로 Dashboard와 문서를 같은 기준으로 유지
- 도메인별(`계좌/잔고`, `입출금/환전`, `주식/회계`, `통제/운영`) 핵심 용어를 한글로 제공
- 각 용어는 영문 원어, 한글 명칭, 설명, 예시를 함께 보관

### 3.11 승인정책(Approval Policy) 도메인
- 정책 테이블(`approval_policies`)로 브로커/요청도메인별 통제 임계치 관리
- 정책 항목:
  - `highThreshold`, `urgentThreshold`, `manualReviewThreshold`
  - `sameDayAutoReview`, `enabled`, `effectiveFrom`, `effectiveTo`
- 요청 생성 시 정책을 조회해 아래를 동적으로 계산
  - 우선순위 최소 레벨 (`HIGH`, `URGENT`)
  - 수동심사 플래그(`manualReviewRequired`)
  - 통제사유(`controlReason`)
- 요청 데이터에 정책 추적 필드 저장
  - `controlPolicyId`, `controlPolicySource`

### 3.12 리스크 한도 정책(Risk Limit Policy) 도메인
- 정책 테이블(`risk_limit_policies`)로 브로커/요청도메인/통화별 한도 관리
- 정책 항목:
  - `maxPerRequest`, `dailySoftLimit`, `dailyHardLimit`
  - `enabled`, `effectiveFrom`, `effectiveTo`
- 요청 생성 시 동작:
  - 단건 한도(`maxPerRequest`) 초과 시 요청 생성 차단
  - 당일 누적 예상노출(`projectedDailyExposure`)이 하드한도 초과 시 생성 차단
  - 소프트한도 초과 시 `manualReviewRequired` 및 `controlReason` 자동 반영
- 요청 데이터에 정책 추적 필드 저장
  - `controlLimitPolicyId`, `controlLimitPolicySource`, `projectedDailyExposure`

### 3.13 운영 예외 케이스(Ops Case) 도메인
- 운영 실패/통제 위반 후속조치 트래킹을 위한 케이스 관리 도메인
- 상태: `OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED` (필요 시 reopen)
- 주요 속성:
  - `caseNo`, `category`, `severity`, `status`
  - `assignee`, `dueAt`, `linkedType`, `linkedId`, `accountId`
  - `resolutionSummary`
- 자동화:
  - 요청 승인 과정에서 브로커 제출이 최종 실패하면 `REQUEST_FAILURE` 케이스 자동 생성
- 감사:
  - 생성/수정/상태전이/재오픈 모두 감사로그 기록

## 4. 감사/통제 포인트
- 감사로그 대상: 로그인 성공, 요청 생성/승인/반려/실패
- 보안로그 대상: 인증 실패(401), 권한 거부(403)
- 요청 데이터에 통제 메타데이터 포함:
  - `priority`, `valueDate`, `slaDueAt`
  - `manualReviewRequired`, `controlReason`

## 5. 데모/초기 데이터
- 계정
  - `opsadmin / admin123!`
  - `opsadmin2 / admin234!`
  - `opsviewer / viewer123!`
  - `auditor / audit123!`
- 계좌/잔고/포지션/증거금 샘플 데이터
- 환율/환전 예상금액 샘플 데이터
- 포트폴리오/주식 화면용 샘플 데이터
- AI 종목 추천 화면 및 메뉴 샘플 데이터
- 주식 매수/포지션/원장/분개 샘플 데이터
- 도메인 용어집 샘플 데이터
- 요청/배치/감사로그 샘플 데이터
- 메뉴 샘플 데이터 (역할별 노출)
