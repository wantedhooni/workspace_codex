# Current Execution Plan

## 목표
- 뱅킹 / 증권 `admin portal`과 `user portal` MVP의 초기 문서와 구현 기준을 확정한다.

## 현재 단계
1. 프로젝트 운영 기준 문서 작성
2. admin / user 분리 구조와 MVP 범위 정의
3. 이후 백엔드 / 각 포털 초기 스캐폴딩 착수

## 이번 작업의 산출물
- `AGENTS.md`
- `PLANS.md`
- `plan.md`
- `task.md`

## 다음 구현 계획
1. 백엔드 Spring Boot 프로젝트 구조 생성
2. `admin-portal` React + Refine 프로젝트 구조 생성
3. `user-portal` React 프로젝트 구조 생성
4. 인증 / 인가 / 감사 로그 기반 모듈부터 구현

## 결정 사항
- 구조는 모듈형 모놀리스로 시작한다.
- 인증은 JWT + Refresh Token을 사용한다.
- 조회는 JPA + Querydsl + JSQL 역할 분리로 간다.
- 프론트엔드는 `admin portal`과 `user portal`을 별도 앱으로 분리한다.
- 백엔드는 `admin API`와 `user API`를 논리적으로 분리한다.
