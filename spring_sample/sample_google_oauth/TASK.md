# 작업 기록

- `sample_google_oauth` 신규 프로젝트 생성
- Google OAuth 로그인용 Spring Security 백엔드 구현
- 로그인 상태 확인 및 로그아웃 가능한 React 프론트엔드 구현
- OAuth 최초 로그인 후 추가 정보를 입력받는 회원가입 흐름 구현
- OAuth 로그인 직후 기본 회원 정보를 RDBMS에 저장하고 마지막 로그인 시각을 갱신하도록 확장
- Redis 기반 OAuth 승인 요청 저장, refresh token 관리, JWT access token 인증 구조로 전환
- `.env.example`, `.env.local.example`, 실행 스크립트(`all-start.sh`, `all-stop.sh`, `all-restart.sh`) 작성
- `README.md` 및 루트 `README.md`에 프로젝트 설명과 검증 방법 반영
