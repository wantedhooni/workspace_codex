# Trading Macro Platform

증권사 매크로/알고리즘 투자용 백엔드/프론트엔드 기본 스캐폴딩입니다.

## 구조
- `backend`: Spring Boot (Java 21, JPA, JWT)
- `frontend`: React + react-admin

## 실행

### Backend
```bash
cd backend
gradle bootRun
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

### Docker Compose (db+backend+frontend)
```bash
./scripts/dev.sh
```

포트 충돌 시:
```bash
DB_PORT=5433 ./scripts/dev.sh
```

백엔드/프론트 포트도 변경 가능:
```bash
DB_PORT=5433 BACKEND_PORT=8081 FRONTEND_PORT=5174 ./scripts/dev.sh
```

`.env` 사용:
```bash
cp .env.example .env
./scripts/dev.sh
```

### Local (backend+frontend)
```bash
./scripts/dev-local.sh
```

## 기본 계정 생성
- 기본 시드 계정
  - `admin@tm.local` / `admin1234` (ADMIN)
  - `trader@tm.local` / `trader1234` (TRADER)

- 수동 생성: `POST /api/auth/register` (ADMIN 토큰 필요)
```json
{
  "email": "admin@tm.local",
  "password": "admin1234",
  "displayName": "Admin",
  "role": "ADMIN"
}
```

## API 리소스
- `strategies`, `macros`, `trades`, `portfolios`, `risk-policies`, `performance`, `performance-summaries`
- `teams`, `desks`, `books` (조직)
- `menus`, `menu-permissions` (메뉴/권한)

## 리스크 체크
- `trades` 생성/수정 시 포트폴리오 기반 리스크 룰을 검사합니다.
- 현재 적용: `maxPositionSize`, `maxLeverage` (간단 룰 엔진)

## 환경 변수
- `app.security.jwt.secret`: JWT 시크릿 (긴 랜덤 문자열 권장)
- DB 접속 정보는 `backend/src/main/resources/application.yml`에서 수정
