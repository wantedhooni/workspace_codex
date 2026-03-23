# sample_google_oauth

Spring Boot와 React를 분리해 구성한 Google OAuth 로그인 및 회원가입 샘플이다. 백엔드는 Spring Security OAuth2 Client로 Google 인증을 처리하고 로그인 직후 기본 회원 정보를 H2에 저장하며, 가입 완료 시 서비스 프로필을 갱신한다. 프론트엔드는 세션 기반 로그인 상태 조회와 추가 가입 입력 화면을 제공한다.

## 목적

- Google 소셜 로그인을 실무형 분리 아키텍처로 빠르게 검증할 수 있는 샘플 제공
- 백엔드에서 OAuth 인증, 세션 관리, 로그인 직후 회원 기본정보 저장, 가입 프로필 저장을 담당하고 프론트엔드는 상태 조회와 가입 경험에 집중
- 로컬 개발 환경에서 `.env` 설정만 채우면 바로 실행 가능한 구조 제공

## 디렉터리 구조

```text
sample_google_oauth
├── AGENTS.md
├── PLANS.md
├── TASK.md
├── README.md
├── .env.example
├── backend
├── frontend
├── runtime
└── scripts
```

## 기술 구성

- Backend
  - Spring Boot 3.4.4
  - Spring Security
  - OAuth2 Client
  - Spring Data JPA
  - H2
- Frontend
  - React 19
  - Vite 6
  - TypeScript

## 인증 및 회원가입 흐름

1. 프론트엔드에서 `Google로 로그인` 버튼을 클릭한다.
2. 브라우저가 백엔드 `GET /oauth2/authorization/google` 로 이동한다.
3. Spring Security가 Google 인증 후 세션을 생성한다.
4. 백엔드는 프론트엔드로 다시 리다이렉트한다.
5. 로그인 성공 핸들러가 Google 계정의 기본 정보(email, name, picture, providerUserId)를 `oauth_member` 테이블에 저장하거나 갱신한다.
6. 프론트엔드는 `GET /api/auth/me` 호출로 현재 사용자 정보를 조회한다.
7. 가입 완료 전 상태라면 추가 프로필 입력 폼을 노출한다.
8. `POST /api/auth/signup` 으로 표시 이름, 조직, 직무를 저장해 가입을 완료한다.

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
GOOGLE_CLIENT_ID=발급받은_클라이언트_ID
GOOGLE_CLIENT_SECRET=발급받은_클라이언트_SECRET
APP_FRONTEND_URL=http://localhost:5173
APP_LOGIN_SUCCESS_PATH=/?login=success
APP_LOGIN_FAILURE_PATH=/?login=error
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

### Backend

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_google_oauth/backend
export GOOGLE_CLIENT_ID=발급받은_클라이언트_ID
export GOOGLE_CLIENT_SECRET=발급받은_클라이언트_SECRET
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

### 로그인 시작

```bash
open http://localhost:8087/oauth2/authorization/google
```

### 로그인 사용자 조회

```bash
curl http://localhost:8087/api/auth/me \
  -H "Cookie: JSESSIONID=<세션값>"
```

로그인만 완료한 직후에도 `oauth_member` 테이블에는 기본 회원 정보가 저장된다. 이 시점에는 `registered=false` 상태다.

### 회원가입 완료

```bash
curl -X POST http://localhost:8087/api/auth/signup \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=<세션값>" \
  -d '{
    "displayName": "리비",
    "organization": "플랫폼실",
    "jobTitle": "백엔드 엔지니어",
    "marketingConsent": true
  }'
```

### 로그아웃

```bash
curl -X POST http://localhost:8087/api/auth/logout \
  -H "Cookie: JSESSIONID=<세션값>"
```

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_google_oauth/backend
./gradlew test

cd /Users/revy/workspace_codex/spring_sample/sample_google_oauth/frontend
npm install
npm run build

cd /Users/revy/workspace_codex/spring_sample/sample_google_oauth
sh -n scripts/all-start.sh
sh -n scripts/all-stop.sh
sh -n scripts/all-restart.sh
```

## 확인 포인트

- 로그인 성공 후 프론트엔드로 정상 복귀하는지 확인
- 최초 로그인 계정은 회원가입 입력 폼이 표시되는지 확인
- OAuth 로그인 직후 H2 `oauth_member` 테이블에 기본 회원 정보가 저장되는지 확인
- 회원가입 완료 후 동일 레코드의 `registered`, `display_name`, `organization`, `job_title` 값이 갱신되는지 확인
- 새로고침 후에도 가입 상태와 사용자 정보가 유지되는지 확인
- 로그아웃 후 사용자 정보 카드가 초기 상태로 돌아가는지 확인
- Google Cloud Console의 테스트 사용자 제한에 따라 허용 계정만 로그인되는지 확인
