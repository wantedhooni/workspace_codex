# Frontend 기능 테스트 결과 (엔터프라이즈 고도화 31차)

- 기준: 사용자 관점 FFE (화면 + 버튼 + 그리드 + API + 업무 액션)
- 실행 스크립트:
  - `./scripts/test-frontend-full.sh`
  - `./scripts/test-frontend-e2e.sh`
- 실행 포트: `http://127.0.0.1:5176`
- 결과: PASS

## 주요 고도화

- 로그인/인증 JWT 전환
  - 로그인 API: `/api/auth/login`
  - 모든 API 요청에 `Authorization: Bearer <token>` 전달
- 백엔드 메뉴권한 API(`/api/account/menus`) 연동
- 로그인 사용자 기준 Resource 동적 렌더링
  - 메뉴별 `canRead`로 노출 제어
  - `canCreate`로 Create 액션 노출 제어
- 포트폴리오 UX 고도화
  - 상단 AppBar에 전역 `Active Portfolio` 선택기 추가
  - 기존 숫자형 `portfolioId` 입력 대신 코드/이름 기반 셀렉트 필터로 전환
  - 주요 그리드의 Portfolio 컬럼을 `ID` 대신 `코드 · 이름`으로 표기
  - 포트폴리오 요약 화면 상단에 `포트폴리오 운영 인사이트` 패널 추가
  - 건강점수/건강상태/PnL마진/회전률/오픈주문비율/집중종목을 카드/칩으로 시각화
  - 상위 익스포저(종목별 비중) 칩 제공 + tooltip로 평가금액/PnL 노출
  - 백엔드 `/api/portfolio-summaries/insight` 연동
- 주문 워크벤치(Order Workbench) 추가
  - 주문 리스트 상단에 KPI 카드(총주문/스테일/오픈노출/회전/손익) 제공
  - 상태별 퀵필터 칩 + 상위 심볼 집중도 칩 제공
  - 백엔드 `/api/orders/workbench` 실시간 연동
- 주문 화면 UX 고도화(실사용 필터링 강화)
  - 주문 리스트 고급 필터 추가: `상태/매수매도/주문유형/TIF/수량범위(min~max)`
  - 주문 상태를 컬러 배지(Chip)로 표기해 모니터링 가독성 강화
  - 워크벤치 저장뷰/기본뷰에 고급 필터 값(side/orderType/timeInForce/min/maxQuantity) 연동
- 핵심 리스트 컬럼 중요도 재정렬
  - 주문/리스크/체결/포지션/원장 리스트를 실무 KPI 우선 컬럼으로 재배치
  - ID/부가 컬럼은 후순위 배치, 상세 정보는 모달에서 확인하도록 정리
- 주문 리스트 대량 액션(Bulk Actions) 추가
  - 체크박스 다중 선택 + `선택 취소` / `선택 거부` 버튼 제공
  - 백엔드 `/api/orders/bulk/cancel`, `/api/orders/bulk/reject` 연동
  - 처리 결과(성공/실패 건수) 노티피케이션 제공
- 저장 뷰(Saved View) UX 추가
  - 주문 필터 상태를 저장/적용/삭제하는 엔터프라이즈형 뷰 관리 추가
  - 백엔드 `/api/saved-views` 연동 (권한 기반 CREATE/DELETE 제어)
- 기본 뷰(Default View) UX 추가
  - 주문 화면에서 선택 뷰를 `기본뷰 고정` 가능
  - `기본뷰 적용` 버튼 + 기본뷰 자동적용(초기 진입 시) 지원
  - 백엔드 `/api/saved-views/default` 연동
- 메뉴별 `canDelete` 기반 삭제 버튼 동작 연동
  - 주문/사용자/권한/메뉴/메뉴권한 삭제 액션 추가
- Create UX 모달 전환
  - 기존 별도 Create 화면 대신 리스트 상단 모달 생성 폼 제공
- 리스트 상세보기 추가
  - 각 리스트 행에 `상세` 버튼 추가 및 상세 모달 제공
  - 상세 모달에서 `필드 보기` / `원본 JSON` 탭 동시 제공
- 주문 화면 인사이트 모달 고도화
  - 주문 리스트를 핵심 컬럼 중심으로 정리해 가독성 개선
  - `인사이트` 버튼으로 주문별 요약/체결/감사/리스크 탭을 단일 모달에서 제공
  - 백엔드 `/api/orders/{orderId}/insight` 연동으로 실시간 데이터 기반 상세 분석 제공
- 로컬 데모 다계정 로그인 지원
  - `admin@quant.io / demo1234`
  - `trader@quant.io / trader1234`
  - `risk@quant.io / risk1234`
  - `viewer@quant.io / viewer1234`
  - 로그인 화면 계정 칩 클릭 시 자동 입력 지원
- UI 테마 고도화
  - Jmix 레퍼런스 스타일 기반 다크 사이드바 + 라이트 탑바 구성
  - 섹션형 메뉴(수익 실행/리스크 통제/회계/운영/내 작업) 적용
  - 필터 패널/액션바/그리드 톤 엔터프라이즈 스타일로 정렬
  - Grid 깨짐 방지(필터 최소폭/가로 스크롤/셀 nowrap) 전역 스타일 반영
- 대시보드 목적성/수익 실행 UX 고도화
  - 상단에 "이 플랫폼의 목적" 블록 추가(목표/시장국면/차단이슈/우선순위)
  - `portfolioProfitPlaybook` 연동으로 실행 액션(담당역할/기대효과/기한/이동경로) 제공
  - 사용자가 지표 확인 후 즉시 행동할 수 있는 실행 루틴 카드 추가
- 대시보드 플레이북 피드백 루프 추가
  - `portfolioProfitPlaybookFeedback` 리소스 추가
  - work-queue 액션 실행 후 손익/회전율/오픈주문/경보 변화량을 카드로 가시화
  - 액션 결과 상태(SUCCESS/FAILED)와 평가(POSITIVE/NEUTRAL/NEGATIVE) 즉시 확인 지원
- 대시보드 역할별 고도화
  - 역할(Role) 기반 보조 KPI 카드 분리(Trader/Risk/Viewer)
  - 회전율 기준 운영 경고(정상/WARN/CRITICAL) 배지 및 경고 패널 제공
- 대시보드 사용자 작업 큐(Action Center) 고도화
  - 백엔드 `/api/account/work-queue` 연동으로 사용자별 우선 작업 제공
  - 치명 경보/지연 주문/전표 대기/세션 정리 작업을 우선순위 카드로 제공
  - 권한 기반 액션 가능 여부(`조회 전용`/실행 가능) 표시 제공
- 대시보드 즉시 실행(Quick Action) 고도화
  - 지연주문 정리: `/api/account/work-queue/actions/remediate-stale-orders`
  - 다른 세션 해지: `/api/account/work-queue/actions/revoke-other-sessions`
  - 승인 전표 일괄 전기: `/api/account/work-queue/actions/post-approved-vouchers`
  - 초안 전표 일괄 승인: `/api/account/work-queue/actions/approve-draft-vouchers`
  - 치명경보 긴급대응(거래중지+오픈주문 취소): `/api/account/work-queue/actions/emergency-risk-response`
  - 거래 재개 검증: `/api/account/work-queue/actions/resume-trading`
  - 카드에서 버튼 한 번으로 실행 후 KPI/작업큐 자동 리프레시
  - 거래 재개 시 치명 경보가 남아있으면 재개 차단 안내(가드 로직) 제공
  - 거래 재개 차단 시 상세 사유 모달 + 강제 재개 버튼 제공(운영자 의사결정 UX 강화)
- 대시보드 활동 피드(Activity Feed) 고도화
  - 백엔드 `/api/account/activity-feed` 연동
  - 최근 주문/리스크/전표 이벤트를 시간순 카드로 제공
  - 각 피드 항목에서 관련 화면으로 즉시 이동 가능
- 생성 모달 도메인 검증 강화
  - 주문: 심볼 포맷/수량/지정가(LIMIT) 교차검증
  - 체결: orderId/체결수량/체결가 양수 검증
  - 리스크한도: 한도 간 상호관계 검증(주문/종목/일회전)
  - 전표: 차변/대변 합계 일치 검증, 라인 필수값 검증
- 상세 모달 엔터프라이즈화
  - 요약 KPI 카드 + 필드 뷰 + 원본 JSON 탭으로 확장
  - 주문/체결/포지션/리스크한도/포트폴리오요약/전표 상세 정보 강화
- 주문 감사로그 고도화
  - `actor(행위자)` 컬럼/필터 추가
  - 백엔드 `actor` 필터 API와 연동
  - 감사로그 요약 패널 추가(`총 이벤트/최근 이벤트/주문 수/액션 카운트/상태전이/행위자 TOP`)
  - 액션 칩 기반 퀵필터로 `action` 조건 즉시 전환
- 리스크 한도 화면 거래통제 고도화
  - 리스트에서 `거래중지/거래재개` 액션 버튼 추가
  - 백엔드 `/api/risk-limits/trading-controls` 연동
  - 리스트에서 `이력` 버튼으로 거래통제 이력 모달 제공
  - 백엔드 `/api/risk-limits/trading-controls/history` 연동
- 리스크 경보 화면 추가
  - `/api/risk-alerts` 연동
  - 상단 `리스크 운영 오버뷰` 패널 추가(`/api/risk-alerts/overview` 연동)
  - SLA 위반/미확인/Critical 원클릭 필터 제공
  - 심각도(CRITICAL/WARN/INFO) 기준 경보 조회/필터/상세 제공
  - SLA(`경과/목표/위반`) 및 우선순위(`priorityScore`) 컬럼 제공
  - 경보 `확인/확인해제(ACK/UNACK)` 액션 추가
  - `/api/risk-alerts/ack`, `/api/risk-alerts/unack` 연동
  - 경보 워크플로(`/api/risk-alerts/workflow`) 전이 액션 연동
  - 확인자/확인시각/확인상태(ACK/UNACK) 컬럼 제공
- 체결 품질 화면 추가
  - `/api/execution-qualities` 연동
  - 종목별 체결률/슬리피지/수수료 기반 등급(A~D) 및 진단 메시지 제공
- 주문 건전성 화면 추가
  - `/api/order-health` 연동
  - 오픈주문/지연주문/오더에이징 기반 상태(HEALTHY/WARN/CRITICAL) 제공
  - 리스트에서 `지연주문 일괄취소` 액션 버튼 제공
  - 백엔드 `/api/order-health/remediate-stale` 연동

## 화면별 결과

| 화면(Route) | API | Grid | Export | Create 버튼 | 결과 |
|---|---|---|---|---|---|
| `/#/orders` | PASS | PASS | PASS | PASS | PASS |
| `/#/orderAudits` | PASS | PASS | PASS | N/A | PASS |
| `/#/trades` | PASS | PASS | PASS | PASS | PASS |
| `/#/positions` | PASS | PASS | PASS | N/A | PASS |
| `/#/portfolioSummaries` | PASS | PASS | PASS | N/A | PASS |
| `/#/orderHealth` | PASS | PASS | PASS | N/A | PASS |
| `/#/riskAlerts` | PASS | PASS | PASS | N/A | PASS |
| `/#/executionQualities` | PASS | PASS | PASS | N/A | PASS |
| `/#/riskLimits` | PASS | PASS | PASS | PASS | PASS |
| `/#/users` | PASS | PASS | PASS | PASS | PASS |
| `/#/roles` | PASS | PASS | PASS | PASS | PASS |
| `/#/menus` | PASS | PASS | PASS | PASS | PASS |
| `/#/menuPermissions` | PASS | PASS | PASS | PASS | PASS |
| `/#/journalVouchers` | PASS | PASS | PASS | PASS | PASS |
| `/#/ledgerEntries` | PASS | PASS | PASS | N/A | PASS |
| `/#/accountProfile` | PASS | PASS | N/A | N/A | PASS |
| `/#/accountSessions` | PASS | PASS | PASS | N/A | PASS |

## 업무 액션 검증

- 전표: 승인/전기 버튼 동작 + 원장 반영 PASS
- 사용자: 잠금/비밀번호 초기화/삭제 버튼 동작 PASS
- 주문: 삭제 버튼 동작 PASS
- 주문: 취소/거부/삭제 버튼 동작 PASS
  - 취소/거부 후 `상태/결정사유/결정시각` 컬럼 반영 PASS
- 주문: 인사이트 모달(요약/체결/감사/리스크 탭) 열람 + `/api/orders/{orderId}/insight` 연동 PASS
- 주문: 고급 필터(side/orderType/timeInForce/수량범위) 연동 + 필터 결과 그리드 표시 PASS
- 주문감사: 주문 상태변경 이력(생성/체결/취소/거부) 조회 PASS
- 주문감사: 행위자(actor) 컬럼 노출/필터 동작 PASS
- 리스크한도: 거래중지/재개 버튼 동작 + API 연동 PASS
- 리스크한도: 거래통제 이력 모달 열람 + `/api/risk-limits/trading-controls/history` 연동 PASS
- 리스크경보: 경보 데이터 조회/그리드/상세 동작 PASS
- 리스크경보: 운영 오버뷰 패널 렌더링 + `/api/risk-alerts/overview` 연동 PASS
- 리스크경보: ACK/UNACK 버튼 동작 + `/api/risk-alerts/ack`, `/api/risk-alerts/unack` 연동 PASS
- 리스크경보: 워크플로 전이(진행중/해결/재오픈) 버튼 동작 + `/api/risk-alerts/workflow` 연동 PASS
- 주문감사: 감사 요약 패널 렌더링 + `/api/orders/audit-logs/summary` 연동 PASS
- 포트폴리오요약: 운영 인사이트 패널 렌더링 + `/api/portfolio-summaries/insight` 연동 PASS
- 체결품질: 체결품질 데이터 조회/그리드/상세 동작 PASS
- 주문건전성: 건전성 데이터 조회/그리드/상세 동작 PASS
- 주문건전성: 지연주문 일괄취소 액션 + API 연동 PASS
- 주문: 저장 뷰 생성/적용/삭제 액션 + API 연동 PASS
- 주문: 대량 선택 취소/거부 액션 + API 연동 PASS
- 주문: 기본뷰 고정/조회/적용 액션 + API 연동 PASS
- 세션: 세션해지 버튼 동작 PASS
- 생성: 모달 생성 버튼 동작 PASS
- 상세: 리스트 상세 모달 열람 동작 PASS
- 폼검증: 생성 모달 도메인 검증 메시지/차단 동작 PASS
- 대시보드: 역할/위험도 기반 카드/경고 표시 PASS
- 대시보드: 사용자 작업 큐 카드 렌더링 + `/api/account/work-queue` 연동 PASS
- 대시보드: 즉시 실행 버튼 클릭 시 work-queue action API 연동 PASS
- 대시보드: 승인 전표 일괄 전기 즉시 실행 + `/api/account/work-queue/actions/post-approved-vouchers` 연동 PASS
- 대시보드: 초안 전표 일괄 승인 즉시 실행 + `/api/account/work-queue/actions/approve-draft-vouchers` 연동 PASS
- 대시보드: 치명경보 긴급대응 실행 후 오픈주문 자동 취소 확인 PASS
- 대시보드: 거래 재개 검증 즉시 실행 + `/api/account/work-queue/actions/resume-trading` 연동 PASS
- 대시보드: 활동 피드 카드 렌더링 + `/api/account/activity-feed` 연동 PASS
- 대시보드: 목적/수익 실행 루틴 카드 렌더링 + `/api/portfolio-summaries/profit-playbook` 연동 PASS
- 대시보드: 플레이북 피드백 루프 카드 렌더링 + `/api/portfolio-summaries/profit-playbook/feedback` 연동 PASS

## 검증 아티팩트

- 종합 결과 JSON:
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/frontend-full-e2e-result.json`
- 대표 스크린샷:
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/frontend-users.png`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/frontend-dashboard-work-queue.png`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/frontend-orders-advanced-filter.png`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/frontend-orders-insight-modal.png`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/frontend-order-audits-full.png`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/frontend-order-health-full.png`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/frontend-risk-alerts-full.png`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/frontend-execution-qualities-full.png`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/frontend-risk-limits-history-modal.png`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/frontend-menu-permissions.png`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/frontend-account-profile.png`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/frontend-account-sessions.png`

## 결론

- 프론트가 JWT 인증 + 백엔드 권한 API와 연동되어 사용자별 메뉴/액션 제어가 가능해짐
- 기존 주문/체결/포지션/전표/원장 + 관리 기능 + 삭제 액션까지 정상 동작
