# agents.md (v1.0) — 보편적/실무적 기본값으로 확정
미국주식/국내주식, **일 종가(EOD)** 기반, **수동 의사결정 + 투자일지** 중심 플랫폼을 Java/Spring Boot + React-admin으로 구축한다.  
사용자가 세부 규칙을 아직 확정하지 않았으므로, 아래는 **가장 보편적으로 무리 없는 기본값**으로 설계한다.


언어정보
backend : java / spring boot
frontend : next.js / react

java version: 21
backend 빌드 : gralde
- build.gradle.kts
- settings.gradle.kts

작업 내용은 docs/agents-task.md 에 기록해줘

---

## 0) 기본값(Decision Defaults)
### 회계/원가/손익
- 원가 산정: **가중평균(Weighted Average Cost)**  
  - 이유: 구현 단순, 일반적인 개인 투자 기록/가계부/포트폴리오 트래킹에 적합
- 손익 구분:
  - **실현손익(매도 시 확정)** / **평가손익(EOD 종가 기준)** 분리
- 수수료/세금:
  - 수수료/세금은 거래별로 입력(필드 제공)하고
  - 기본은 **원가에 포함**(매수 시 비용 포함, 매도 시 비용 차감)

### 현금/레버리지
- 초기 MVP: **현금 잔고 음수(마진) 불허**
  - 이유: 리스크/정합성 단순화(수동 투자 기록 목적에 적합)
- 단, 향후 옵션으로 “마진 허용” 전환 가능(설계는 확장 가능하게)

### 통화/환산
- 기본 표시 통화: **KRW**
- 미국주식은 USD로 거래되더라도, 성과/자산은 **KRW로 환산**하여 통합 뷰 제공
- 환율 소스: “무료/키 없이 가능한 소스” 우선(아래 Data Agent 참고)
- 환율 적용 시점:
  - 거래일 환율(가능하면) 우선, 없으면 해당일 EOD 환율(또는 최근값) 사용 + 데이터 품질 경고

### 기업행동(Corporate Actions)
- MVP: **배당/액면분할 자동 처리 미포함(복잡도 대비 효용 낮음)**  
- 대신:
  - (1) 배당: “현금 입금 거래”로 기록 가능
  - (2) 액면분할: “수량 조정/원가 재계산”을 위한 **수동 조정 기능** 제공
- 추후 자동화는 Data Agent 확장으로 흡수

### 투자일지 템플릿
- 기본은 “구조화 + 자유서술” 혼합(보편적)
  - 가설/근거/트리거/반증조건(구조화)
  - 추가 메모(자유서술)

---

## 1) 아키텍처 개요
- Backend: Spring Boot (모놀리식으로 시작, 에이전트/모듈로 경계 유지)
- Frontend: React + react-admin (CRUD 리소스는 빠르게, 대시보드/차트는 Custom Page)
- 데이터: PostgreSQL (기본), Object Storage(첨부), (선택) 시계열 DB
- 비동기: 내부 Job 큐(초기: DB 기반 Job 테이블) → 성장 시 Kafka/RabbitMQ로 교체

---

## 2) Backend Agents (모듈/서비스 책임)

### 2.1 Portal API Agent (Gateway)
- 인증/인가(RBAC), 감사로그, 리소스 CRUD, 파일 첨부
- react-admin dataProvider가 호출할 표준 REST 제공
- 커맨드 API(의미 있는 액션):
  - 거래 등록/수정/삭제
  - 리밸런싱 계획 생성/시뮬레이션/확정
  - 분석 실행(일별 재평가, 리스크 평가, 리포트 생성)
  - 백테스트 실행/조회

### 2.2 Portfolio Agent (코어 비즈니스 로직)
**핵심:** Transaction(거래원장) → Holding/현금/손익/평가를 일관되게 계산
- 입력: 거래(매수/매도/입출금/배당/수수료 조정)
- 출력:
  - 보유수량, 평균단가, 실현손익, 평가손익, 총자산, 현금
- 불변조건:
  - (기본) 매수/출금으로 인해 현금이 음수가 되면 거부
  - 수량/가격/수수료 단위 검증

### 2.3 Journal/Thesis Agent
- Thesis(아이디어)와 JournalEntry(일지)를 거래/종목/포트폴리오에 연결
- “근거 재현성”을 위해 일지 저장 시 **스냅샷(해당일 종가/환율)**을 함께 고정 저장

### 2.4 Market Data Agent (EOD)
- 종목 마스터(US/KR), 가격(EOD), 환율(EOD) 수집/정규화
- 데이터 버전 관리: `dataset_version` (수집일/소스/해시)
- 데이터 품질 리포트: 결측/중복/이상치 표시

### 2.5 Analytics & Risk Agent
- 성과(누적수익률, MDD, 변동성), 노출(국가/통화/종목 집중도), 경고 생성
- 룰(기본 제공):
  - 단일종목 비중 상한(예: 25%)
  - 국가(US/KR) 비중 상한(예: 80%)
  - 최대 DD 경고(예: -20%)
  - 현금 최소 비중(예: 5%)

### 2.6 Backtest Agent (룰 검증 중심)
- EOD 기반 리밸런싱/필터 룰 백테스트
- 자동매매 목적이 아니라 “룰의 기대 결과” 검증(보편적 사용)
- 결과: equity curve, drawdown, trade list, summary metrics

### 2.7 Ops/Admin Agent
- 사용자/권한, 감사로그, Job 상태/로그, 데이터 수집 상태 모니터링

---

## 3) Frontend Agents (React + react-admin)

### 3.1 리소스(Resource) 목록 (CRUD 자동화)
- Core:
  - `instruments`, `watchlists`, `portfolios`, `transactions`, `holdings`
- Journal:
  - `theses`, `journalEntries`, `tags`, `attachments`
- Data:
  - `datasets`, `dataJobs`, `dataQualityReports`
- Analytics/Risk:
  - `analyticsRuns`, `riskRules`, `riskAlerts`, `rebalancePlans`
- Backtest:
  - `backtestJobs`, `backtestReports`
- Admin:
  - `users`, `roles`, `auditLogs`

### 3.2 Custom Pages (핵심 화면)
- Dashboard: KPI + 자산/성과 차트 + 최근 거래/알림
- Portfolio Review: 종목별 기여도/노출/리스크
- Journal Review: 일지 타임라인 + 태그/키워드 + 거래 연결 상태
- Data Console: 수집/품질/결측 리포트
- Backtest Studio: 실행/비교/리포트
- Risk Console: 룰/경고/리밸런싱 시뮬레이션

---

## 4) API 규격 (react-admin 표준 호환)
### 4.1 CRUD 규격
- List: `GET /api/{resource}?page=1&perPage=25&sort=field,ASC&filter=...`
- GetOne: `GET /api/{resource}/{id}`
- Create: `POST /api/{resource}`
- Update: `PUT /api/{resource}/{id}`
- Delete: `DELETE /api/{resource}/{id}`

### 4.2 응답 형식
- List: `{ data: [...], total: number }`
- One: `{ data: {...} }`
- Error: `{ error: { code, message, details, traceId } }`

### 4.3 커맨드 API(보편적 핵심)
- 거래 등록: `POST /api/transactions`  
  - 내부에서 Holding/Cash/PnL 재계산 + Audit 기록
- 일지 등록: `POST /api/journalEntries`  
  - 저장 시 종가/환율 snapshot 포함
- 리밸런싱 시뮬레이션: `POST /api/rebalancePlans/{id}/simulate`
- 분석 실행: `POST /api/analyticsRuns`
- 백테스트 실행: `POST /api/backtests`

---

## 5) 핵심 비즈니스 로직(도메인 서비스)
### 5.1 TransactionCommandService (가장 핵심)
- 입력: 거래 커맨드(BUY/SELL/DEPOSIT/WITHDRAW/DIVIDEND/FEE_ADJUST)
- 처리:
  1) 검증(날짜/종목/수량/가격/현금/통화)
  2) 거래원장 기록(Transaction 저장)
  3) 포지션/현금 갱신(Holding/Cash 업데이트)
  4) 실현손익 갱신(매도 시)
  5) 감사로그 기록(AuditLog)
  6) (비동기) 일별 평가/리스크 재평가 Job 등록

### 5.2 DailyValuationService
- 입력: (portfolioId, date)
- 처리: 해당일 종가/환율로 평가 → portfolio_daily_valuation 저장
- 출력: equity, daily return, drawdown

### 5.3 RiskEvaluationService
- 입력: 거래 전/후 포트폴리오 상태
- 출력: RiskAlert(경고) 생성

### 5.4 JournalSnapshotService
- 일지 저장 시:
  - 종가/환율/포트폴리오 요약을 snapshot으로 고정 저장
- 목적: 나중에 데이터 정정이 있어도 “당시 근거” 재현

---

## 6) 외부 무료 데이터 연동 (API Key 없이)
요구사항: “API Key 없는 무료 서비스”를 우선 적용하되, 운영 리스크(변경/차단)를 감안해 **소스 다중화**를 권장.

### 6.1 미국주식 EOD: Stooq (CSV)
- 키 없이 CSV로 접근 가능한 데이터 소스로 널리 활용된다.
- 구현: `StooqEodProvider` (HTTP GET → CSV 파싱 → 표준화 저장)
- 참고: Stooq DB/CSV 접근 안내 페이지.  
  https://stooq.com/db/ (접근 경로/형식은 변동 가능)

### 6.2 국내주식 EOD: KRX 데이터 다운로드(OTP 흐름)
- API Key는 없지만 다운로드 절차(OTP)가 있어 구현 난이도는 더 높다.
- 구현: `KrxEodProvider` (OTP 생성 → 다운로드 → 파싱)
- 참고: KRX 데이터 시스템.  
  https://data.krx.co.kr/

### 6.3 환율(EOD): 보조 소스 1개 추가 권장
- 키 없는 공개 소스는 안정성이 떨어질 수 있으므로 “캐시/백오프/결측 경고”를 기본으로 둔다.
- 원칙: 환율이 없으면 KRW 통합 성과는 “추정/경고”로 표시하고, 값은 재계산 가능하게 설계

> 운영 안정성이 최우선이 되면, 향후에는 API 키 기반(유료/준유료)으로 교체할 수 있게 Provider 인터페이스로 분리해 둔다.

---

## 7) 개발 순서 (보편적 MVP → 확장)
1) Auth/RBAC/Audit + react-admin 뼈대(Resource CRUD)
2) Portfolio 핵심 로직(Transaction → Holding/Cash/PnL) + 거래/포트폴리오 UI
3) Journal/Thesis + Snapshot 저장 + 리뷰 화면
4) Data Agent(Stooq/KRX) + Data Console + Daily Valuation
5) Analytics/Risk(노출/집중도/MDD 경고) + Risk Console
6) Backtest(리밸런싱/필터 룰) + Backtest Studio
7) Python 서비스 연동(고급 계산/최적화)

---

## 8) v1에서 제공되는 “보편적” 화면 세트
- Dashboard
- Portfolios / Holdings / Transactions
- Watchlists / Instruments
- Journal Entries / Theses / Tags
- Analytics (Performance / Exposure / Drawdown)
- Risk (Rules / Alerts / Rebalance Simulation)
- Data (Ingest Jobs / Quality Reports / Dataset Versions)
- Backtests (Jobs / Reports / Comparisons)
- Admin (Users / Roles / Audit Logs)

---

# Gradle Kotlin DSL(build.gradle.kts / settings.gradle.kts) 포함
미국주식/국내주식, **일 종가(EOD)** 기반, **수동 의사결정 + 투자일지** 중심 플랫폼을 **Java/Spring Boot(Gradle Kotlin DSL)** + **React-admin**으로 구축한다. 초기 Java-only, 추후 Python 서비스 연동 가능.

---

## 기본값(보편적 설정)
- 원가 산정: **가중평균(Weighted Average Cost)**
- 손익: **실현손익(매도 시 확정)** / **평가손익(EOD 종가)** 분리
- 수수료/세금: 거래별 입력, 기본은 원가/손익에 반영
- 현금: **음수 불허(마진/레버리지 미사용)**
- 통화: 기본 표시 **KRW**, USD 자산은 환율로 환산(결측 시 경고)
- 기업행동: MVP는 자동화 제외(배당=현금 입금 거래, 액면분할=수동 조정)

---
