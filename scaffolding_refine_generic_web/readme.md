# 통합 포털 (Admin)

generic web CRUD 패턴과 refine.dev를 기반으로 한 통합 포털 관리자 UI/백엔드 예제입니다.

## 구조
- backend: `backand/server/api-admin-server`
- frontend: `frontend/admin-ui`

참고: 현재 저장소 폴더명이 `backend`가 아니라 `backand`로 되어 있습니다.

## 백엔드 (Spring Boot)
- 실행
  - `cd backand`
  - `PORT=8090 ./gradlew :server:api-admin-server:bootRun --args="--server.port=8090"`
- 기본 포트: `8090` (스크립트 기준)
- Swagger UI: `/swagger-ui/index.html`

## 프론트엔드 (Refine + Ant Design)
- 실행
  - `cd frontend/admin-ui`
  - `npm install`
  - `VITE_API_URL=http://localhost:8090 npm run dev -- --host localhost --port 5173`
- 기본 포트: `5173` (스크립트 기준)
- 로그인 쿠키(SameSite=Lax) 동작을 위해 프론트 접속은 `http://localhost:5173` 사용을 권장합니다.
- API 변경: `.env`에 `VITE_API_URL=http://localhost:8090`

## 빌드/실행 스크립트
- 빌드: `./scripts/build.sh`
- 백엔드 실행: `./scripts/run-backend.sh`
- 프론트 실행: `./scripts/run-frontend.sh`
- 전체 실행: `./scripts/run-all.sh` (기본 `BACKEND_PORT=8090`, `API_URL=http://localhost:8090`)

## 인프라 (Docker Compose)
- Postgres 기동: `docker compose up -d`
- 기본 DB는 Postgres로 설정되어 있습니다.
- 기본 포트: `55433`
- DB 설정
  - `backand/server/api-admin-server/src/main/resources/application.yml`에서 확인/수정

## 테스트 실행(참고)
- 로컬 포트 점유가 있을 수 있어 테스트는 아래 포트로 수행했습니다.
  - Postgres: `55433`
  - Backend: `8090` (예시)
  - Frontend: `5173`
- 예시 명령
  - `docker run --rm -d --name portal-postgres-test -e POSTGRES_DB=portal_admin -e POSTGRES_USER=portal -e POSTGRES_PASSWORD=portal1234 -p 55433:5432 postgres:16`
  - `cd backand && ./gradlew :server:api-admin-server:bootRun --args='--server.port=8090 --spring.datasource.url=jdbc:postgresql://localhost:55433/portal_admin --spring.datasource.username=portal --spring.datasource.password=portal1234'`
  - `cd frontend/admin-ui && VITE_API_URL=http://localhost:8090 npm run dev -- --host localhost --port 5173`
- 기본 접속 정보
  - DB: `portal_admin`
  - User: `portal`
  - Password: `portal1234`

## 데모 계정
- 애플리케이션 시작 시 기본 권한/역할/메뉴/콘텐츠와 함께 아래 계정이 자동 생성(보정)됩니다.
- 인증 방식은 데모용으로 평문 비밀번호 비교를 사용합니다.
- `admin / admin1234` (`SUPER_ADMIN`)
- `tester / tester1234` (`CONTENT_MANAGER`)
- `ops / ops1234` (`OPS_MANAGER`)
- `manager / manager1234` (`AUDITOR`)
- `security / security1234` (`SECURITY_ADMIN`)

## 기본 리소스
- Admin Users
- Roles / Permissions
- Menus
- Contents
- Common Codes
- Programs
- Role Routes
- Access Logs
- Service Audit Logs
- Batch Jobs / Batch Schedules
- Banners
- Login Policies
- Statistics (Dashboard)

## 로그/통계 동작
- Access Log는 요청 단위로 자동 적재됩니다. (`/swagger*`, `/v3/api-docs*`, `/actuator*`, `/error` 제외)
- Service Audit Log는 대부분 CRUD 생성/수정/삭제 시 자동 적재됩니다. (로그 테이블 자체 CRUD는 제외)
- 통계 API
  - `GET /statistics/overview`
  - `GET /statistics/menu-usage?limit=10`
  - `GET /statistics/action-usage?limit=10`
  - `GET /statistics/dashboard?limit=10`
- Statistics Dashboard (`/statistics`)
