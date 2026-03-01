# Frontend 기능 가이드

## 1. 공통 구조
- 프레임워크: Refine + React + MUI
- 레이아웃: `ThemedLayoutV2`
- 사이드 메뉴: DB 기반 (`/menus/my`)
- 라우팅
  - `/` Dashboard
  - `/domain-terms`
  - `/portfolio`
  - `/stock-purchases`
  - `/stock-positions`
  - `/stock-recommendations`
  - `/exchange-rates`
  - `/accounts`, `/accounts/:id`
  - `/cash-requests`, `/cash-requests/:id`
  - `/fx-requests`, `/fx-requests/:id`
  - `/batches`
  - `/audit-logs`
  - `/approval-policies`
  - `/risk-limits`
  - `/ops-cases`, `/ops-cases/:id`
  - `/menus`

## 2. 로그인
- 로그인 페이지 기본값
  - Username: `opsadmin`
  - Password: `admin123!`
- 데모 계정 안내 문구 표시

## 3. 화면별 기능

### Dashboard
- 오늘 실패 배치 건수
- 입출금 승인 대기 건수
- 환전 승인 대기 건수
- DB 기반 `도메인 용어집` 카드 표시
- 용어는 한글 설명과 예시를 함께 보여줌
- 페이지 제목 tooltip으로 화면 설명 제공

### Domain Terms
- DB 기반 도메인 용어집 전용 화면
- 용어 검색: 도메인명, 한글명, 영문명, 설명, 예시 기준
- 업무 용어를 도메인별 카드로 조회

### Portfolio
- 계좌 선택 후 현금 잔고, 통화별 장부원가, 주식 보유, 최근 매수 내역 조회
- 관리자만 계좌번호 마스킹 해제 가능

### Stock Purchases
- 매수 이력 검색: keyword + symbol/accountId 필터
- 관리자 기능: 매수 거래 등록
- 거래 후 포지션/원장/분개는 백엔드에서 자동 반영

### Stock Positions
- 종목/계좌 기준 현재 보유 현황 조회
- 평균단가와 총원가를 같이 표시

### Stock Recommendations
- 계좌, 리스크 성향, 투자기간, 선호 시장, 후보 종목, 운용 메모 입력
- Ollama 기반 AI 추천 초안 생성
- 결과:
  - 추천 요약
  - 종목별 액션/신뢰도/비중 힌트/리스크
  - 주의사항
  - 추천 시점 포트폴리오 스냅샷
- 실제 주문 전 검토용 화면이며 자동 주문 기능은 없음

### Exchange Rates
- 통화쌍 기준 환율 조회
- 관리자 기능: 환율 등록/갱신
- `from/to/rateDate` 기준 검색 가능

### FX Requests
- 생성 팝업에서 환율과 예상 수취금액 미리보기 제공
- 리스트/상세에서 적용 환율, 예상 수취금액, 환율 소스 표시

### Accounts
- keyword 검색 + filter 입력(Broker/Status)
- 계좌번호 마스킹 해제 토글(관리자만)
- Summary 버튼으로 상세 페이지 이동

### Cash Requests / FX Requests
- 리스트 검색: keyword + RSQL filter
- 생성(Create): 모달에서 입력 후 생성
- 승인/반려: 모달에서 사유 입력 후 확정
- 상세보기: Detail 버튼으로 상세 페이지 이동
- 상세 페이지: 통제 필드(우선순위/수동심사/SLA/사유/정책ID/정책소스) 포함 표시

### Batch Runs
- 실행 이력 검색: keyword + 상태/날짜 필터
- Quartz 스케줄 테이블 조회
- 관리자 기능: Run Now, Pause, Resume

### Audit Logs
- actor/action/from/to + keyword 검색
- RSQL filter 기반 상세 필터링

### Menus
- 메뉴 DB 조회 리스트
- Enabled 여부 필터

### Approval Policies
- 정책 리스트 검색: keyword + RSQL filter
- 정책 생성/수정: 모달 처리
- 브로커/도메인/임계치/유효기간/same-day 자동심사 여부 관리

### Risk Limits
- 한도정책 리스트 검색: keyword + RSQL filter
- 한도정책 생성/수정: 모달 처리
- 브로커/도메인/통화별 `Max/Req`, `Daily Soft`, `Daily Hard` 관리
- 요청 화면(Cash/FX)에서 리스크 정책 소스 및 예상노출 컬럼 표시

### Ops Cases
- 리스트 검색: keyword + RSQL filter(status/severity/assignee)
- 생성/수정: 모달 처리
- 상세보기: Detail 버튼으로 페이지 이동
- 상태전이 액션: Start/Resolve/Close/Reopen (사유 입력 모달)
- 요청 브로커 실패 시 자동 생성된 케이스를 같은 화면에서 추적 가능

## 4. 공통 리스트 UX 규칙
- Keyword 입력은 상단 좌측
- Create 버튼(해당 화면에 있는 경우)은 keyword 우측
- Search/Reset 버튼은 Create 우측
- 추가 filter input은 하단 라인에 배치
- Enter 키 입력 시 즉시 Search 실행

## 5. 에러 처리
- 인증 만료/미인증 시 로그인 페이지로 유도
- DB 메뉴 로딩 실패 시 사이드에 경고 메시지 표시
- 주요 화면 제목 옆 tooltip으로 화면 목적 설명 제공
