# 해외 파생상품 운영 관리 프로그램 MVP

해외 파생 운영 핵심 업무(계좌/포지션/증거금 조회, 입출금/환전 요청, 배치 모니터링, 감사로그)를 위한 내부 관리자 시스템입니다.

## Stack
- Backend: Spring Boot 3, Spring Security, JPA, Querydsl, JWT, PostgreSQL
- Frontend: Refine + React + MUI

## Project Structure
- `backend/`: API 서버
- `frontend/`: 관리자 웹 UI
- `docs/`: 기능/화면/API/RSQL 문서
- `PLAN.md`: TASK 체크리스트 및 진행 로그

## Documentation
- `docs/README.md`: 문서 인덱스
- `docs/FEATURES.md`: 기능 개요/권한/도메인 규칙
- `docs/DOMAIN_TERMS.md`: 한글 도메인 용어집
- `docs/DOMAIN_OVERVIEW.md`: 도메인 모델/관계/주식 매수 처리 흐름
- `docs/BACKEND_API.md`: 백엔드 API 설명
- `docs/FRONTEND_GUIDE.md`: 프론트 화면/UX 가이드
- `docs/RSQL_FILTER.md`: RSQL 필터 사용 가이드

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
./gradlew bootRun
```

기본 접속: `http://localhost:8080`

AI 종목 추천 기능 사용 시
```bash
ollama serve
ollama pull llama3.1
```

필요 환경변수
- `OLLAMA_BASE_URL` 기본값: `http://localhost:11434`
- `OLLAMA_CHAT_MODEL` 기본값: `llama3.1`
- `AI_STOCK_RECOMMENDATION_PROVIDER` 기본값: `ollama`

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
- `GET /api/v1/portfolios/{accountId}`
- `GET /api/v1/stock-purchases`
- `GET /api/v1/stock-positions`
- `POST /api/v1/stock-recommendations`
- `GET /api/v1/exchange-rates`
- `GET /api/v1/exchange-rates/quote`
- `POST /api/v1/cash-requests`
- `POST /api/v1/fx-requests`
- `POST /api/v1/requests/{id}/approve`
- `POST /api/v1/requests/{id}/reject`
- `GET /api/v1/batches/runs`
- `GET /api/v1/batches/runs/{runId}`
- `GET /api/v1/audit-logs`
- `GET /api/v1/approval-policies`
- `GET /api/v1/risk-limits`
- `GET /api/v1/ops-cases`

## Notes
- 계좌번호 마스킹 해제(`unmask=true`)는 `OPS_ADMIN`만 허용됩니다.
- 요청 승인 시 브로커 연계는 모의 어댑터로 동작하며 최대 3회 재시도합니다.
- 로그인 페이지는 기본 데모 계정(`opsadmin / admin123!`)이 미리 입력됩니다.
- AI 종목 추천은 운영 보조용 초안이며 실제 투자 자문이나 주문 자동화가 아닙니다.

## RSQL Filter
- 리스트 API는 `filter` 쿼리 파라미터로 RSQL 형식 필터를 받을 수 있습니다.
- 지원 연산자: `==`, `!=`, `=like=`, `=gt=`, `=ge=`, `=lt=`, `=le=`, `=in=`, `=out=`
- 예시:
  - `GET /api/v1/accounts?filter=status==ACTIVE;broker==CME`
  - `GET /api/v1/cash-requests?filter=status==PENDING;amount=ge=10000`
  - `GET /api/v1/batches/runs?filter=status==FAILED;retryCount=ge=1`
