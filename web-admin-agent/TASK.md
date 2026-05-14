# 작업 기록

## 2026-05-14
- `api-docs.json` OpenAPI 명세를 학습했다.
- 인증, 관리자, 계좌, 계좌 거래 API 그룹과 엔드포인트를 정리했다.
- `AGENTS.md`에 프로젝트 작업 지침을 한글로 보강했다.
- `PLANS.md`에 초기 개발 계획과 API 학습 결과를 작성했다.
- 프로젝트 실행 스크립트 `script/all-start.sh`, `script/all-stop.sh`, `script/all-restart.sh`를 추가했다.
- `README.md`를 현재 프로젝트 기준으로 업데이트했다.
- `bash -n`으로 실행 스크립트 문법 검사를 통과했다.
- `npm run lint`는 기존 `src/components/ui/carousel.tsx`, `src/hooks/use-mobile.ts`의 React hook 규칙 위반으로 실패했다.
- 로그인, 로그아웃, 대시보드, 좌측 메뉴, 도메인별 CRUD 화면, 도메인별 검색 필터, 공통 AG Grid 템플릿을 구현했다.
- JWT 세션 저장/복원과 ADMIN-API 공통 REST 서비스 클래스를 추가했다.
- 기존 lint 실패 지점인 carousel, use-mobile hook 구현을 React 19 규칙에 맞게 수정했다.
- `npm run lint` 검증을 통과했다.
- `npm run build` 검증을 통과했다.
- Playwright 브라우저로 로그인 화면, 세션 주입 후 대시보드, 관리자 CRUD 화면과 AG Grid 렌더링을 확인했다.
- 실제 개발 서버 포트 3333과 데모 계정 정보를 README 및 실행 스크립트에 반영했다.
