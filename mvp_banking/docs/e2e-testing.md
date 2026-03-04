# E2E Smoke Test Guide

## 목적

- `scripts/e2e-smoke.sh`는 로컬 개발 스택이 기본적으로 정상 동작하는지 빠르게 확인하는 smoke 테스트다.
- 대상 범위는 `infra -> discovery-server -> admin-api / user-api -> api-gateway -> admin-portal / user-web-app` 전체다.
- 회귀 방지용 전체 시나리오 테스트라기보다, 배포 전과 로컬 변경 후에 핵심 경로가 살아있는지 확인하는 용도에 가깝다.

## 검증 대상

현재 smoke 테스트는 아래를 확인한다.

1. 인프라 및 서비스 health check
2. 관리자 로그인
3. 관리자 핵심 조회 API 접근
4. 관리자 알림 목록 및 읽음 처리
5. 사용자 로그인
6. 사용자 핵심 조회 API 접근
7. 사용자 알림 목록 및 읽음 처리
8. 프론트 dev 서버 응답 확인

실제 확인 경로는 다음과 같다.

- `http://localhost:8761/actuator/health`
- `http://localhost:8080/actuator/health`
- `http://localhost:5173`
- `http://localhost:5174`
- `POST /api/admin/auth/login`
- `GET /api/admin/me`
- `GET /api/admin/overview`
- `GET /api/admin/announcements`
- `GET /api/admin/customers?page=0&size=5`
- `GET /api/admin/funding-requests`
- `GET /api/admin/stock-orders`
- `GET /api/admin/stock-positions`
- `GET /api/admin/exchange-requests`
- `GET /api/admin/notifications`
- `POST /api/admin/notifications/{notificationId}/read`
- `POST /api/user/auth/login`
- `GET /api/user/me`
- `GET /api/user/dashboard/insights`
- `GET /api/user/announcements`
- `GET /api/user/accounts`
- `GET /api/user/funding-requests`
- `GET /api/user/stock-orders`
- `GET /api/user/stock-positions`
- `GET /api/user/exchange-requests`
- `GET /api/user/notifications`
- `POST /api/user/notifications/{notificationId}/read`

## 사전 조건

- 작업 경로: `/Users/revy/workspace_codex/mvp_banking`
- Docker 실행 가능 상태
- `infra/docker-compose.yml` 기준 `PostgreSQL`, `Redis` 실행 가능 상태
- 관리자/사용자 시드 계정 존재

기본 시드 계정:

- Admin: `admin@mvpbanking.local / Admin1234!`
- User: `user@mvpbanking.local / User1234!`

## 실행 방법

### 1. 전체 스택을 직접 올리고 테스트

```bash
cd /Users/revy/workspace_codex/mvp_banking
./scripts/e2e-smoke.sh
```

기본값은 아래와 같다.

- `START_STACK=1`
- `STOP_STACK=0`

즉, 필요한 스택을 자동으로 올리고 테스트한 뒤 스택은 유지한다.

### 2. 이미 떠 있는 스택을 재사용

```bash
cd /Users/revy/workspace_codex/mvp_banking
START_STACK=0 ./scripts/e2e-smoke.sh
```

이 모드는 이미 실행 중인 로컬 스택에 대해서만 검증한다.

권장 상황:

- 개발 중 이미 `./scripts/all-start.sh`를 실행한 상태
- 백엔드 또는 프론트 일부만 수정한 뒤 빠르게 재검증하고 싶은 경우

### 3. 테스트 후 스택 자동 종료

```bash
cd /Users/revy/workspace_codex/mvp_banking
START_STACK=1 STOP_STACK=1 ./scripts/e2e-smoke.sh
```

또는 이미 실행 중인 스택을 테스트한 뒤 내리고 싶다면:

```bash
cd /Users/revy/workspace_codex/mvp_banking
START_STACK=0 STOP_STACK=1 ./scripts/e2e-smoke.sh
```

주의:

- `STOP_STACK=1`이면 테스트 종료 시 `./scripts/all-stop.sh`가 호출된다.
- 이미 다른 용도로 띄워둔 스택도 함께 내려가므로 공유 환경에서는 주의가 필요하다.

## 내부 동작 순서

`scripts/e2e-smoke.sh`는 아래 순서로 동작한다.

1. 필요하면 `./scripts/all-start.sh` 실행
2. discovery server, gateway, 두 프론트 엔드포인트 응답 확인
3. 관리자 로그인 후 access token 추출
4. 관리자 보호 API 호출
5. 관리자 알림 목록 조회 후 첫 알림 읽음 처리
6. 사용자 로그인 후 access token 추출
7. 사용자 보호 API 호출
8. 사용자 알림 목록 조회 후 첫 알림 읽음 처리
9. 성공 시 `E2E smoke test passed.` 출력
10. `STOP_STACK=1`인 경우 `./scripts/all-stop.sh` 실행

## 성공 기준

아래 문구가 출력되면 smoke 테스트는 통과다.

```text
E2E smoke test passed.
```

## 실패 시 확인 순서

### 1. 서비스 상태 확인

```bash
cd /Users/revy/workspace_codex/mvp_banking
./scripts/status.sh
```

확인 포인트:

- `8761`, `8080`, `8081`, `8082`, `5173`, `5174` 포트 리스너
- PostgreSQL / Redis 실행 여부
- PID 파일 존재 여부

### 2. 로그 확인

로그 위치:

- `/Users/revy/workspace_codex/mvp_banking/.runtime/logs`

대표 로그 파일:

- `discovery-server.log`
- `api-gateway.log`
- `admin-api.log`
- `user-api.log`
- `admin-portal.log`
- `user-web-app.log`

### 3. 개별 서비스 수동 확인

```bash
curl http://localhost:8761/actuator/health
curl http://localhost:8080/actuator/health
curl http://localhost:8081/api/system/ping
curl http://localhost:8082/api/system/ping
```

### 4. 인증 실패 시 확인

- 시드 계정이 초기화되었는지 확인
- PostgreSQL 데이터가 예상과 다르게 남아 있는지 확인
- Redis에 남아 있는 토큰/세션 영향이 있는지 확인

필요하면 아래 순서로 재기동한다.

```bash
cd /Users/revy/workspace_codex/mvp_banking
./scripts/all-stop.sh
./scripts/all-start.sh
START_STACK=0 ./scripts/e2e-smoke.sh
```

## 한계

- 현재는 조회 중심 smoke 테스트다.
- 승인, 환전 생성, 주식 주문 생성, 부분 체결, 반려 같은 쓰기 시나리오는 포함하지 않는다.
- 브라우저 기반 UI 상호작용을 실제로 클릭/검증하지는 않고, 프론트 dev 서버 응답만 확인한다.

## 확장 권장 방향

다음 단계에서는 아래를 별도 e2e로 늘리는 것이 좋다.

1. 관리자 승인 플로우
2. 사용자 환전 요청 생성
3. 사용자 주식 주문 생성
4. 부분 체결 후 관리자 `complete-fill`
5. 감사 로그 적재 여부 확인
6. Playwright 기반 브라우저 UI e2e 추가

## 관련 파일

- 스크립트: [scripts/e2e-smoke.sh](/Users/revy/workspace_codex/mvp_banking/scripts/e2e-smoke.sh)
- 통합 시작: [scripts/all-start.sh](/Users/revy/workspace_codex/mvp_banking/scripts/all-start.sh)
- 통합 종료: [scripts/all-stop.sh](/Users/revy/workspace_codex/mvp_banking/scripts/all-stop.sh)
- 상태 확인: [scripts/status.sh](/Users/revy/workspace_codex/mvp_banking/scripts/status.sh)
- 루트 가이드: [README.md](/Users/revy/workspace_codex/mvp_banking/README.md)
