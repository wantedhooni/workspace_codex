# Backend API 테스트 결과 (엔터프라이즈 고도화 31차)

- 실행 일시: 2026-02-10 (Asia/Seoul)
- 기준: SpringDoc OpenAPI v3 (`/v3/api-docs`) + 서비스 단위 테스트
- 실행 스크립트:
  - `./scripts/test-backend-openapi.sh`
  - `cd /Users/revy/workspace_codex/spring_boot_Quant/backend-mvp && gradle test`

## OpenAPI 경로 전수 테스트 결과

| Method | Path | 결과 |
|---|---|---|
| POST | /api/auth/login | PASS |
| GET | /api/orders | PASS |
| POST | /api/orders | PASS |
| GET | /api/orders/{orderId}/insight | PASS |
| GET | /api/orders/workbench | PASS |
| POST | /api/orders/bulk/cancel | PASS |
| POST | /api/orders/bulk/reject | PASS |
| DELETE | /api/orders/{orderId} | PASS |
| POST | /api/orders/{orderId}/cancel | PASS |
| POST | /api/orders/{orderId}/reject | PASS |
| GET | /api/orders/audit-logs | PASS |
| GET | /api/orders/audit-logs/summary | PASS |
| GET | /api/trades | PASS |
| POST | /api/trades/events | PASS |
| GET | /api/positions | PASS |
| GET | /api/risk-limits | PASS |
| PUT | /api/risk-limits | PASS |
| GET | /api/risk-limits/trading-controls | PASS |
| PUT | /api/risk-limits/trading-controls | PASS |
| GET | /api/risk-limits/trading-controls/history | PASS |
| GET | /api/risk-alerts | PASS |
| GET | /api/risk-alerts/overview | PASS |
| POST | /api/risk-alerts/ack | PASS |
| POST | /api/risk-alerts/unack | PASS |
| POST | /api/risk-alerts/workflow | PASS |
| GET | /api/portfolios | PASS |
| GET | /api/order-health | PASS |
| POST | /api/order-health/remediate-stale | PASS |
| GET | /api/execution-qualities | PASS |
| GET | /api/portfolio-summaries | PASS |
| GET | /api/portfolio-summaries/insight | PASS |
| GET | /api/portfolio-summaries/profit-playbook | PASS |
| GET | /api/portfolio-summaries/profit-playbook/feedback | PASS |
| GET | /api/users | PASS |
| POST | /api/users | PASS |
| PUT | /api/users/{userId}/status | PASS |
| PUT | /api/users/{userId}/roles | PASS |
| POST | /api/users/{userId}/reset-password | PASS |
| DELETE | /api/users/{userId} | PASS |
| GET | /api/roles | PASS |
| POST | /api/roles | PASS |
| DELETE | /api/roles/{roleId} | PASS |
| GET | /api/menus | PASS |
| POST | /api/menus | PASS |
| DELETE | /api/menus/{menuId} | PASS |
| GET | /api/saved-views | PASS |
| POST | /api/saved-views | PASS |
| DELETE | /api/saved-views/{viewId} | PASS |
| GET | /api/saved-views/default | PASS |
| POST | /api/saved-views/default | PASS |
| GET | /api/menu-permissions | PASS |
| PUT | /api/menu-permissions | PASS |
| DELETE | /api/menu-permissions/{menuPermissionId} | PASS |
| GET | /api/account/me | PASS |
| GET | /api/account/menus | PASS |
| GET | /api/account/work-queue | PASS |
| GET | /api/account/activity-feed | PASS |
| POST | /api/account/work-queue/actions/remediate-stale-orders | PASS |
| POST | /api/account/work-queue/actions/revoke-other-sessions | PASS |
| POST | /api/account/work-queue/actions/post-approved-vouchers | PASS |
| POST | /api/account/work-queue/actions/approve-draft-vouchers | PASS |
| POST | /api/account/work-queue/actions/pause-trading | PASS |
| POST | /api/account/work-queue/actions/emergency-risk-response | PASS |
| POST | /api/account/work-queue/actions/resume-trading | PASS |
| POST | /api/account/change-password | PASS |
| GET | /api/account/sessions | PASS |
| POST | /api/account/sessions/{sessionId}/revoke | PASS |
| GET | /api/journal-vouchers | PASS |
| POST | /api/journal-vouchers | PASS |
| POST | /api/journal-vouchers/{voucherId}/approve | PASS |
| POST | /api/journal-vouchers/{voucherId}/post | PASS |
| POST | /api/journal-vouchers/{voucherId}/cancel | PASS |
| GET | /api/ledgers/entries | PASS |
| POST | /api/ledgers/validate | PASS |
| GET | /api/ledgers/validate/last | PASS |

## 핵심 고도화 반영

- 사용자/권한/메뉴/메뉴권한 + Account 기능 추가 완료
- JWT 인증 도입
  - 로그인: `/api/auth/login`
  - 인증 방식: `Authorization: Bearer <accessToken>`
- 사용자별 동적 메뉴 권한 API 추가
  - `/api/account/menus`
  - JWT subject(email) 기반 사용자 컨텍스트 지원
- 사용자 작업 큐(User Work Queue) API 추가
  - `/api/account/work-queue`
  - 포트폴리오 기준 작업 우선순위(치명 경보/지연주문/전표 대기/세션) 요약 제공
  - 사용자 권한에 따라 액션 가능 여부(`actionEnabled`) 구분 제공
- 사용자 활동 피드(User Activity Feed) API 추가
  - `/api/account/activity-feed`
  - 주문/리스크/전표 이벤트를 단일 타임라인으로 통합 제공
  - 사용자 메뉴 권한에 따라 노출 이벤트를 자동 제한
- 사용자 작업 큐 액션 API 추가
  - `/api/account/work-queue/actions/remediate-stale-orders`
  - `/api/account/work-queue/actions/revoke-other-sessions`
  - `/api/account/work-queue/actions/post-approved-vouchers`
  - `/api/account/work-queue/actions/approve-draft-vouchers`
  - `/api/account/work-queue/actions/pause-trading`
  - `/api/account/work-queue/actions/emergency-risk-response`
  - `/api/account/work-queue/actions/resume-trading`
  - 대시보드에서 지연주문 정리/다른 세션 해지/승인 전표 전기/초안 전표 승인/치명경보 긴급대응(거래중지+오픈주문 취소)/거래재개 검증 실행 지원
  - `resume-trading`은 치명 경보가 남아있으면 재개를 차단하고(`resumed=false`), `force=true`일 때만 강제 재개
  - `resume-trading` 응답에 `blockedMessages` 추가(코드+메시지), 운영자가 차단 사유를 즉시 확인 가능
- 서버측 API 권한 차단(403) 적용
  - 컨트롤러 전체에 메뉴키 + CRUD 액션 권한검사 적용
  - 프론트 메뉴 숨김과 별개로 백엔드 API 자체 차단
- 서버측 DELETE API 추가
  - `/api/orders/{orderId}`
  - `/api/users/{userId}`
  - `/api/roles/{roleId}`
  - `/api/menus/{menuId}`
  - `/api/menu-permissions/{menuPermissionId}`
- 주문 라이프사이클 API 추가
  - `/api/orders/{orderId}/cancel`
  - `/api/orders/{orderId}/reject`
  - `/api/orders/audit-logs`
  - `/api/orders/audit-logs/summary`
  - 상태전이 규칙(부분체결 주문 reject 금지 등) 반영
  - 주문 상태결정 메타(결정사유, 결정시각) 응답 노출 반영
  - 감사로그 행위자(`actor`) 저장/조회 + `actor` 필터 반영
  - 감사로그 운영 요약(최근 이벤트/액션 카운트/상태전이 카운트/행위자 TOP) 집계 추가
- 주문 인사이트(Order Insight) API 추가
  - `/api/orders/{orderId}/insight`
  - 주문별 집행요약(체결률/집행금액/평균체결가/수수료/슬리피지/순현금흐름) 제공
  - 주문별 체결/감사/리스크 시그널(건전성 포함) 통합 응답 제공
- 주문 조회 고급 필터(필드별 다중조건) 고도화
  - `/api/orders` 조회 파라미터 확장
  - `side/orderType/timeInForce`
  - `minQuantity/maxQuantity`
  - `minRemainingQuantity/maxRemainingQuantity`
  - `createdFrom/createdTo(ISO-8601)`
  - 범위 입력 역전(min > max) 및 잘못된 시간 범위(from > to) 서버 검증 적용
- 거래 긴급중지(Kill Switch) 추가
  - `/api/risk-limits/trading-controls` 조회/변경 API
  - `/api/risk-limits/trading-controls/history` 이력 조회 API
  - 거래중지/재개 이벤트를 `action(ENABLE/DISABLE), previousTradingEnabled, reason, updatedAt, updatedBy` 기준으로 감사 이력 저장
  - 거래 중지 시 주문/체결 차단 검증 반영
- 리스크 경보(Risk Alerts) 추가
  - `/api/risk-alerts` 조회 API
  - `/api/risk-alerts/overview` 운영 요약 API
  - `/api/risk-alerts/ack`, `/api/risk-alerts/unack` 운영 확인(ACK) API
  - `/api/risk-alerts/workflow` 워크플로 전이 API(OPEN/IN_PROGRESS/RESOLVED)
  - 회전율/오픈주문 사용률/손실률/Kill Switch 상태 기반 경보 계산
  - 경보 ACK 시 `acknowledged/acknowledgedBy/acknowledgementNote/acknowledgedAt` 상태 저장
  - 경보 워크플로 상태(`workflowStatus/assignee/resolvedBy/resolvedAt`) 저장
  - SLA 지표(`ageMinutes/slaTargetMinutes/slaBreached`) 및 우선순위(`priorityScore`) 계산
  - 오버뷰에서 `critical/warn/info`, `미확인`, `워크플로 상태`, `SLA 위반`, `평균 ACK/해결 시간` 집계
  - INFO 경보 ACK 제한(운영 경보인 WARN/CRITICAL만 ACK 허용)
- 체결 품질(Execution Quality) 추가
  - `/api/execution-qualities` 조회 API
  - 종목별 체결률/평균 수수료·슬리피지(bps)/순현금흐름 기반 등급(A~D) 계산
- 주문 건전성(Order Health) 추가
  - `/api/order-health` 조회 API
  - 오픈주문 수/지연주문 수/오더에이징/한도사용률 기반 HEALTHY/WARN/CRITICAL 계산
  - `/api/order-health/remediate-stale` 조치 API
  - 지연 주문 일괄 취소(자동 사유/행위자 감사로그 포함) 지원
- 포트폴리오 카탈로그(Portfolio Catalog) 추가
  - `/api/portfolios` 조회 API
  - `portfolioId` 외에 `portfolioCode/portfolioName/전략/벤치마크` 메타 제공
- 주문 워크벤치(Order Workbench) 집계 API 추가
  - `/api/orders/workbench` 조회 API
  - 상태별 주문 집계/오픈주문 금액/스테일 주문/상위 심볼 집중도 집계 제공
- 포트폴리오 인사이트(Portfolio Insight) API 추가
  - `/api/portfolio-summaries/insight` 조회 API
  - 건강점수(0~100)/건강상태(HEALTHY/WARN/CRITICAL) 계산
  - 주요 집중 종목(top concentration) 및 상위 익스포저 비중 집계
  - 리스크 경보 개수(CRITICAL/WARN), 거래 활성 여부(tradingEnabled) 통합 제공
- 수익 실행 플레이북(Profit Playbook) API 추가
  - `/api/portfolio-summaries/profit-playbook` 조회 API
  - 포트폴리오별 목표/시장국면/우선순위 점수/차단이슈/실행 액션 목록 제공
  - 실행 액션에 담당역할/기대효과/화면 이동 경로를 포함하여 즉시 실행 UX 지원
- 플레이북 액션 피드백 루프(Feedback Loop) API 추가
  - `/api/portfolio-summaries/profit-playbook/feedback` 조회 API
  - work-queue 액션 실행 시 포트폴리오 스냅샷(before/after) 자동 저장
  - 손익/회전율/오픈주문/리스크경보 변화량과 결과평가(POSITIVE/NEUTRAL/NEGATIVE/FAILED) 제공
- 주문 대량 액션(Bulk Actions) API 추가
  - `/api/orders/bulk/cancel` 일괄 취소
  - `/api/orders/bulk/reject` 일괄 거부
  - 요청 단위 성공/실패 건수 및 주문별 처리 결과 반환
- 저장 뷰(Saved Views) API 추가
  - `/api/saved-views` 조회/생성/삭제 API
  - 공용(shared)/개인(owner) 뷰 분리 지원
  - 공용(shared) 생성은 `ADMIN/RISK`만 허용
  - 삭제는 `소유자` 또는 `ADMIN`만 허용
- 기본 뷰(Default View) 고정 API 추가
  - `/api/saved-views/default` 조회/설정 API
  - 사용자별 리소스(orders) 기본 뷰 pin 저장/조회 지원
  - 접근 가능한 뷰(shared 또는 본인 소유)만 기본 뷰 지정 허용
- 비밀번호 변경 정책 강화
  - 현재 비밀번호 검증
  - 최근 비밀번호 재사용 방지

## 동적 메뉴 권한 검증

- 관리자 메뉴 응답: `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-menus-admin.json`
- 트레이더 메뉴 응답: `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-menus-trader.json`
- 검증 규칙:
  - trader 계정은 `users` 메뉴 미노출 (스크립트 자동 검증 PASS)

## API 권한 차단(403) 검증

- `trader@quant.io` -> `GET /api/users` 요청 시 `403 FORBIDDEN` PASS
- `viewer@quant.io` -> `POST /api/orders` 요청 시 `403 FORBIDDEN` PASS
- `viewer@quant.io` -> `GET /api/orders/audit-logs` 요청 시 `403 FORBIDDEN` PASS
- `viewer@quant.io` -> `GET /api/orders/audit-logs/summary` 요청 시 `403 FORBIDDEN` PASS
- `trader@quant.io` -> `POST /api/orders/{orderId}/cancel` 요청 시 `403 FORBIDDEN` PASS
- `trader@quant.io` -> `PUT /api/risk-limits` 요청 시 `403 FORBIDDEN` PASS
- `trader@quant.io` -> `PUT /api/risk-limits/trading-controls` 요청 시 `403 FORBIDDEN` PASS
- `viewer@quant.io` -> `POST /api/risk-alerts/ack` 요청 시 `403 FORBIDDEN` PASS
- `viewer@quant.io` -> `POST /api/risk-alerts/workflow` 요청 시 `403 FORBIDDEN` PASS
- `viewer@quant.io` -> `POST /api/order-health/remediate-stale` 요청 시 `403 FORBIDDEN` PASS
- `viewer@quant.io` -> `POST /api/saved-views` 요청 시 `403 FORBIDDEN` PASS
- `trader@quant.io` -> `POST /api/saved-views(shared=true)` 요청 시 `403 FORBIDDEN` PASS
- `trader@quant.io` -> `DELETE /api/saved-views/{viewId}(타인 소유)` 요청 시 `403 FORBIDDEN` PASS
- `trader@quant.io` -> `POST /api/orders/bulk/reject` 요청 시 `403 FORBIDDEN` PASS
- `viewer@quant.io` -> `POST /api/saved-views/default(viewId=private)` 요청 시 `400 BAD_REQUEST` PASS
- `viewer@quant.io` -> `POST /api/account/work-queue/actions/remediate-stale-orders` 요청 시 `403 FORBIDDEN` PASS
- `viewer@quant.io` -> `POST /api/account/work-queue/actions/post-approved-vouchers` 요청 시 `403 FORBIDDEN` PASS
- `viewer@quant.io` -> `POST /api/account/work-queue/actions/approve-draft-vouchers` 요청 시 `403 FORBIDDEN` PASS
- `viewer@quant.io` -> `POST /api/account/work-queue/actions/pause-trading` 요청 시 `403 FORBIDDEN` PASS
- `viewer@quant.io` -> `POST /api/account/work-queue/actions/emergency-risk-response` 요청 시 `403 FORBIDDEN` PASS
- `viewer@quant.io` -> `POST /api/account/work-queue/actions/resume-trading` 요청 시 `403 FORBIDDEN` PASS
- 토큰 미포함 -> `GET /api/users` 요청 시 `401 UNAUTHORIZED` PASS
- 아티팩트:
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-users-trader-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-orders-viewer-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-order-audits-viewer-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-order-audits-summary-viewer-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-orders-trader-cancel-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-risk-limits-trader-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-trading-controls-trader-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-risk-alerts-ack-viewer-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-risk-alerts-workflow-viewer-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-order-health-remediate-viewer-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-saved-views-viewer-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-saved-views-trader-shared-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-saved-views-trader-delete-foreign-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-orders-bulk-reject-trader-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-saved-views-default-private-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-work-queue-remediate-viewer-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-work-queue-post-approved-vouchers-viewer-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-work-queue-approve-draft-vouchers-viewer-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-work-queue-pause-trading-viewer-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-work-queue-emergency-risk-response-viewer-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-work-queue-resume-trading-viewer-forbidden.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-users-unauthorized.json`

## 통합 테스트(MockMvc)

- 테스트 클래스:
  - `/Users/revy/workspace_codex/spring_boot_Quant/backend-mvp/src/test/java/com/quant/mvp/api/SecurityAuthorizationIntegrationTest.java`
- 검증 항목:
  - 무토큰 보호 API 접근 시 `401`
  - admin 로그인 토큰으로 보호 API 접근 성공
  - trader 토큰으로 `/api/users` 접근 시 `403`
  - viewer 토큰으로 주문 삭제 시 `403`
  - trader 토큰으로 주문 취소 시 `403`
  - viewer 토큰으로 `/api/orders/audit-logs/summary` 접근 시 `403`
  - 감사로그 요약 API 응답(`actionCounters/transitionCounters/topActors`) 검증

## 검증 아티팩트

- OpenAPI 문서:
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/openapi.json`
- 대표 API 결과:
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-users.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-roles.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-menus.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-menu-permissions.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-orders-cancel.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-orders-reject.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-orders-advanced-filter.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-orders-advanced-filter-trade.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-orders-insight-admin.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-orders-insight-trader.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-order-audits.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-order-audits-admin-filter.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-order-audits-summary.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-trading-controls-disable.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-trading-controls-enable.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-trading-controls-history.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-trading-controls.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-risk-alerts.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-risk-alerts-overview.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-risk-alerts-kill-switch.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-risk-alerts-ack.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-risk-alerts-unack.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-risk-alerts-workflow-in-progress.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-risk-alerts-workflow-resolved.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-risk-alerts-workflow-open.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-portfolios.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-orders-workbench.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-orders-bulk-cancel.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-orders-bulk-reject.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-saved-views.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-saved-views-delete.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-saved-views-default-pin.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-saved-views-default.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-order-health.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-order-health-remediate.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-execution-qualities.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-portfolio-summaries-insight.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-portfolio-profit-playbook.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-orders-kill-switch-blocked.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-me.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-menus-admin.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-menus-trader.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-work-queue-admin.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-work-queue-viewer.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-activity-feed-admin.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-activity-feed-viewer.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-work-queue-remediate.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-work-queue-revoke-sessions.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-work-queue-post-approved-vouchers.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-work-queue-approve-draft-vouchers.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-work-queue-resume-trading.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-work-queue-pause-trading.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-work-queue-emergency-risk-response.json`
  - `/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts/api-account-work-queue-emergency-order-after.json`

## 결론

- 엔터프라이즈 관리 기능 + 사용자별 메뉴 권한까지 백엔드 반영 완료
- JWT 인증 + 서버측 삭제 API + 권한 강제까지 반영 완료
- OpenAPI 전수 테스트 PASS
- 단위 테스트 PASS
