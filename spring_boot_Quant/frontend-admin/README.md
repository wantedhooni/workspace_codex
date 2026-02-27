# frontend-admin (A6)

React-Admin 1차 화면 (주문/체결/포지션/전표/원장)

## 실행

```bash
cd frontend-admin
npm install
VITE_APP_PROFILE=local npm run dev
```

기본 포트: `5175`

## 연결 백엔드

- 기본 API: `http://127.0.0.1:8088`
- 백엔드 MVP(`backend-mvp`)가 먼저 실행되어 있어야 목록이 조회됩니다.

## 로그인(로컬)

- `localhost` 또는 `127.0.0.1`로 접속 시 로그인 화면에 데모 관리자 계정이 자동 입력됩니다.
- 데모 계정
  - 아이디: `admin@quant.io`
  - 비밀번호: `demo1234`

## 구현 범위

- 리소스 목록 화면
  - `orders`
  - `trades`
  - `positions`
  - `journalVouchers`
  - `ledgerEntries`
- MVP DataProvider(create/list 중심)
