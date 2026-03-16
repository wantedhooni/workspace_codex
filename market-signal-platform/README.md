# market-signal-platform

미국 주식 시장 데이터를 기반으로 시장 상태, 섹터 강도, 종목 시그널, 뉴스 분석을 제공하는 웹 기반 투자 분석 플랫폼입니다. 모노레포 안에서 Next.js 프론트엔드, Spring Boot 백엔드, PostgreSQL, Redis, Docker 실행 환경을 함께 관리합니다.

## 아키텍처 개요

### Monorepo 구조
```text
market-signal-platform
├─ backend
├─ frontend
├─ docker
├─ scripts
├─ AGENTS.md
├─ PLANS.md
├─ TASK.md
├─ docker-compose.yml
└─ README.md
```

### 구성 요소
- `frontend`: Next.js 16 + React 19 + TypeScript + Tailwind CSS 기반 웹 애플리케이션
- `backend`: Java 21 + Spring Boot 3.x 기반 API 서버
- `postgres`: 사용자, 관심종목, 스냅샷, 시그널, 리포트, 뉴스 분석 데이터 저장
- `redis`: 오늘의 리포트 캐시 저장
- `docker`: 프론트엔드/백엔드 이미지 빌드용 Dockerfile 보관

### 핵심 흐름
1. 사용자는 회원가입 또는 로그인으로 access token을 발급받습니다.
2. refresh token은 쿠키로 유지되고, 프론트엔드 API 클라이언트가 401 응답 시 자동 재발급을 시도합니다.
3. 백엔드는 최신 매크로/섹터/종목 스냅샷을 기반으로 시장 레짐과 시그널을 계산합니다.
4. 오늘의 리포트는 Redis 캐시에 저장되어 대시보드와 리포트 화면에서 재사용됩니다.
5. 뉴스 분석 API는 감성, 영향도, 요약, 해석을 생성해 저장하고 최근 분석 이력을 다시 조회할 수 있습니다.
6. 관심 종목과 리포트는 검색 / 최근 이력 중심으로 운영 화면에서 빠르게 탐색할 수 있게 구성되어 있습니다.
7. 관심 종목 화면은 최신 종목 스냅샷을 기반으로 현재 시그널 액션, 점수, 근거를 함께 보여주는 커버리지 뷰를 제공합니다.
8. 기본 시장 데이터는 코드 하드코딩이 아니라 실제 시장 스냅샷 JSON에서 적재되며, 현재 포함된 기준일은 `2026-03-13` 입니다.

## 기술 스택

### Backend
- Java 21
- Spring Boot 3.5
- Spring Security / JWT
- Spring Data JPA / Validation / Spring Batch / Quartz / Redis Cache
- PostgreSQL
- Swagger / OpenAPI
- JUnit 5 / Mockito / Testcontainers

### Frontend
- Next.js 16 App Router
- React 19
- TypeScript
- Tailwind CSS
- shadcn/ui 스타일 컴포넌트
- TanStack Query
- React Hook Form
- Zod

## 로컬 실행 방법

### 1. 환경 변수 준비
```bash
cp .env.example .env
```

### 2. Docker Compose로 전체 실행
```bash
./scripts/all-start.sh
```

중지:
```bash
./scripts/all-stop.sh
```

재시작:
```bash
./scripts/all-restart.sh
```

### 3. 수동 실행

백엔드:
```bash
cd backend
mvn spring-boot:run
```

프론트엔드:
```bash
cd frontend
npm install
npm run dev
```

## 접속 정보
- Frontend: [http://localhost:13000](http://localhost:13000)
- Backend API: [http://localhost:18080](http://localhost:18080)
- Swagger UI: [http://localhost:18080/swagger-ui.html](http://localhost:18080/swagger-ui.html)
- 데모 계정: `demo@marketsignal.dev / Demo1234!`
- 기본 시드 기준일: `2026-03-13`

## API 문서

### 인증
- `POST /api/auth/signup`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `POST /api/auth/refresh`

### 사용자
- `GET /api/users/me`
- `PATCH /api/users/me`

### 관심 종목
- `POST /api/watchlists`
- `GET /api/watchlists`
- `DELETE /api/watchlists/{id}`

검색:
- `GET /api/watchlists?query=nvda`
- `GET /api/watchlists/coverage?query=nvda`

### 리포트 / 시그널
- `POST /api/signals/generate`
- `GET /api/reports/today`
- `GET /api/reports/recent`

### 배치 운영
- `GET /api/batch/jobs`
- `GET /api/batch/jobs/{jobName}`
- `GET /api/batch/jobs/{jobName}/metadata`
- `POST /api/batch/jobs/{jobName}/run`
- `POST /api/batch/jobs/{jobName}/pause`
- `POST /api/batch/jobs/{jobName}/resume`

운영 화면:
- `/operations/batch`

응답 특징:
- 오늘의 리포트와 최근 리포트는 `topSignals[].reasons` 필드로 종목 선정 근거를 함께 제공합니다.
- 리포트 응답에는 `snapshotDate`, `generatedAt` 이 포함되어 데이터 기준일과 실제 생성 시각을 UI에서 직접 표시할 수 있습니다.
- 오늘의 리포트는 Redis 캐시를 사용하며 `LocalDate` 기반 응답도 안전하게 직렬화되도록 설정했습니다.
- 기존 PostgreSQL 볼륨에도 `snapshot_date` 컬럼이 안전하게 추가되도록 애플리케이션 시작 시 스키마 보정 러너를 실행합니다.
- Quartz 스케줄과 Spring Batch 메타데이터는 모두 JDBC 테이블로 관리되며, 운영 화면에서 스케줄 pause / resume 과 수동 실행을 직접 제어할 수 있습니다.
- 배치 상세 메타데이터는 `batch_job_execution`, `batch_step_execution`, `batch_job_execution_params`, `qrtz_triggers`, `qrtz_cron_triggers` 에서 직접 읽어 운영 화면에 표시합니다.
- Spring Batch PostgreSQL 시퀀스는 `batch_job_seq`, `batch_job_execution_seq`, `batch_step_execution_seq` 기준으로 초기화되며, 시작 시 시드 적재 배치가 자동 실행됩니다.

시드 데이터:
- 시장 시드 파일은 [real-market-seed.json](/Users/revy/workspace_codex/market-signal-platform/backend/src/main/resources/seed/real-market-seed.json) 에 저장됩니다.
- 적재 서비스는 [RealMarketSeedService.java](/Users/revy/workspace_codex/market-signal-platform/backend/src/main/java/com/example/marketsignal/batch/RealMarketSeedService.java) 에서 동작하고, 시작 시 [BatchStartupInitializer.java](/Users/revy/workspace_codex/market-signal-platform/backend/src/main/java/com/example/marketsignal/batch/BatchStartupInitializer.java) 가 Spring Batch 작업으로 실행합니다.
- `earningsReactionPositive`, `negativeNewsWeakPrice` 는 실제 가격/거래량 흐름을 반영한 보수적 프록시로 저장됩니다.

### 뉴스 분석
- `POST /api/news/analyze`
- `GET /api/news/analyses`

조회 예시:
- `GET /api/news/analyses?limit=8&query=nvidia`
- `GET /api/news/analyses?limit=8&sentiment=POSITIVE`
- `GET /api/news/analyses?limit=8&query=nvidia&sentiment=POSITIVE`

## 시장 엔진 규칙
- `10Y down AND DXY down` -> `GROWTH`
- `Oil surge` -> `ENERGY`
- `Futures drop strong` -> `RISK_OFF`
- 그 외 -> `NEUTRAL`

## 종목 시그널 규칙
- `above 20dma` +1
- `above 50dma` +1
- `relative strength` +2
- `earnings reaction positive` +2
- `volume surge` +1
- `negative news weak price` -2

분류:
- `score >= 5` -> `BUY`
- `score >= 3` -> `WATCH`
- 그 외 -> `AVOID`

## 테스트

백엔드 테스트 실행:
```bash
cd backend
mvn test
```

프론트엔드 프로덕션 빌드 검증:
```bash
cd frontend
npm install
npm run build
```

참고:
- `UserRepositoryTest` 는 Testcontainers 기반이며, Docker 데몬이 없는 환경에서는 자동으로 skip 됩니다.

## 주요 구현 포인트
- JWT access token + refresh token 쿠키 기반 인증
- 사용자별 관심 종목 관리 및 티커 / 종목명 검색
- 관심 종목별 최신 시그널 커버리지, 액션, 점수, 근거 노출
- 실제 시장 스냅샷 JSON과 기본 테스트 계정 자동 시드
- 시장 레짐 / 섹터 / 종목 시그널 기반 일간 리포트 생성, 최근 브리핑 이력 조회, 종목 선정 근거 노출
- 리포트 기준일 대비 실제 데이터 기준일과 생성 시각을 함께 보여주는 운영형 대시보드 / 리포트 UX
- 규칙 기반 뉴스 감성 및 영향도 분석, 최근 분석 이력 조회, 헤드라인 검색 / 감성 필터
- 공통 API 클라이언트의 자동 토큰 재발급 처리
- 모바일 대응 내비게이션과 대시보드 / 리포트 / 뉴스 분석 운영 UI 개선
- 로그인 / 회원가입 / 프로필 화면의 온보딩 흐름, 데모 계정 빠른 진입, 계정 상태 요약 UX 개선
- Quartz JDBC JobStore와 Spring Batch JDBC 메타데이터 기반 배치 운영 API / 운영 UI 제공
- Redis JSON 직렬화 안정화와 전역 예외 로그 보강
- Docker Compose와 일괄 실행 스크립트 제공
