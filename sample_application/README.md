# 증권/계좌 서비스 샘플 시스템

채널계/계정계/정보계/대외계 분리를 반영한 샘플 구현입니다.

## 기술 스택
- backend: Java 21, Spring Boot, JPA, Querydsl, Spring Security(JWT), H2/PostgreSQL
- frontend: React, Next.js(App Router), TypeScript

## 디렉터리
- `backend`: 4계 도메인 API 서버
- `frontend`: 운영 콘솔 UI

## 핵심 도메인 반영
- 채널계: 신청 API + 멱등키 기반 중복 방지
- 계정계: 계좌/원장/잔액 트랜잭션 확정(비관적 락 + referenceId 중복 방지)
- 정보계: 확정 이벤트 적재/조회
- 대외계: 전문 송신 상태 추적 + 재전송
- EOD: 일 단위 스냅샷/대사 상태 기록(수동 실행 + 스케줄)

## 인증/RBAC
- 로그인 API: `POST /api/auth/login`
- 계정
  - 운영자(OPERATOR): `admin / admin1234`
  - 조회자(VIEWER): `auditor / audit1234`
- 인증 방식: `Authorization: Bearer <JWT>`
- 권한 정책
  - 조회 API: `VIEWER`, `OPERATOR`
  - 변경 API(신청/개설/입출금/대외전송/EOD실행): `OPERATOR`

## 실행 방법
### 1) 백엔드 실행
```bash
cd backend
gradle bootRun
```
- 기본 URL: `http://localhost:8080`

### 2) 프론트엔드 실행
```bash
cd frontend
npm install
npm run dev
```
- 기본 URL: `http://localhost:3000`
- API 대상: `NEXT_PUBLIC_API_BASE` (기본값 `http://localhost:8080`)

### 3) Docker Compose 실행
```bash
docker compose up --build
```
- frontend: `http://localhost:3000`
- backend: `http://localhost:8080`
- postgres: `localhost:5432`

### 4) 스크립트로 전체 기동/종료/재기동
```bash
./script/all-start.sh
./script/all-stop.sh
./script/all-restart.sh
```
- PID 파일: `.backend.pid`, `.frontend.pid`
- 로그 파일: `.logs/backend.log`, `.logs/frontend.log`

## 주요 API
- 인증
  - `POST /api/auth/login`
- 채널계
  - `POST /api/channel/applications`
- 계정계
  - `POST /api/accounts`
  - `GET /api/accounts`
  - `POST /api/accounts/{accountId}/transactions`
  - `POST /api/accounts/ledger/search` (Querydsl 조건 검색)
- 정보계
  - `GET /api/information/events`
- 대외계
  - `POST /api/external/messages`
  - `POST /api/external/messages/{messageId}/retry`
  - `GET /api/external/messages`
- EOD
  - `POST /api/eod/run`
  - `GET /api/eod/snapshots`

## 검증 결과
- backend: `gradle clean build` 성공
- frontend: `npm run typecheck`, `npm run lint`, `npm run build` 성공
- RBAC 스모크 테스트
  - `VIEWER`가 변경 API 호출 시 `403 FORBIDDEN`
  - `OPERATOR`는 변경 API 정상 처리
