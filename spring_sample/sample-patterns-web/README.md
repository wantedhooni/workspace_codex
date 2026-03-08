# sample-patterns-web

`sample_cqrs`, `sample_saga`, `transactional_outbox` 3개 서비스를 한 화면에서 조회하는 Next.js 기반 샘플 웹 앱이다. 서버 컴포넌트가 각 백엔드 API를 직접 호출해 샘플 데이터를 대시보드 형태로 보여준다.

## 기술 스택

- Next.js
- React
- TypeScript

## 표시 데이터

- CQRS 최근 주문 프로젝션
- Saga 주문 상태
- Saga 실행 상태
- Transactional Outbox 주문 및 이벤트 상태

## 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample-patterns-web
npm install
npm run dev
```

기본 포트는 `3000`이다.

## 환경 변수

- `CQRS_API_BASE_URL` 기본값: `http://localhost:8081`
- `SAGA_API_BASE_URL` 기본값: `http://localhost:8082`
- `OUTBOX_API_BASE_URL` 기본값: `http://localhost:8083`

## 함께 실행

루트 스크립트를 사용하면 백엔드 3개와 웹 앱을 한 번에 기동할 수 있다.

```bash
cd /Users/revy/workspace_codex/spring_sample
./scripts/patterns-all-start.sh
```
