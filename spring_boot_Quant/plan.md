# 미국 증시 퀀트 투자 프로그램 개발 계획

## 단계별 TODO 트리

- [ ] 0. 프로젝트 착수
  - [ ] 0.1 MVP 범위 확정
    - [ ] 투자 대상: 미국 주식 + ETF
    - [ ] 운용 주기: 일봉 기준 장 마감 리밸런싱(EOD)
    - [ ] 초기 모드: 모의투자(Paper Trading) 우선
  - [ ] 0.2 성공 지표 정의
    - [ ] CAGR / MDD / 샤프지수 / 회전율(Turnover)
    - [ ] 데이터 최신성 SLA
    - [ ] 리밸런싱 작업 성공률
  - [ ] 0.3 저장소 운영 규칙 정리
    - [ ] 브랜치 전략(`main` + 기능 브랜치)
    - [ ] 커밋/PR 템플릿
    - [ ] 환경 프로필 정책(`local/dev/prod`)

- [ ] 1. 아키텍처 및 기반 구성
  - [ ] 1.1 백엔드 초기 구성(Spring Boot)
    - [ ] 모듈 구성: `api`, `domain`, `batch`, `infra`
    - [ ] Gradle/Maven 빌드 및 프로필 설정
    - [ ] OpenAPI/Swagger 적용
  - [ ] 1.2 보안 기본 구성(JWT)
    - [ ] JWT 발급/재발급(Refresh) 흐름
    - [ ] 권한 역할: `ADMIN`, `QUANT`, `VIEWER`
    - [ ] 인증/인가 감사 로그
    - [ ] 사용자/권한/메뉴 권한 체크를 위한 공통 인가 미들웨어
  - [ ] 1.3 프론트엔드 초기 구성(React-Admin)
    - [ ] JWT 기반 로그인/Auth Provider
    - [ ] 백엔드 API 연동 Data Provider
    - [ ] 역할 기반 메뉴 노출 제어
    - [ ] 메뉴 권한 기반 라우팅 가드
  - [ ] 1.4 인프라 기본 구성
    - [ ] PostgreSQL + 마이그레이션 도구(Flyway/Liquibase)
    - [ ] Redis(선택: 캐시/이벤트)
    - [ ] 로컬 실행용 Docker Compose

- [ ] 2. 도메인 및 DB 모델링
  - [ ] 2.1 핵심 엔티티 정의
    - [ ] `Symbol`, `PriceBar`, `CorporateAction`
    - [ ] `Strategy`, `Signal`, `FactorSnapshot`
    - [ ] `Portfolio`, `Position`, `Order`, `Trade`
    - [ ] `Ledger`, `JournalVoucher`, `JournalEntry`
    - [ ] `BacktestRun`, `RebalanceJob`
    - [ ] `User`, `Role`, `Menu`, `MenuPermission`, `UserRole`
    - [ ] `UserSession`, `PasswordHistory`, `LoginAudit`
  - [ ] 2.2 스키마/인덱스 설계
    - [ ] 시계열 인덱스(`symbol`, `date`)
    - [ ] 스냅샷 버전 유니크 제약
    - [ ] 대용량 가격 테이블 파티셔닝 전략
  - [ ] 2.3 QueryDSL 저장소 계층
    - [ ] 팩터 랭킹 조회 쿼리
    - [ ] 포트폴리오 노출도 조회 쿼리
    - [ ] 성과 집계 쿼리

- [ ] 3. 시장 데이터 파이프라인
  - [ ] 3.1 데이터 소스 연동
    - [ ] `yahoo-finance2` 어댑터 API 연동
    - [ ] OHLCV 수집기 구현
    - [ ] 재시도/요청 제한(Rate Limit) 처리
  - [ ] 3.2 데이터 품질 검증
    - [ ] 결측 거래일 탐지
    - [ ] 이상치 및 액면분할/배당 보정 검증
    - [ ] 멱등(중복 안전) 업서트 처리
  - [ ] 3.3 배치 오케스트레이션
    - [ ] 일일 수집 스케줄러
    - [ ] 기간 지정 백필(Backfill) 작업
    - [ ] 작업 모니터링/알림

- [ ] 4. 전략 엔진
  - [ ] 4.1 전략 표준 인터페이스
    - [ ] `universe -> score -> select -> weight -> rebalance`
    - [ ] 전략 버전 및 파라미터 스키마
  - [ ] 4.2 MVP 기본 전략
    - [ ] 모멘텀(12-1)
    - [ ] 단순 밸류/퀄리티 혼합 팩터
  - [ ] 4.3 리스크 제약
    - [ ] 종목당 최대 비중
    - [ ] 섹터 익스포저 상한
    - [ ] 최소 현금 비중 및 회전율 가드레일

- [ ] 5. 백테스트 엔진
  - [ ] 5.1 체결 모델
    - [ ] EOD 신호, 익일 시가/종가 체결 가정
    - [ ] 수수료 + 슬리피지 반영
    - [ ] 상장폐지 종목 처리
  - [ ] 5.2 편향 방지
    - [ ] 룩어헤드(미래 데이터 사용) 금지
    - [ ] 생존편향 제거된 유니버스 사용
    - [ ] 시점 기준(Point-in-time) 데이터 강제
  - [ ] 5.3 결과 리포트
    - [ ] 자산곡선(Equity Curve) + 낙폭(Drawdown)
    - [ ] 월간/연간 수익률 표
    - [ ] 거래 기여도(Attribution) 요약

- [ ] 6. 주문/체결/포지션/원장/전표 실행
  - [ ] 6.1 리밸런싱 결과 -> 주문 변환
    - [ ] 목표/현재 포지션 차이 계산
    - [ ] 수량 단위/금액 반올림 처리
    - [ ] 비정상 주문 사전 차단
  - [ ] 6.2 주문 생명주기 관리
    - [ ] 상태: `NEW -> SENT -> PARTIAL -> FILLED/CANCELED/REJECTED`
    - [ ] 상태 전이 이벤트 로그
    - [ ] 실패 재시도 정책
  - [ ] 6.3 브로커 어댑터 추상화
    - [ ] 실거래 브로커 확장용 인터페이스
    - [ ] 모의체결(Mock) 어댑터 구현
  - [ ] 6.4 Trade(체결) 정합성 관리
    - [ ] 체결 이벤트 수신/저장(중복 제거 키 설계)
    - [ ] 주문-체결 매핑 및 부분체결 누적
    - [ ] 체결 정정/취소(Cancel/Correct) 처리
  - [ ] 6.5 Position(포지션) 스냅샷 관리
    - [ ] 체결 기반 포지션 증감 반영
    - [ ] 평균단가/평가손익/실현손익 계산
    - [ ] 일자별 포지션 스냅샷 및 검증 배치
  - [ ] 6.6 원장(Ledger) 관리
    - [ ] 현금/주식/수수료/세금 계정 과목 정의
    - [ ] 거래 이벤트 -> 원장 분개 규칙 엔진
    - [ ] 원장 잔액 검증(차대합/계정 잔액) 배치
  - [ ] 6.7 전표(Journal Voucher) 관리
    - [ ] 전표 생성(자동/수동), 승인, 취소 워크플로
    - [ ] 전표 상태: `DRAFT -> APPROVED -> POSTED -> CANCELED`
    - [ ] 전표-원장 전기(posting) 및 감사 이력 보관

- [ ] 7. 관리자 UI(React-Admin)
  - [ ] 7.1 대시보드
    - [ ] NAV / 손익(PnL) / MDD 위젯
    - [ ] 최신 리밸런싱 상태
    - [ ] 데이터 최신성 표시
  - [ ] 7.2 전략 관리
    - [ ] 파라미터 수정 화면
    - [ ] 버전 활성/비활성 관리
    - [ ] 백테스트 즉시 실행 액션
  - [ ] 7.3 주문/포지션 모니터링
    - [ ] 미체결/진행 주문 목록 + 상태 필터
    - [ ] 현재 포지션 및 비중
    - [ ] 체결/예외 이력
  - [ ] 7.3.1 Trade 관리 화면
    - [ ] 체결 목록/상세/정정 이력
    - [ ] 주문별 체결 매칭 현황
  - [ ] 7.3.2 원장/전표 관리 화면
    - [ ] 원장 계정별 잔액/거래내역 조회
    - [ ] 전표 생성/승인/취소 및 상태 필터
    - [ ] 전표-원장 전기 결과 및 오류 모니터링
  - [ ] 7.4 사용자 관리
    - [ ] 사용자 목록/상세/등록/수정/비활성화
    - [ ] 계정 상태(잠금/활성) 및 마지막 로그인 확인
    - [ ] 비밀번호 초기화/강제 변경 정책 적용
  - [ ] 7.5 권한 관리
    - [ ] 권한(Role) 목록/등록/수정/삭제
    - [ ] 권한별 API 접근 범위 관리
    - [ ] 사용자-권한 매핑 관리
  - [ ] 7.6 메뉴 관리
    - [ ] 메뉴 트리(상위/하위) 구성 관리
    - [ ] 메뉴 노출 순서/경로/Icon 관리
    - [ ] 메뉴 활성/비활성 및 배포 반영
  - [ ] 7.7 메뉴권한 관리
    - [ ] 메뉴별 조회/등록/수정/삭제 권한 매핑
    - [ ] 권한(Role)별 메뉴 접근 제어
    - [ ] 변경 이력(누가/언제/무엇) 감사 로그
  - [ ] 7.8 사용자 Account 기능
    - [ ] 내 계정 프로필 조회/수정(이메일, 이름, 알림 설정)
    - [ ] 비밀번호 변경 및 최근 비밀번호 재사용 방지
    - [ ] 내 접속 이력/활성 세션 조회 및 강제 로그아웃
    - [ ] OTP(2차 인증) 등록/해제(선택)

- [ ] 8. 테스트 및 품질 게이트
  - [ ] 8.1 백엔드 테스트
    - [ ] 단위 테스트(도메인/전략 로직)
    - [ ] 통합 테스트(API + DB)
    - [ ] 배치 테스트(수집/리밸런싱)
  - [ ] 8.2 프론트엔드 테스트
    - [ ] 핵심 화면 컴포넌트 테스트
    - [ ] API 계약 목(Mock) 테스트
  - [ ] 8.3 회귀 방지 장치
    - [ ] 기준 백테스트 결과 스냅샷
    - [ ] 핵심 쿼리 성능 예산 점검

- [ ] 9. 관측성 및 운영
  - [ ] 9.1 로깅/추적
    - [ ] API/배치 공통 상관관계 ID
    - [ ] 구조화 JSON 로그
  - [ ] 9.2 지표/알림
    - [ ] 배치 성공/실패 지표
    - [ ] 주문 실패율 알림
    - [ ] API 지연/오류율 알림
  - [ ] 9.3 배포 운영
    - [ ] dev/stage/prod 배포 파이프라인
    - [ ] 롤백 런북
    - [ ] 장애 대응 체크리스트

- [ ] 10. 마일스톤 일정(총 10~12주)
  - [ ] 10.1 1~2주차: 기반 구성 + 인증 + 기본 스키마
  - [ ] 10.2 3~4주차: 데이터 파이프라인 + 품질 검증
  - [ ] 10.3 5~7주차: 전략 + 백테스트 엔진
  - [ ] 10.4 8~9주차: 주문/체결/포지션 엔진 + 모의투자
  - [ ] 10.5 10~11주차: 원장/전표 + 회계 정합성 검증
  - [ ] 10.6 12주차: React-Admin + 안정화 + 출시 준비

## 바로 실행할 액션

- [x] A1 도메인 재확정: `Order/Trade/Position/Ledger/JournalVoucher` 용어/상태값 표준화 (초안: `docs/01-domain-standard.md`)
- [x] A2 ERD/Flyway 초안 작성(원장/전표 포함) + 샘플 데이터 (초안: `db/migration/V1__init_quant_core.sql`)
- [x] A3 `yahoo-finance2` 어댑터 API 스펙 확정 및 수집 배치 연결 (초안: `docs/03-market-data-adapter-spec.md`, 스캐폴딩: `adapter/market-data-adapter`)
- [x] A4 주문->체결->포지션 계산 파이프라인 MVP 구현 (모듈: `backend-mvp`)
- [x] A5 전표 승인/전기(posting) 흐름 및 원장 검증 배치 구현 (모듈: `backend-mvp`)
- [x] A6 React-Admin 화면(주문/체결/포지션/원장/전표) 1차 구현 (모듈: `frontend-admin`)
- [x] A7 통합 테스트 + 운영 모니터링(지표/알림) 마무리 (`backend-mvp` 테스트 + `scripts/smoke_a7.sh`)

## 작업 순서(재정리)

1. 도메인/회계 규칙 확정
  - `Order/Trade/Position/Ledger/JournalVoucher` 정의
  - 상태 전이 및 정합성 룰 문서화
2. 스키마/마이그레이션 확정
  - 핵심 테이블 + 인덱스 + 제약조건
  - 원장 차대합 검증용 뷰/쿼리 설계
3. 시장데이터 수집 확정
  - `yahoo-finance2` 어댑터 + Spring Batch 연동
  - 일봉 적재/백필/검증 자동화
4. 실행 엔진 구현
  - 리밸런싱 -> 주문 생성
  - 주문 -> 체결 반영 -> 포지션 스냅샷
5. 회계(원장/전표) 구현
  - 거래 이벤트 기반 전표 생성/승인/전기
  - 원장 잔액/차대합 검증 배치
6. 관리자 기능 구현
  - 사용자/권한/메뉴/메뉴권한 + Account
  - 주문/체결/포지션/원장/전표 UI
7. 품질/운영 전환
  - 통합/회귀 테스트
  - 지표/알림/배포 파이프라인 점검

## 개발 작업 패턴(백엔드 API)

- Controller 요청/응답 DTO는 `class` 대신 `record` 사용
- 하나의 기능은 `{기능명}Payload` 내부에 `Req`/`Res` record로 쌍으로 관리
- 네이밍 규칙
  - Payload: `{기능명}Payload`
  - 요청: `{기능명}Payload.Req`
  - 응답: `{기능명}Payload.Res`
- 패키지/구조 규칙
  - `controller`는 HTTP 입출력(`Req`/`Res`)만 담당
  - 비즈니스 로직은 `service`로 위임
  - 엔티티(JPA)와 API record는 분리
- 검증/응답 규칙
  - `Req` 필드는 Bean Validation(`@NotNull`, `@NotBlank` 등) 적용
  - 공통 응답 포맷 필요 시 `ApiResponse<Res>`로 감싸서 반환
  - 목록 조회는 `PageRes<T>` 또는 페이징 메타정보를 포함한 `Res` 사용
- 검색 규칙(QueryDSL)
  - 검색 API는 `SearchPayload.Req`에 필드별 검색 조건을 명시적으로 선언
  - 문자열 검색은 `containsIgnoreCase`, 정확 검색은 `eq`, 기간 검색은 `between/goe/loe` 사용
  - 조건값이 `null`/빈값이면 해당 조건은 제외(동적 where)
  - 복합 검색은 `BooleanBuilder` 또는 `where(vararg Predicate)`로 조합
  - 정렬/페이징은 `Pageable` 기반으로 통일하고, 결과는 `PageRes<...>`로 반환

## 사용할 기술 스택(안)

- 백엔드
  - Java 21
  - Spring Boot 3.x
  - Spring Security + JWT(Access/Refresh)
  - Spring Data JPA + QueryDSL
  - Bean Validation + Global Exception Handler
  - OpenAPI(Swagger) + Actuator
- 배치/비동기
  - Spring Batch(일봉 수집, 백필, 리밸런싱)
  - Scheduler(`@Scheduled`) 또는 Quartz(복잡 스케줄 필요 시)
  - Redis(캐시/분산락 선택)
  - Node.js `market-data-adapter`(`yahoo-finance2` 연동)
- 데이터/인프라
  - PostgreSQL 15+
  - Flyway(마이그레이션)
  - Testcontainers(통합 테스트)
  - Docker Compose(로컬 통합 실행)
- 프론트엔드
  - React-Admin + TypeScript
  - MUI(Material UI)
  - React Query(서버 상태 관리)
  - ECharts 또는 Recharts(성과 차트)
- 품질/운영
  - JUnit5 + Mockito + AssertJ
  - ESLint + Prettier
  - Micrometer + Prometheus + Grafana
  - GitHub Actions(CI)

## 외부 API 선택(안)

- 시장데이터(확정): `yahoo-finance2`
  - 저장소: `https://github.com/gadicc/yahoo-finance2`
  - 용도: 미국 주식/ETF 시세 조회(quote, chart/historical 등)
  - 주의: 공식 Yahoo API가 아닌 커뮤니티 라이브러리(비공식)
- 연동 방식(권장)
  - Spring Boot가 직접 호출하지 않고, Node 기반 `market-data-adapter`(Worker/API)에서 `yahoo-finance2` 호출
  - Spring Batch는 어댑터 API를 호출해 수집 후 PostgreSQL에 적재
  - 어댑터 장애 대비 캐시/재시도/백오프 정책 적용
- 주문/모의투자 API
  - Paper Trading은 별도 브로커 API(예: Alpaca) 연동 가능하도록 `BrokerAdapter` 분리
- 운영 원칙
  - 초기 MVP는 EOD(일봉) 중심으로 시작
  - 실시간 기능은 대시보드/주문 모니터링 중심으로 점진 확장
  - 벤더 변경 가능하도록 `MarketDataProvider`, `BrokerAdapter` 인터페이스 고정

## 백엔드 API 범위(초안)

- 인증/인가
  - `POST /api/auth/login`
  - `POST /api/auth/refresh`
  - `POST /api/auth/logout`
  - `GET /api/auth/me`
- 사용자 Account
  - `GET /api/account/profile`
  - `PUT /api/account/profile`
  - `PUT /api/account/password`
  - `GET /api/account/sessions`
  - `DELETE /api/account/sessions/{sessionId}`
  - `GET /api/account/login-audits`
  - `POST /api/account/otp/enroll`
  - `DELETE /api/account/otp`
- 사용자/권한/메뉴
  - `GET|POST|PUT /api/admin/users`
  - `GET|POST|PUT|DELETE /api/admin/roles`
  - `GET|POST|PUT|DELETE /api/admin/menus`
  - `GET|POST|PUT|DELETE /api/admin/menu-permissions`
  - `PUT /api/admin/users/{id}/roles`
- 시장데이터/배치
  - `POST /api/jobs/market-data/daily`
  - `POST /api/jobs/market-data/backfill`
  - `GET /api/market/symbols`
  - `GET /api/market/price-bars`
- 전략/백테스트
  - `GET|POST|PUT /api/strategies`
  - `POST /api/strategies/{id}/run-backtest`
  - `GET /api/backtests`
  - `GET /api/backtests/{id}/report`
- 포트폴리오/주문
  - `GET /api/portfolios/{id}`
  - `POST /api/rebalances/{portfolioId}/preview`
  - `POST /api/rebalances/{portfolioId}/execute`
  - `GET /api/orders`
  - `GET /api/trades`
  - `GET /api/positions`
  - `GET /api/positions/{id}`
  - `GET /api/ledgers/accounts`
  - `GET /api/ledgers/entries`
  - `GET|POST|PUT /api/journal-vouchers`
  - `POST /api/journal-vouchers/{id}/approve`
  - `POST /api/journal-vouchers/{id}/post`
  - `POST /api/journal-vouchers/{id}/cancel`
- 대시보드
  - `GET /api/dashboard/summary`
  - `GET /api/dashboard/performance`
  - `GET /api/dashboard/jobs`
