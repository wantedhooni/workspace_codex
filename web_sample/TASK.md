# 작업 기록

## 2026-05-04

- Spring Boot + JPA + Querydsl + Gradle + PostgreSQL 프로젝트 신규 구성
- JWT 기반 회원 가입 / 로그인 기능 구현
- 게시글 기본 CRUD API 구현
- Querydsl 기반 게시글 검색 목록 구현
- 전역 예외 처리, 보안 설정, 공통 응답 모델 작성
- Docker Compose 및 전체 시작 / 중지 / 재시작 스크립트 작성
- README 사용법 문서 작성
- JWT 인증을 엔티티 비의존 모듈로 분리
- `JwtPrincipalLoader` 주입 구조로 USER / ADMIN 인증 주체 교체 가능하게 변경
- 별도 Admin 엔티티, 관리자 로그인 API, 관리자 principal 복원 어댑터 추가

## 2026-05-05

- 단일 프로젝트를 Gradle 멀티 모듈 구조로 재구성
- `modules:common` 공통 응답 / 예외 / 로그인 DTO 모듈 추가
- `modules:jwt-auth` JWT 인증 전용 모듈 분리
- `modules:user-server` USER 주체 서버 분리
- `modules:admin-server` ADMIN 주체 서버 분리
- USER / ADMIN PostgreSQL 컨테이너와 Flyway 마이그레이션 분리
- 전체 시작 / 중지 스크립트를 두 서버 동시 실행 방식으로 변경

## 2026-05-07

- USER / ADMIN 서버에 로그아웃 API 추가
- 액세스 토큰 블랙리스트와 리프레시 토큰 회전 발급 추가
- 토큰 상태 관리를 서버별 DB에서 `modules:jwt-auth` 공통 Redis 서비스로 이전
- Redis 컨테이너와 Spring Data Redis 설정 추가
- 기존 DB 토큰 테이블은 Flyway V3에서 제거하도록 정리
