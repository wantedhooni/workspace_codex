# 구현 계획

## 목표

Spring Boot, JPA, Querydsl, Gradle, PostgreSQL 기반의 기본 CRUD API와 JWT 로그인을 구현한다.

## 범위

- Gradle 기반 Spring Boot 프로젝트 구성
- PostgreSQL Docker Compose 구성
- Flyway 기반 DB 마이그레이션
- 회원 가입 / 로그인 API
- JWT 발급 및 인증 필터
- 게시글 기본 CRUD API
- Querydsl 기반 게시글 목록 검색
- 전역 예외 처리 및 표준 API 응답
- 실행 / 중지 / 재시작 스크립트
- README 문서화

## 설계 기준

- 인증은 stateless JWT Bearer 토큰을 사용한다.
- 비밀번호는 BCrypt로 단방향 해시한다.
- JPA 엔티티는 비즈니스 메서드로 상태를 변경한다.
- 목록 조회의 동적 조건은 Querydsl로 처리한다.
- API 경계에서는 DTO를 사용해 엔티티 직접 노출을 피한다.
- 운영 설정값은 환경 변수로 오버라이드 가능하게 둔다.

## 멀티 모듈 전환 계획

- `common`: 공통 API 응답, 예외, 요청 DTO를 제공한다.
- `jwt-auth`: JWT 발급, 검증, 인증 필터, 인증 주체 로더 계약을 제공한다.
- `user-server`: USER 엔티티를 인증 주체로 사용하고 게시글 CRUD를 제공한다.
- `admin-server`: ADMIN 엔티티를 인증 주체로 사용하고 관리자 API를 제공한다.
- 각 서버는 독립 DB와 독립 Flyway 마이그레이션을 사용한다.

## Redis 기반 JWT 세션 계획

- `jwt-auth` 모듈이 액세스 토큰 / 리프레시 토큰 발급을 모두 담당한다.
- 리프레시 토큰은 Redis에 SHA-256 해시 키로 저장하고 JWT 만료 시각과 같은 TTL을 둔다.
- 리프레시 요청은 기존 리프레시 토큰을 삭제하고 새 토큰 쌍을 발급하는 회전 방식을 사용한다.
- 로그아웃 시 액세스 토큰 `jti`를 Redis 블랙리스트에 저장하고 액세스 토큰 만료 시각과 같은 TTL을 둔다.
- USER / ADMIN 서버는 각각 `JwtPrincipalLoader`만 제공하고 토큰 상태 저장소를 직접 다루지 않는다.
