# 작업 계획

## 목표
- `api-saas-docs.json`의 인증 API 명세를 기준으로 SaaS 랜딩 사이트, 로그인, 로그아웃, 인증 프록시를 구현한다.
- 액세스 토큰과 리프레시 토큰은 브라우저 JavaScript에서 직접 다루지 않고 Next.js Route Handler에서 HttpOnly 쿠키로 관리한다.

## 구현 범위
- 메인 랜딩 페이지: 제품 소개, 주요 가치, 데모 계정 안내, 로그인 진입점
- 로그인 페이지: 데모 계정 기본값, API 로그인 호출, 오류/로딩 상태
- 로그아웃 페이지 및 버튼: 서버 로그아웃 호출 후 쿠키 정리
- 인증 API 프록시: `/api/auth/login`, `/api/auth/logout`, `/api/auth/me`, `/api/auth/refresh`
- 실행 스크립트: `scripts/all-start.sh`, `scripts/all-stop.sh`, `scripts/all-restart.sh`

## 검증 계획
- 의존성 설치 후 lint/build 실행
- 개발 서버 구동 후 랜딩, 로그인 화면, 로그아웃 흐름을 브라우저로 확인
