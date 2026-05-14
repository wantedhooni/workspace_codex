# web-sample

Spring Boot, JPA, Querydsl, Gradle, PostgreSQL 기반의 멀티 모듈 JWT 인증 샘플 API입니다.

## 기술 스택

- Java 21
- Spring Boot 3.3
- Spring Web / Security / Validation / Actuator
- Spring Data JPA
- Querydsl 5
- PostgreSQL 16
- Redis 7
- Flyway
- Gradle

## 실행

```bash
chmod +x scripts/*.sh
scripts/all-start.sh
```

스크립트는 `./gradlew`가 있으면 Gradle Wrapper를 우선 사용하고, 없으면 로컬 `gradle` 명령을 사용합니다.

실행 후 접속 정보:

- USER 서버 API: `http://localhost:8080`
- USER 서버 Health: `http://localhost:8080/actuator/health`
- ADMIN 서버 API: `http://localhost:8081`
- ADMIN 서버 Health: `http://localhost:8081/actuator/health`
- Redis: `redis://localhost:6379`
- USER 데모 계정: `user@example.com` / `User1234!`
- ADMIN 데모 계정: `admin@example.com` / `Admin1234!`

중지:

```bash
scripts/all-stop.sh
```

재시작:

```bash
scripts/all-restart.sh
```

## 주요 API

### 회원 가입

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","password":"Password123!","name":"홍길동"}'
```

### 사용자 로그인

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","password":"User1234!"}'
```

응답의 `data.accessToken` 값을 이후 요청의 `Authorization: Bearer <token>` 헤더에 사용합니다.
응답의 `data.refreshToken` 값은 재발급과 로그아웃에 사용합니다.

### 사용자 토큰 재발급

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken":"<refresh-token>"}'
```

### 사용자 로그아웃

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <access-token>' \
  -d '{"refreshToken":"<refresh-token>"}'
```

### 관리자 로그인

```bash
curl -X POST http://localhost:8081/api/admin/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"Admin1234!"}'
```

관리자 토큰은 `principalType=ADMIN`으로 발급되며, ADMIN 서버의 `/api/admin/**` API에서 사용합니다.

### 관리자 토큰 재발급

```bash
curl -X POST http://localhost:8081/api/admin/auth/refresh \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken":"<admin-refresh-token>"}'
```

### 관리자 로그아웃

```bash
curl -X POST http://localhost:8081/api/admin/auth/logout \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <admin-access-token>' \
  -d '{"refreshToken":"<admin-refresh-token>"}'
```

### 관리자 인증 확인

```bash
curl http://localhost:8081/api/admin/me \
  -H 'Authorization: Bearer <admin-token>'
```

### 게시글 목록

```bash
curl 'http://localhost:8080/api/posts?keyword=게시글&page=0&size=20'
```

### 게시글 생성

```bash
curl -X POST http://localhost:8080/api/posts \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <token>' \
  -d '{"title":"제목","content":"내용"}'
```

### 게시글 수정

```bash
curl -X PUT http://localhost:8080/api/posts/1 \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <token>' \
  -d '{"title":"수정 제목","content":"수정 내용"}'
```

### 게시글 삭제

```bash
curl -X DELETE http://localhost:8080/api/posts/1 \
  -H 'Authorization: Bearer <token>'
```

## 환경 변수

- `USER_DB_URL`: USER 서버 PostgreSQL JDBC URL. 기본값은 `jdbc:postgresql://localhost:55432/user_sample`입니다.
- `USER_DB_USERNAME`: USER 서버 DB 사용자
- `USER_DB_PASSWORD`: USER 서버 DB 비밀번호
- `USER_SERVER_PORT`: USER 서버 포트
- `ADMIN_DB_URL`: ADMIN 서버 PostgreSQL JDBC URL
- `ADMIN_DB_USERNAME`: ADMIN 서버 DB 사용자
- `ADMIN_DB_PASSWORD`: ADMIN 서버 DB 비밀번호
- `ADMIN_SERVER_PORT`: ADMIN 서버 포트
- `REDIS_HOST`: JWT 세션 Redis 호스트
- `REDIS_PORT`: JWT 세션 Redis 포트
- `JWT_ISSUER`: JWT issuer
- `JWT_SECRET`: JWT 서명 키. 운영에서는 충분히 긴 난수 문자열을 사용해야 합니다.
- `JWT_ACCESS_TOKEN_EXPIRATION_MINUTES`: 액세스 토큰 만료 시간
- `JWT_REFRESH_TOKEN_EXPIRATION_DAYS`: 리프레시 토큰 만료 일수

## 구조

```text
modules
├── common
│   └── 공통 응답, 예외, 로그인 DTO
├── jwt-auth
│   └── JWT 발급, 파싱, Redis 세션, 인증 필터, principal loader 계약
├── user-server
│   └── USER 주체 로그인, 게시글 CRUD, Querydsl 검색
└── admin-server
    └── ADMIN 주체 로그인, 관리자 API
```

## JWT 인증 모듈 구조

JWT 모듈은 특정 엔티티를 직접 참조하지 않습니다.

- `modules:jwt-auth`: 특정 엔티티를 모르는 JWT 인증 모듈입니다.
- `JwtTokenProvider`: `JwtPrincipal` 계약으로 토큰을 발급하고 파싱합니다.
- `JwtSessionService`: Redis로 리프레시 토큰, 토큰 회전, 로그아웃 블랙리스트를 공통 관리합니다.
- `JwtAuthenticationFilter`: 토큰의 `principalType`과 `sub`를 읽어 인증 주체를 복원합니다.
- `JwtPrincipalLoader`: 서버별 엔티티를 인증 주체로 바꾸는 주입 지점입니다.
- `user-server`의 `UserJwtPrincipalLoader`: `principalType=USER` 토큰을 `User` 엔티티로 복원합니다.
- `admin-server`의 `AdminJwtPrincipalLoader`: `principalType=ADMIN` 토큰을 `Admin` 엔티티로 복원합니다.

새 인증 주체 서버를 추가하려면 새 Spring Boot 모듈에서 해당 엔티티와 `JwtPrincipalLoader` 구현체를 등록하고, 토큰 발급 시 같은 `principalType`을 넣으면 됩니다.

## 직접 실행

```bash
./gradlew :modules:user-server:bootRun
./gradlew :modules:admin-server:bootRun
```
