# PLAN

## TASK Checklist

- [x] 1. 프로젝트 구조 스캐폴딩 (`backend`, `frontend`, 공통 문서)
- [x] 2. 백엔드 빌드/설정 구성 (Spring Boot, JPA, Querydsl, JWT, PostgreSQL)
- [x] 3. 도메인 모델/리포지토리 구현 (`users`, `accounts`, `balances`, `positions`, `margins`, `cash_requests`, `fx_requests`, `batch_runs`, `audit_logs`)
- [x] 4. 인증/인가 구현 (`/api/v1/auth/login`, RBAC: `OPS_ADMIN|OPS_VIEWER|AUDITOR`)
- [x] 5. 계좌/요약 API 구현 (`GET /api/v1/accounts`, `GET /api/v1/accounts/{id}/summary`)
- [x] 6. 입출금/환전 요청 API 구현 (`POST /api/v1/cash-requests`, `POST /api/v1/fx-requests`)
- [x] 7. 승인/반려 워크플로우 구현 (`POST /api/v1/requests/{id}/approve|reject`, 낙관적 락, 재시도)
- [x] 8. 배치 모니터링 API 구현 (`GET /api/v1/batches/runs`, `GET /api/v1/batches/runs/{runId}`)
- [x] 9. 감사로그/보안로그 구현 (`GET /api/v1/audit-logs`, 주요 변경행위 추적, 인증/인가 실패 로그)
- [x] 10. 외부 연계 추상화/모의 어댑터 구현 (`ExchangeAdapter`, `BrokerAdapter`)
- [x] 11. 프론트엔드 Refine 앱 구현 (대시보드/리소스 화면/요청 액션)
- [x] 12. 로컬 실행/환경 구성 (`docker-compose`, 샘플 데이터, 실행 가이드)
- [x] 13. 검증 (빌드/테스트/기본 시나리오 점검)

## Progress Log
- 초기화: PLAN.md 생성
- 백엔드 1차 완료: 도메인/보안/JWT/Querydsl/API/감사로그/모의어댑터/시드데이터 구현
- 프론트 구현 완료: Refine 라우팅/대시보드/계좌/요청/배치/감사로그 화면 연결
- 환경 구성 완료: `docker-compose.yml`, `README.md`, `.gitignore` 추가
- 검증 완료: `backend mvn test` 통과, `frontend npm run build` 통과
- 추가 요청 반영: `./script/start_all.sh`, `./script/stop_all.sh` 생성
- 추가 요청 반영: 기본 데모 데이터(요청/승인/반려/실패 및 감사로그) 확장 시드 추가
- 추가 요청 반영: 로그인 페이지 데모 계정 사전 입력 + 안내 문구 표시
- 추가 요청 검증: `./script/start_all.sh` 기동 확인, `./script/stop_all.sh` 정상 종료 확인
- UI 정책 반영: 생성/수정은 모달 처리, 상세보기는 페이지 이동 처리 (cash/fx 요청 화면)
- 리스트 검색 반영: keyword search + filter search (accounts/cash/fx/batches/audit)
- 필터 포맷 반영: 리스트 `filter` 파라미터를 RSQL 형식으로 처리 (백엔드 파서/스펙 빌더 + 프론트 입력창 연동)
