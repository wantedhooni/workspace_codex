# Banking / Securities Platform MVP Plan

## 목표
- 내부 운영자용 `admin portal`과 일반 사용자용 `user web application`을 분리된 채널로 구축한다.
- 빠른 시연이 가능해야 하며, 이후 운영 기능과 사용자 기능을 독립적으로 확장할 수 있는 구조를 만든다.

## MVP 범위
### 공통
- 관리자 / 사용자 인증
- 관리자 권한 모델
- 감사 로그
- 공통 검색 / 페이징 / 정렬

### Admin Portal
- 고객 조회
- 계좌 조회
- 거래 / 주문 / 입출금 내역 조회
- 승인 대기 / 승인 / 반려
- 운영 대시보드

### User Web Application
- 회원가입 / 로그인
- 내 계좌 / 잔고 / 보유상품 조회
- 거래 / 주문 / 입출금 내역 조회
- 프로필 / 보안 설정
- 공지 / 알림 확인

### 운영 기능
- 관리자 계정 관리
- 장애 대응용 수동 상태 변경
- 공지 또는 운영 메모 기반 관리 기능

## 비범위
- 실시간 체결 엔진
- 정식 회계 / 원장 시스템
- 복잡한 파생상품 가격 산출 엔진
- 다국가 / 다법인 / 다통화 운영 최적화

## 백엔드 설계 방향
- Spring Boot 기반 모듈형 모놀리스로 시작한다.
- 하나의 백엔드 안에서 `admin API`와 `user API`를 명확히 분리한다.
- 주 데이터베이스는 PostgreSQL로 고정한다.
- Redis는 Refresh Token, 세션, 캐시 계층으로 추가한다.
- JPA는 트랜잭션 처리와 기본 엔티티 관리에 사용한다.
- Querydsl은 검색 조건이 많은 리스트 API에 사용한다.
- JSQL은 복잡 조회와 운영 리포트성 SQL에 사용한다.
- JWT + Refresh Token 기반 인증 구조를 사용한다.
- 관리자와 사용자는 인증 흐름, 권한 모델, 응답 DTO를 분리한다.
- Audit 이벤트는 비즈니스 로직과 분리된 공통 메커니즘으로 처리한다.

## 프론트엔드 설계 방향
- `frontend/admin-portal`과 `frontend/user-web-app`을 별도 앱으로 구성한다.
- Admin Portal은 React + Refine 기반으로 resource를 빠르게 구성한다.
- User Web Application은 React 기반 일반 서비스 앱으로 구성한다.
- admin은 운영 화면 중심의 검색성과 가시성을 우선한다.
- user는 계좌, 자산, 거래 확인과 요청 흐름의 단순성을 우선한다.

## 단계별 계획
### Phase 1. 프로젝트 기반 구성
- backend / admin-portal / user-web-app 기본 프로젝트 생성
- 공통 설정, 환경변수, 빌드, lint, formatting 정리
- PostgreSQL / Redis 연결 및 공통 응답 / 예외 포맷 수립

### Phase 2. 인증 / 권한 / 감사
- 관리자 로그인
- 사용자 로그인 / 회원가입
- JWT Access / Refresh Token 발급 및 재발급
- Role / Permission 모델
- 감사 로그 적재 및 조회 API

### Phase 3. 핵심 운영 조회와 사용자 조회
- 고객 조회
- 계좌 조회
- 거래 / 주문 / 입출금 조회
- 사용자 자산 요약 조회
- 사용자 내 거래 / 내 계좌 조회
- 검색 조건, 정렬, 페이징, 마스킹 정책 반영

### Phase 4. 승인 플로우
- 승인 요청 생성
- 승인 / 반려 처리
- 사유 기록
- 승인 이력 조회

### Phase 5. 포털 완성도 향상
- Admin KPI 카드
- 상태별 집계
- 관리자 액션 모니터링
- 사용자 프로필 / 보안 설정 완성
- 운영 편의 기능 추가

## 핵심 엔티티 초안
- AdminUser
- EndUser
- AdminRole
- AdminPermission
- RefreshToken
- LoginSession
- Customer
- Account
- Product
- Transaction
- Order
- ApprovalRequest
- ApprovalAction
- AuditLog

## API 우선순위
1. `/api/admin/auth/login`, `/api/admin/auth/refresh`, `/api/admin/auth/logout`
2. `/api/user/auth/signup`, `/api/user/auth/login`, `/api/user/auth/refresh`
3. `/api/admin/admins`, `/api/admin/admins/{id}/roles`
4. `/api/admin/audit-logs`
5. `/api/admin/customers`, `/api/admin/accounts`
6. `/api/admin/transactions`, `/api/admin/orders`
7. `/api/user/me`, `/api/user/accounts`, `/api/user/transactions`
8. `/api/admin/approvals`, `/api/admin/approvals/{id}/approve`, `/api/admin/approvals/{id}/reject`

## 성공 기준
- 관리자 로그인과 권한 기반 메뉴 노출이 동작한다.
- 사용자 로그인과 본인 데이터 기반 화면 진입이 동작한다.
- admin에서 고객 / 계좌 / 거래 데이터를 조회할 수 있다.
- user에서 본인 계좌 / 거래 데이터를 조회할 수 있다.
- 승인 액션이 가능하고 이력이 남는다.
- 주요 관리자 액션이 감사 로그에 적재된다.
- 두 포털이 분리된 UI와 API 경계로 동작한다.

## 리스크
- 금융 도메인 범위가 커서 초기 모델링이 과도해질 수 있다.
- 조회 성능 요구가 빨리 커질 수 있다.
- 권한 정책과 감사 요건이 뒤늦게 추가되면 구조 수정 폭이 커질 수 있다.
- admin / user 경계가 모호하면 API와 화면 책임이 섞일 수 있다.

## 운영 원칙
- 먼저 admin과 user의 인증 경계를 명확히 만든다.
- PostgreSQL 정합성과 Redis 만료 정책을 초기부터 분리해 설계한다.
- 읽기 기능을 안정화한 뒤 쓰기 / 승인 기능을 확장한다.
- 모든 민감 액션은 추적 가능해야 한다.
