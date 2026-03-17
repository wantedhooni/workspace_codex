# TASK

## 완료 작업
- `market-signal-platform` 모노레포 폴더 구조 생성
- Spring Boot 백엔드 인증 / 사용자 / 관심종목 / 리포트 / 뉴스 분석 API 구현
- 시장 레짐 엔진, 종목 점수 엔진, 아침 리포트 스케줄러 구현
- Next.js 프론트엔드 로그인 / 회원가입 / 대시보드 / 프로필 / 관심종목 / 리포트 / 뉴스 분석 화면 구현
- Docker Compose, Dockerfile, `.env.example`, 실행 스크립트 작성
- README, AGENTS, PLANS 문서 정리

## 진행 중 작업
- 실무형 품질 기준으로 UI / UX / 백엔드 기능 고도화 진행
- 백엔드에 최근 리포트 조회, 최근 뉴스 분석 조회, 관심 종목 검색 API 추가
- 프론트엔드에 모바일 내비게이션, 검색/필터, 최근 이력, 오류/빈 상태 UX 보강
- 관심 종목에 최신 시그널 커버리지와 리포트 근거 노출 기능 추가 완료
- 오늘의 리포트 Redis 캐시 직렬화 오류 수정 및 운영 로그 보강 완료
- 가짜 데모 시장 데이터 제거 및 실제 시장 스냅샷 JSON 시드 적재 구조로 전환 완료
- 리포트 응답에 데이터 기준일 / 생성 시각 노출, 뉴스 분석 이력 검색 / 감성 필터 기능 추가 완료
- 로그인 / 회원가입 / 프로필 화면을 온보딩 흐름 중심으로 재설계하고, 데모 계정 빠른 진입과 계정 상태 요약 UX를 추가 완료
- Quartz JDBC JobStore, Spring Batch JDBC 메타데이터, 배치 제어 REST API, 배치 운영 UI 추가 진행
- Quartz / Spring Batch를 JDBC 메타데이터 기반으로 운영 관리하도록 고도화 완료
- 배치 운영 상세 API에 JobExecution / StepExecution / Quartz Trigger JDBC 조회 추가 완료
- Spring Batch PostgreSQL 시퀀스 호환 문제(`batch_job_seq`) 수정으로 시작 배치 자동 실행 복구 완료
- 백엔드 도메인별 소스 경로를 `controller / service / entity / repository / api` 기준으로 재정렬 완료
- 프론트엔드 feature 경로를 `api / components / types / context` 기준으로 재정렬 완료
- README와 테스트 시나리오를 최신 기능 기준으로 재정리 완료

## 검증 결과
- `backend`: `mvn test` 통과
- `frontend`: `npm run build` 통과
- `docker`: `./scripts/all-start.sh` 실행 후 `http://localhost:18080/api/auth/login`, `http://localhost:18080/api/reports/today`, `http://localhost:18080/api/news/analyze`, `http://localhost:13000/login` 스모크 테스트 통과
- `backend`: 최근 리포트 조회 / 최근 뉴스 분석 조회 / 관심 종목 검색 API 테스트 추가 후 통과
- `frontend`: 모바일 내비게이션, 관심 종목 검색, 최근 리포트 / 최근 뉴스 분석 UI 포함한 프로덕션 빌드 통과
- `backend`: `GET /api/watchlists/coverage?query=nv`, `GET /api/reports/today` 런타임 스모크 테스트 통과
- `frontend`: `http://localhost:13000/watchlist` 페이지 응답 200 확인
- `backend`: 실제 시장 시드 적재 후 `macro_snapshots`, `sector_snapshots`, `stock_snapshots`, `watchlists` 테이블이 `2026-03-13` 기준 데이터로 교체된 것 확인
- `backend`: 재생성 후 `GET /api/reports/today` 응답이 `NEUTRAL / ENERGY / SOFTWARE / XOM / CVX` 기준으로 갱신된 것 확인
- `backend`: `mvn test` 재통과, `ReportServiceTest` 추가로 리포트 생성 시 실제 `snapshotDate` 기준으로 시그널 삭제 / 재적재되는 것 검증
- `frontend`: `npm run build` 재통과, 뉴스 분석 이력 검색 / 감성 필터와 리포트 데이터 기준일 / 생성 시각 UI 반영 확인
- `docker`: `./scripts/all-restart.sh` 재실행 후 기존 PostgreSQL 볼륨에 `snapshot_date` 스키마 보정이 적용되어 백엔드가 정상 기동하는 것 확인
- `runtime`: 로그인 후 `GET /api/reports/today` 에서 `snapshotDate`, `generatedAt` 응답 확인, `GET /api/news/analyses?query=nvidia&sentiment=POSITIVE` 200 확인, `http://localhost:13000/news/analyze` 200 확인
- `frontend`: AppShell, 카드/배지 스타일, 대시보드/관심종목/리포트/뉴스 화면의 정보 우선순위와 모바일 내비게이션 개선 반영
- `frontend`: 로그인 / 회원가입 / 프로필 화면의 온보딩, 데모 계정 진입, 계정 상태 요약 UI 반영 후 `npm run build` 통과
- `runtime`: `http://localhost:13000/login` 에서 데모 계정 자동 채우기와 로그인 후 `http://localhost:13000/dashboard`, `http://localhost:13000/profile` 새 UI가 반영된 것 확인
- `backend`: Spring Batch / Quartz JDBC 배치 제어 API 추가 후 `mvn test` 재통과 예정
- `frontend`: `/operations/batch` 운영 화면과 대시보드 배치 운영 요약 UI 추가 후 `npm run build` 통과
- `runtime`: PostgreSQL `batch_job_seq`, `batch_job_execution`, `batch_step_execution`, `qrtz_triggers` 기준으로 시작 시드 배치와 수동 리포트 배치가 모두 `COMPLETED` 기록되는 것 확인
- `runtime`: `GET /api/batch/jobs`, `GET /api/batch/jobs/{jobName}/metadata`, `POST /api/batch/jobs/daily-report-generate/run`, `http://localhost:13000/operations/batch` 응답 200 확인
- `structure`: 백엔드 도메인 디렉터리를 `controller / service / entity / repository / api` 기준으로 재정렬하고, 프론트엔드 feature 디렉터리를 `api / components / types / context` 기준으로 재정렬한 뒤 import 정리 완료
