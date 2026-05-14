# web-admin-agent

Next.js 16 App Router 기반 ADMIN-API 관리자 웹 프로젝트입니다.

## 기술 스택
- Next.js 16.2.6
- React 19.2.4
- TypeScript
- Tailwind CSS 4

## API 학습 요약
- API 명세 파일: `api-docs.json`
- OpenAPI 버전: 3.1.0
- API 서버 기본값: `http://localhost:8081`
- 인증 방식: Bearer Token

### 엔드포인트 그룹
- 인증: `POST /api/v1/auth/login`, `POST /api/v1/auth/logout`, `POST /api/v1/auth/refresh`, `GET /api/v1/auth/me`
- 관리자: `GET/POST /api/v1/admin`, `GET/PATCH/DELETE /api/v1/admin/{id}`
- 계좌: `GET/POST /api/v1/account`, `GET/PATCH/DELETE /api/v1/account/{id}`
- 계좌 거래: `GET/POST /api/v1/accounttransaction`, `GET/PATCH/DELETE /api/v1/accounttransaction/{id}`

### 주요 요청/응답 모델
- `LoginRequest`: `email`, `password`
- `RefreshTokenRequest`: `refreshToken`
- `LogoutRequest`: `refreshToken`
- `AdminAuthResponse`: `accessToken`, `refreshToken`, `tokenType`
- 공통 응답: `success`, `data`, `message`, `timestamp`
- 페이지 응답: `content`, `totalElements`, `totalPages`, `page`, `size`

계좌와 계좌 거래의 상세 request/response schema는 현재 명세에서 `object` 또는 `Void`로 표시되어 있어 실제 화면 컬럼 구현 전 백엔드 응답 예시 확인이 필요합니다.

목록 검색 query는 `page`, `size`와 도메인 검색 필드를 flat query parameter로 전송합니다. 예: `?page=0&size=20&email=admin@example.com`.

## 구현 기능
- 로그인 페이지와 JWT 로그인 처리
- JWT 세션 localStorage 저장/복원
- accessToken 만료 시 refreshToken으로 자동 갱신 후 요청 재시도
- 로그아웃 API 호출 및 브라우저 세션 삭제
- admin 대시보드 index 페이지
- 대시보드 좌측 메뉴
- 관리자, 계좌, 계좌 거래 도메인 CRUD 화면
- 도메인별 별도 검색 필터 섹션
- REST API 요청 기반 공통 AG Grid 템플릿
- 생성/수정/삭제 후 목록 자동 재조회
- 행 선택 시 단건 상세 API 조회 후 수정 폼 반영
- 페이지 크기 변경과 이전/다음 페이지 이동

## API 클라이언트 구조
- `src/services/api-client.ts`: `ApiClient`, `AuthStorageService`, `QueryStringService`
- `ApiClient`: axios 인스턴스와 단일 `request` 메서드로 `NEXT_PUBLIC_API_URL` 기준 REST 요청을 보내고, JWT 첨부와 refresh token 갱신을 담당합니다.
- axios request interceptor에서 accessToken을 자동 첨부하고, response interceptor에서 401 응답 시 refresh 후 원 요청을 1회 재시도합니다.
- `AuthStorageService`: localStorage 세션 저장/복원/구독을 담당합니다.
- `src/services/auth-service.ts`: `auth-controller` 로그인, 로그아웃, refresh, me API를 담당합니다.
- `src/services/base-crud-service.ts`: 검색, 단건 조회, 생성, 수정, 삭제 공통 CRUD 요청을 담당합니다.
- `src/services/admin-service.ts`: `admin-controller` API를 담당합니다.
- `src/services/account-service.ts`: `account-controller` API를 담당합니다.
- `src/services/account-transaction-service.ts`: `account-transaction-controller` API를 담당합니다.
- 화면은 `AuthService`와 도메인별 CRUD 서비스를 직접 주입받습니다. 중간 파사드는 사용하지 않습니다.

## API 설정

브라우저에서 호출할 백엔드 ADMIN-API 서버는 `NEXT_PUBLIC_API_URL`로 변경합니다.

```bash
NEXT_PUBLIC_API_URL=http://localhost:8081 npm run dev
```

`admin-proxy`는 사용하지 않습니다. 브라우저가 백엔드를 직접 호출하므로 백엔드 CORS 설정에서 웹 origin인 `http://localhost:3333`을 허용해야 합니다.

## 실행 방법

```bash
npm install
script/all-start.sh
```

실행 후 기본 접속 정보:
- 웹 URL: `http://localhost:3333`
- API URL: `http://localhost:8081`
- 데모 계정: `admin@example.com` / `Qwer1234!`

## 종료와 재시작

```bash
script/all-stop.sh
script/all-restart.sh
```

## 개발 명령

```bash
npm run dev
npm run build
npm run lint
```

## 작업 문서
- `AGENTS.md`: 프로젝트 작업 지침
- `PLANS.md`: 개발 계획과 API 학습 내용
- `TASK.md`: 작업 기록

## 검증

```bash
npm run lint
npm run build
```
