# 해외 파생상품 운영 관리 프로그램 MVP

해외 파생 운영 핵심 업무(계좌/포지션/증거금 조회, 입출금/환전 요청, 배치 모니터링, 감사로그)를 위한 내부 관리자 시스템입니다.

## Stack
- Backend: Spring Boot 3, Spring Security, JPA, Querydsl, JWT, PostgreSQL
- Frontend: Refine + React + MUI

## Project Structure
- `backend/`: API 서버
- `frontend/`: 관리자 웹 UI
- `PLAN.md`: TASK 체크리스트 및 진행 로그

## Quick Start

### 0) One Command Run/Stop
```bash
./script/start_all.sh
./script/stop_all.sh
```

### 1) Database
```bash
docker compose up -d
```

### 2) Backend
```bash
cd backend
mvn spring-boot:run
```

기본 접속: `http://localhost:8080`

샘플 계정
- `opsadmin / admin123!` (OPS_ADMIN)
- `opsviewer / viewer123!` (OPS_VIEWER)
- `auditor / audit123!` (AUDITOR)

### 3) Frontend
```bash
cd frontend
npm install
npm run dev
```

기본 접속: `http://localhost:5173`

## 주요 API
- `POST /api/v1/auth/login`
- `GET /api/v1/accounts`
- `GET /api/v1/accounts/{accountId}/summary`
- `POST /api/v1/cash-requests`
- `POST /api/v1/fx-requests`
- `POST /api/v1/requests/{id}/approve`
- `POST /api/v1/requests/{id}/reject`
- `GET /api/v1/batches/runs`
- `GET /api/v1/batches/runs/{runId}`
- `GET /api/v1/audit-logs`

## Notes
- 계좌번호 마스킹 해제(`unmask=true`)는 `OPS_ADMIN`만 허용됩니다.
- 요청 승인 시 브로커 연계는 모의 어댑터로 동작하며 최대 3회 재시도합니다.
- 로그인 페이지는 기본 데모 계정(`opsadmin / admin123!`)이 미리 입력됩니다.

## RSQL Filter
- 리스트 API는 `filter` 쿼리 파라미터로 RSQL 형식 필터를 받을 수 있습니다.
- 지원 연산자: `==`, `!=`, `=like=`, `=gt=`, `=ge=`, `=lt=`, `=le=`, `=in=`, `=out=`
- 예시:
  - `GET /api/v1/accounts?filter=status==ACTIVE;broker==CME`
  - `GET /api/v1/cash-requests?filter=status==PENDING;amount=ge=10000`
  - `GET /api/v1/batches/runs?filter=status==FAILED;retryCount=ge=1`
