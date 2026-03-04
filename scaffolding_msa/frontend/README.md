# frontend

프론트엔드 워크스페이스다. `Next.js` 기반으로 `admin-portal`과 `web-application` 두 앱을 분리했다.

## 앱 구성
- `apps/admin-portal`: refine.dev 기반 운영자 포털
- `apps/web-application`: 대외용 웹 애플리케이션

## 개발 실행
```bash
npm install
npm run dev:admin
npm run dev:web
```

## 운영 기동
```bash
./scripts/all-start.sh
./scripts/all-stop.sh
```

`all-start`는 백엔드 전체 스택이 건강해진 뒤 `admin-portal`, `web-application`을 `next start`로 함께 기동한다.

## E2E 테스트
```bash
./scripts/e2e-test.sh
```

## 기본 포트
- `admin-portal`: `3001`
- `web-application`: `3002`
