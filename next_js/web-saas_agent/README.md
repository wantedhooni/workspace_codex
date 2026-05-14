# SaaS Agent 랜딩 사이트

`api-saas-docs.json` 인증 명세를 기준으로 구현한 Next.js SaaS 랜딩 사이트입니다. 메인 페이지, 로그인, 로그아웃, 인증 API 프록시를 포함합니다.

## 주요 기능

- SaaS 제품 랜딩 메인 페이지
- 데모 계정 로그인 화면
- `/api/auth/login`, `/api/auth/logout`, `/api/auth/me`, `/api/auth/refresh` 프록시 라우트
- 액세스 토큰과 리프레시 토큰 HttpOnly 쿠키 저장
- 로그인 후 데모 대시보드 진입
- 프로젝트 실행/중지/재시작 스크립트 제공

## API 서버

기본 API 서버 주소는 OpenAPI 명세의 `http://localhost:8091`입니다.

필요하면 환경 변수로 변경할 수 있습니다.

```bash
SAAS_API_BASE_URL=http://localhost:8091
```

## 데모 계정

```text
id: demo@example.com
pw: Qwer1234!
```

## Getting Started

의존성을 설치합니다.

```bash
npm install
```

개발 서버를 실행합니다.

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

## 통합 스크립트

```bash
chmod +x scripts/all-start.sh scripts/all-stop.sh scripts/all-restart.sh
./scripts/all-start.sh
./scripts/all-stop.sh
./scripts/all-restart.sh
```

`all-start.sh`와 `all-restart.sh`는 실행 후 접속 URL과 데모 계정 정보를 출력합니다.

## 라우트

- `/`: SaaS 랜딩 메인 페이지
- `/login`: 로그인 페이지
- `/logout`: 로그아웃 처리 페이지
- `/dashboard`: 로그인 후 접근 가능한 데모 대시보드

## 검증

```bash
npm run lint
npm run build
```

## 개발 메모

브라우저는 백엔드 인증 API를 직접 호출하지 않습니다. 로그인 폼은 Next.js 내부 라우트인 `/api/auth/login`을 호출하고, 서버 라우트가 백엔드 `/api/v1/auth/login`과 통신한 뒤 토큰을 HttpOnly 쿠키로 저장합니다.

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.
