# Frontend Template Assets

이 디렉터리는 `scripts/scaffold-frontend-domain.sh`가 사용하는 프론트 템플릿 파일을 보관합니다.

## Placeholder 규칙
- `__DOMAIN_KEBAB__`: kebab-case 도메인명 (예: `transfer-limit`)
- `__DOMAIN_CAMEL__`: camelCase 도메인명 (예: `transferLimit`)
- `__DOMAIN_PASCAL__`: PascalCase 도메인명 (예: `TransferLimit`)
- `__DOMAIN_TITLE__`: 표시용 제목 (예: `Transfer Limit`)

## 구성
- `user-domain/`
  - `types.ts.tpl`
  - `api.ts.tpl`
  - `Page.tsx.tpl`
  - `index.ts.tpl`
- `admin-page/`
  - `Page.tsx.tpl`
