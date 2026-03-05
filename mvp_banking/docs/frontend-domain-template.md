# Frontend Domain Template Guide

## 목적
- `user-web-app` / `admin-portal`에서 신규 도메인 파일 생성을 표준화한다.
- 반복적인 보일러플레이트 작성을 줄이고, 도메인 추가 시 구조 일관성을 유지한다.

## 스캐폴딩 스크립트

```bash
cd /Users/revy/workspace_codex/mvp_banking
./scripts/scaffold-frontend-domain.sh --domain transfer-limit --target both
```

옵션:
- `--domain`: 도메인명(kebab-case, 예: `transfer-limit`)
- `--target`: `user` | `admin` | `both` (기본: `both`)
- `--overwrite`: 기존 파일 덮어쓰기

## 생성 결과

`--target user`:
- `frontend/user-web-app/src/domains/<domain>/types.ts`
- `frontend/user-web-app/src/domains/<domain>/api.ts`
- `frontend/user-web-app/src/domains/<domain>/<Domain>Page.tsx`
- `frontend/user-web-app/src/domains/<domain>/index.ts`

`--target admin`:
- `frontend/admin-portal/src/pages/<Domain>Page.tsx`

## 수동 연동 체크리스트

1. `user-web-app` 연동
- `src/App.tsx`에 API import와 Page import를 추가한다.
- `loadDashboard` 또는 도메인 전용 로더에 신규 API 호출을 추가한다.
- 상태 타입(`DashboardState`)과 Route를 추가한다.

2. `admin-portal` 연동
- `src/api.ts`에 타입/엔드포인트 함수를 추가한다.
- `src/App.tsx`에 lazy import와 Route를 등록한다.
- 필요 시 운영 요약 카드(overview) 집계를 추가한다.

3. 도메인 계약 확정
- 템플릿의 `BASE_PATH`, 타입 필드(`code`, `name`, `status`)를 실제 백엔드 API 계약으로 교체한다.
- status 값이 `status-pill` CSS 규칙과 맞지 않으면 색상 매핑을 보강한다.

## 템플릿 위치
- `frontend/templates/user-domain/*.tpl`
- `frontend/templates/admin-page/Page.tsx.tpl`

## 권장 운영 방식
- 신규 도메인 추가 PR에서는 스캐폴딩 결과 + 실제 API 계약 반영을 한 커밋으로 묶는다.
- API 계약 변경 시 템플릿도 함께 갱신해 다음 도메인 작업에 반영되도록 유지한다.
