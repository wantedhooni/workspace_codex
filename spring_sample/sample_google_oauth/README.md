# sample_google_oauth

Spring Boot와 React를 분리해 구성한 Google OAuth 로그인/회원가입 샘플이다. 세션은 사용하지 않고, OAuth 승인 요청과 refresh token은 Redis로 관리하며, OAuth 완료 이후 API 인증은 JWT Bearer 토큰으로 처리한다. 로그인 직후 기본 회원 정보는 H2 RDBMS에 저장되고, 추가 회원가입 완료 시 동일 레코드가 갱신된다.

## 목적

- Google OAuth 이후를 세션이 아닌 JWT 기반으로 운영하는 실무형 구조 제공
- 스케일 아웃 환경에서 OAuth state, refresh token을 Redis로 공유하는 패턴 제공
- OAuth 로그인 직후 회원 기본 정보 저장과 가입 완료 프로필 갱신을 RDBMS에서 확인할 수 있게 구성

## 디렉터리 구조

```text
sample_google_oauth
├── AGENTS.md
├── PLANS.md
├── TASK.md
├── README.md
├── .env.example
├── docker-compose.yml
├── backend
├── frontend
├── runtime
└── scripts
```

## 기술 구성

- Backend
  - Spring Boot 3.4.4
  - Spring Security OAuth2 Client
  - JWT(`jjwt`)
  - Spring Data JPA
  - H2
  - Redisson
- Frontend
  - React 19
  - Vite 6
  - TypeScript
- Infra
  - Redis 7
  - Docker Compose

## 인증 구조

1. 프론트엔드가 `GET /oauth2/authorization/google` 로 브라우저를 이동시킨다.
2. OAuth authorization request는 Redis에 저장된다.
3. Google 로그인 성공 후 백엔드가 회원 기본 정보를 H2 `oauth_member` 테이블에 upsert한다.
4. 백엔드는 JWT access token / refresh token을 발급한다.
5. refresh token은 Redis에 저장된다.
6. 프론트엔드는 URL 파라미터로 전달받은 토큰을 저장하고 이후 API를 Bearer 방식으로 호출한다.
7. access token 만료 시 `POST /api/auth/refresh` 로 새 토큰 쌍을 발급받는다.
8. 회원가입 완료 전 상태라면 `POST /api/auth/signup` 으로 추가 프로필을 저장한다.

## 저장 위치

### Redis

- OAuth 승인 요청(state 기반)
- refresh token

### RDBMS(H2)

- OAuth 로그인 직후 생성되는 회원 기본 정보
- 가입 완료 여부(`registered`)
- 표시 이름, 조직, 직무, 마케팅 동의 여부
- 마지막 로그인 시각

## 사전 준비

### 1. Google OAuth 앱 생성

Google Cloud Console에서 OAuth Client ID를 만든 뒤 아래 값을 등록한다.

- 승인된 JavaScript 원본: `http://localhost:5173`
- 승인된 리디렉션 URI: `http://localhost:8087/login/oauth2/code/google`

### 2. 환경 변수 파일 작성

루트에 `.env` 파일을 만들고 아래 값을 채운다.

```env
BACKEND_PORT=8087
FRONTEND_PORT=5173
REDIS_PORT=6379
GOOGLE_CLIENT_ID=발급받은_클라이언트_ID
GOOGLE_CLIENT_SECRET=발급받은_클라이언트_SECRET
APP_FRONTEND_URL=http://localhost:5173
APP_LOGIN_SUCCESS_PATH=/
APP_LOGIN_FAILURE_PATH=/
APP_JWT_SECRET=충분히_긴_JWT_서명_시크릿
APP_JWT_ACCESS_TOKEN_MINUTES=30
APP_JWT_REFRESH_TOKEN_DAYS=7
APP_REDISSON_ADDRESS=redis://localhost:6379
APP_REDISSON_PASSWORD=
```

프론트엔드용 환경 변수는 아래 예제를 복사해 사용한다.

```bash
cp frontend/.env.local.example frontend/.env.local
```

기본값은 아래와 같다.

```env
VITE_API_BASE_URL=http://localhost:8087
```

## 실행 방법

### 전체 시작

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_google_oauth
cp .env.example .env
cp frontend/.env.local.example frontend/.env.local
./scripts/all-start.sh
```

실행 후 확인 정보:

- Redis: `redis://localhost:6379`
- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8087`
- Redirect URI: `http://localhost:8087/login/oauth2/code/google`
- H2 Console: `http://localhost:8087/h2-console`

### 전체 중지

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_google_oauth
./scripts/all-stop.sh
```

### 전체 재시작

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_google_oauth
./scripts/all-restart.sh
```

## 개별 실행

### Redis

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_google_oauth
docker compose up -d redis
```

### Backend

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_google_oauth/backend
export GOOGLE_CLIENT_ID=발급받은_클라이언트_ID
export GOOGLE_CLIENT_SECRET=발급받은_클라이언트_SECRET
export APP_JWT_SECRET=충분히_긴_JWT_서명_시크릿
export APP_REDISSON_ADDRESS=redis://localhost:6379
./gradlew bootRun
```

### Frontend

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_google_oauth/frontend
npm install
cp .env.local.example .env.local
npm run dev
```

## 주요 API

### 현재 로그인 사용자 조회

```bash
curl http://localhost:8087/api/auth/me \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

### 회원가입 완료

```bash
curl -X POST http://localhost:8087/api/auth/signup \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "displayName": "리비",
    "organization": "플랫폼실",
    "jobTitle": "백엔드 엔지니어",
    "marketingConsent": true
  }'
```

### 토큰 재발급

```bash
curl -X POST http://localhost:8087/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<REFRESH_TOKEN>"
  }'
```

### 로그아웃

```bash
curl -X POST http://localhost:8087/api/auth/logout \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<REFRESH_TOKEN>"
  }'
```

## 토큰 처리 참고

- 이 샘플은 데모 단순화를 위해 OAuth 성공 후 JWT를 프론트엔드 URL query parameter로 전달한다.
- 실제 운영에서는 백엔드 포 프론트엔드(BFF), 짧은 수명 access token, HttpOnly cookie, HTTPS 강제, CSP/redirect whitelist를 함께 검토하는 편이 안전하다.

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_google_oauth/backend
./gradlew test

cd /Users/revy/workspace_codex/spring_sample/sample_google_oauth/frontend
npm run build

cd /Users/revy/workspace_codex/spring_sample/sample_google_oauth
sh -n scripts/all-start.sh
sh -n scripts/all-stop.sh
sh -n scripts/all-restart.sh
FRONTEND_PORT=5184 APP_FRONTEND_URL=http://localhost:5184 ./scripts/all-start.sh
./scripts/all-stop.sh
```

## 확인 포인트

- OAuth 로그인 직후 H2 `oauth_member` 테이블에 기본 회원 정보가 저장되는지 확인
- OAuth 승인 요청과 refresh token이 Redis에 저장되는지 확인
- 프론트가 access token을 `Authorization: Bearer` 헤더로 전송하는지 확인
- access token 만료 시 refresh token으로 자동 재발급되는지 확인
- 로그아웃 후 Redis refresh token이 제거되고 API가 다시 401을 반환하는지 확인
