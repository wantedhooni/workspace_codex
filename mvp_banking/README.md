# MVP Banking Platform

뱅킹/증권 MVP 통합 워크스페이스입니다.

구성:
- `backend/platform-core`: 공통 Spring Boot 도메인, 영속성, 보안, 비즈니스 모듈
- `backend/discovery-server`: Spring Cloud 서비스 레지스트리
- `backend/api-gateway`: Spring Cloud API 게이트웨이
- `backend/admin-api`: Spring Boot 관리자 API 서버
- `backend/user-api`: Spring Boot 사용자 API 서버
- `frontend/admin-portal`: React + Refine 관리자 애플리케이션
- `frontend/user-web-app`: React 사용자 웹 애플리케이션
- `infra`: 로컬 PostgreSQL / Redis 실행 환경

모듈 README:
- `backend/README.md`
- `backend/platform-core/README.md`
- `backend/discovery-server/README.md`
- `backend/api-gateway/README.md`
- `backend/admin-api/README.md`
- `backend/user-api/README.md`
- `frontend/admin-portal/README.md`
- `frontend/user-web-app/README.md`
- `infra/README.md`

문서:
- `docs/system-architecture.md`
- `docs/e2e-testing.md`
- `docs/frontend-domain-template.md`

## 로컬 인프라 실행

```bash
cd /Users/revy/workspace_codex/mvp_banking
docker compose -f infra/docker-compose.yml up -d
```

## 백엔드 실행

```bash
cd /Users/revy/workspace_codex/mvp_banking/backend
./gradlew :discovery-server:bootRun --args='--spring.profiles.active=discovery-server'
./gradlew :admin-api:bootRun --args='--spring.profiles.active=admin-api'
./gradlew :user-api:bootRun --args='--spring.profiles.active=user-api'
./gradlew :api-gateway:bootRun --args='--spring.profiles.active=api-gateway'
```

백엔드 기본 접속 정보:
- Discovery Server: `http://localhost:8761`
- API Gateway HTTP: `http://localhost:8080`
- API Gateway Health: `http://localhost:8080/actuator/health`
- Admin API HTTP: `http://localhost:8081`
- Admin API Health: `http://localhost:8081/actuator/health`
- Admin API Ping: `http://localhost:8081/api/system/ping`
- User API HTTP: `http://localhost:8082`
- User API Health: `http://localhost:8082/actuator/health`
- User API Ping: `http://localhost:8082/api/system/ping`
- 공통 저장소: PostgreSQL + Redis
- 게이트웨이 라우팅:
  - `/api/admin/** -> mvp-banking-admin-api`
  - `/api/user/** -> mvp-banking-user-api`

시드 계정:
- 관리자: `admin@mvpbanking.local` / `Admin1234!`
- 사용자: `user@mvpbanking.local` / `User1234!`
- 데모 데이터셋: 고객 `100명` + 계좌/거래/승인 큐 기본 데이터
- 기본 데모 사용자 은행 계좌: `KRW` + `USD`

초기 API 목록:
- `POST /api/admin/auth/login`
- `POST /api/admin/auth/refresh`
- `POST /api/admin/auth/logout`
- `GET /api/admin/me`
- `GET /api/admin/overview`
- `GET /api/admin/announcements`
- `POST /api/admin/announcements`
- `POST /api/admin/announcements/{announcementId}/publish`
- `POST /api/admin/announcements/{announcementId}/archive`
- `GET /api/admin/customers`
- `GET /api/admin/accounts`
- `GET /api/admin/transactions`
- `GET /api/admin/fx-rates`
- `GET /api/admin/linked-bank-accounts`
- `POST /api/admin/linked-bank-accounts/{linkedBankAccountId}/activate`
- `POST /api/admin/linked-bank-accounts/{linkedBankAccountId}/block`
- `GET /api/admin/funding-requests`
- `GET /api/admin/exchange-requests`
- `GET /api/admin/stock-orders`
- `GET /api/admin/stock-positions`
- `GET /api/admin/approvals`
- `POST /api/admin/approvals/{approvalRequestId}/approve`
- `POST /api/admin/approvals/{approvalRequestId}/reject`
- `GET /api/admin/notifications`
- `POST /api/admin/notifications/{notificationId}/read`
- `GET /api/admin/audit-logs`
- `POST /api/user/auth/signup`
- `POST /api/user/auth/login`
- `POST /api/user/auth/refresh`
- `POST /api/user/auth/logout`
- `GET /api/user/me`
- `GET /api/user/dashboard/insights`
- `GET /api/user/announcements`
- `GET /api/user/accounts`
- `GET /api/user/linked-bank-accounts`
- `POST /api/user/linked-bank-accounts`
- `POST /api/user/linked-bank-accounts/{linkedBankAccountId}/primary`
- `POST /api/user/linked-bank-accounts/{linkedBankAccountId}/resend-verification`
- `POST /api/user/linked-bank-accounts/{linkedBankAccountId}/verify`
- `GET /api/user/funding-requests`
- `POST /api/user/funding-requests`
- `POST /api/user/funding-requests/{requestId}/cancel`
- `GET /api/user/transactions`
- `GET /api/user/fx-rates`
- `GET /api/user/exchange-requests`
- `POST /api/user/exchange-requests`
- `POST /api/user/exchange-requests/{requestId}/cancel`
- `GET /api/user/stock-orders`
- `GET /api/user/stock-positions`
- `POST /api/user/stock-orders`
- `POST /api/user/stock-orders/{orderId}/cancel`
- `GET /api/user/notifications`
- `POST /api/user/notifications/{notificationId}/read`

관리자 목록 조회 쿼리 옵션:
- `GET /api/admin/customers?query=&status=&createdFrom=&createdTo=&page=&size=`
- `GET /api/admin/customers/summary?query=&createdFrom=&createdTo=`
- `GET /api/admin/accounts?query=&status=&accountType=&minBalance=&maxBalance=&page=&size=`
- `GET /api/admin/transactions?query=&status=&transactionType=&minAmount=&maxAmount=&occurredFrom=&occurredTo=&page=&size=`
- 관리자 목록은 `sortBy` / `sortDir` 지원
- 예시: `GET /api/admin/customers?sortBy=fullName&sortDir=asc`
- 예시: `GET /api/admin/accounts?sortBy=balance&sortDir=desc&minBalance=100000000`
- 예시: `GET /api/admin/transactions?sortBy=amount&sortDir=desc&minAmount=2000000&occurredFrom=2026-03-01`

마스킹 정책:
- 관리자 고객 이름/이메일 응답 마스킹
- 관리자 계좌번호 응답 마스킹

## Admin Portal 실행

```bash
cd /Users/revy/workspace_codex/mvp_banking/frontend/admin-portal
npm install
npm run dev
```

Admin Portal 기본 정보:
- URL: `http://localhost:5173`
- 시드 계정: `admin@mvpbanking.local / Admin1234!`
- 라우트: `/`, `/customers`, `/accounts`, `/transactions`, `/fx-rates`, `/linked-bank-accounts`, `/funding-requests`, `/exchange-requests`, `/stock-orders`, `/stock-positions`, `/approvals`, `/announcements`, `/notifications`, `/audit-logs`
- 관리자 라우트 페이지 단위 lazy loading 적용
- 고급 필터: 잔액 범위, 금액 범위, 거래 기간 필터
- 고객 페이지: 상태 요약 카드, 고객 생성일 범위 필터
- 마켓 운영 화면: FX, 입출금 큐, 환전 큐, 주식 주문 큐
- Overview: 승인 백로그, 심사 필요 고객, 시세 신선도, 운영 알림, 입출금 대기, 지시 대기 볼륨
- 운영 인박스: 미읽음 배지, 심각도별 알림, 읽음 처리, 도착 시각/액션 경로 확인
- 연결계좌 운영: 사용자 외부 출금 계좌, stale verification 우선 점검, 운영 차단 액션
- 입출금 운영: 수동 심사 플래그, 일일 한도 초과, 당일/익영업일 정산 윈도우 즉시 확인
- 공지 운영: draft/publish/archive, 심각도, 대상, pin, 서비스 배너 관리
- 주식 운영: 체결 이력, 부분체결/잔여수량 완료, 정산 거래번호, 수수료/세금/순정산, 포지션 평가

## User Web Application 실행

```bash
cd /Users/revy/workspace_codex/mvp_banking/frontend/user-web-app
npm install
npm run dev
```

User Web Application 기본 정보:
- URL: `http://localhost:5174`
- 시드 계정: `user@mvpbanking.local / User1234!`
- 라우트: `/`, `/announcements`, `/accounts`, `/linked-bank-accounts`, `/funding-requests`, `/transactions`, `/fx-rates`, `/exchange-requests`, `/stock-orders`, `/stock-positions`, `/notifications`
- 기본 필터: 계좌 텍스트 필터, 거래 텍스트 필터, 거래 상태 필터
- 대시보드: 총자산, 현금/투자 자산 분리, 통화 노출, 액션보드, 상위 보유종목, 서비스 큐 반영
- 서비스 배너: 게시된 공지 중 critical/pinned 우선 공지 상단 노출
- 알림 센터: 미읽음 배지, 최근 이벤트 프리뷰, 환전/주식/포트폴리오 알림 읽음 처리
- 계좌 카드: 계좌별 최근 거래 연동 표시
- 연결계좌: 외부 출금 계좌 등록, 검증 대기 티켓, 만료/재발송 cooldown, 다음 재발송 시각, 인증 문구 제출, 기본 출금 전환, 활성/차단 상태 확인
- 입출금: 계좌 기반 입금/출금 티켓, 기본 목적지 자동 선택, 승인 후 예상 잔액, 컷오프/일일한도/수동심사 프리뷰, 요청 추적
- 마켓 화면: FX 계산기/환율보드, source/destination 기반 환전 티켓(총수령/수수료/순수령), 주식 주문 티켓(주문금액/수수료/세금/현금영향)
- 포트폴리오 확장: 포지션, 현재가, 평가금액, 평가손익, 체결이력, 부분체결 진행률, 잔여수량, 정산 상세

개발 노트:
- `frontend/admin-portal`은 `/api`, `/actuator`를 `http://localhost:8080`으로 프록시
- `frontend/user-web-app`은 `/api`, `/actuator`를 `http://localhost:8080`으로 프록시
- 백엔드 권장 기동 순서: `discovery-server -> admin-api / user-api -> api-gateway`

## 스크립트 명령

```bash
cd /Users/revy/workspace_codex/mvp_banking
./scripts/all-start.sh
./scripts/status.sh
./scripts/all-stop.sh
```

런타임 아티팩트:
- PID 파일: `/Users/revy/workspace_codex/mvp_banking/.runtime/pids`
- 로그 파일: `/Users/revy/workspace_codex/mvp_banking/.runtime/logs`
- `all-start.sh`는 Spring Cloud 스택 실행 전에 `8080`의 레거시 모놀리식 리스너를 정리
- start/stop 스크립트는 URL, 데모 계정, 로그 경로를 표준 출력으로 제공

권장 실행 순서:

```bash
cd /Users/revy/workspace_codex/mvp_banking
./scripts/all-start.sh
./scripts/e2e-smoke.sh
./scripts/all-stop.sh
```

제공 스크립트:
- `./scripts/infra-start.sh`
- `./scripts/infra-stop.sh`
- `./scripts/discovery-server-start.sh`
- `./scripts/discovery-server-stop.sh`
- `./scripts/api-gateway-start.sh`
- `./scripts/api-gateway-stop.sh`
- `./scripts/admin-api-start.sh`
- `./scripts/admin-api-stop.sh`
- `./scripts/user-api-start.sh`
- `./scripts/user-api-stop.sh`
- `./scripts/backend-start.sh`
- `./scripts/backend-stop.sh`
- `./scripts/admin-portal-start.sh`
- `./scripts/admin-portal-stop.sh`
- `./scripts/user-web-app-start.sh`
- `./scripts/user-web-app-stop.sh`
- `./scripts/status.sh`
- `./scripts/all-start.sh`
- `./scripts/all-stop.sh`
- `./scripts/e2e-smoke.sh`
- `./scripts/scaffold-frontend-domain.sh`

E2E 스모크 테스트:
- 기본 동작: 전체 스택 기동, 헬스체크 확인, admin/user 로그인, 핵심 API(연결계좌 생성/재발송/검증, 입출금 생성/취소, 알림 읽음) 검증
- 이미 실행 중인 스택 재사용: `START_STACK=0 ./scripts/e2e-smoke.sh`
- 테스트 후 스택 종료: `STOP_STACK=1 ./scripts/e2e-smoke.sh`
- 상세 가이드: `docs/e2e-testing.md`

프론트 도메인 스캐폴딩:
- user/admin 프론트 템플릿 생성: `./scripts/scaffold-frontend-domain.sh --domain transfer-limit --target both`
- 상세 가이드: `docs/frontend-domain-template.md`
