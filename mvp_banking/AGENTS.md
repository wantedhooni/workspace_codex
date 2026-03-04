# MVP Banking Platform

## 목적
- 이 저장소는 뱅킹 / 증권 서비스를 위한 `admin portal`과 `user web application`을 함께 구현한다.
- 목표는 내부 운영자 업무와 일반 사용자 서비스를 분리된 채널로 검증하는 것이다.
- 설계와 코드는 MVP여도 실무 환경에 바로 확장 가능한 구조를 유지한다.

## 제품 범위
- 대상 사용자
  - 운영 관리자
  - 심사 / 승인 담당자
  - 고객지원 운영자
  - 내부 감사 / 보안 담당자
  - 일반 사용자
- 1차 MVP 핵심 기능
  - Admin Portal
    - 관리자 인증 / 인가
    - 대시보드 및 운영 현황 조회
    - 고객 / 계좌 / 상품 기본 조회
    - 거래 / 주문 / 입출금 / 정산 내역 조회
    - 상태 변경 승인 플로우
    - 공지 / 배치 / 장애 대응용 운영 기능
    - 감사 로그 / 접속 로그 / 관리자 액션 이력
  - User Web Application
    - 사용자 회원가입 / 로그인
    - 내 계좌 / 잔고 / 보유상품 조회
    - 거래 / 주문 / 입출금 내역 조회
    - 기본 프로필 / 보안 설정
    - 공지 / 알림 확인

## 기술 스택
- Backend
  - Java 21
  - Spring Boot
  - Spring Data JPA
  - Querydsl
  - JSQL
  - JWT Access Token
  - Refresh Token
  - PostgreSQL
  - Redis
- Frontend
  - React
  - Refine for Admin Portal
  - React App for User Web Application

## 아키텍처 원칙
- 모듈은 업무 중심으로 나눈다. 공통 기술 계층보다 도메인 흐름이 우선이다.
- 기본 구조는 `presentation -> application -> domain -> infrastructure` 흐름을 따른다.
- JPA는 명령과 단순 조회 중심으로 사용한다.
- 복잡한 목록 조회, 검색, 집계는 Querydsl 또는 JSQL로 분리한다.
- 인증 / 인가 / 감사 / 공통 예외는 횡단 관심사로 공통화한다.
- `admin portal`과 `user web application`은 프론트엔드 앱과 API 경계를 분리한다.
- 화면은 admin에서는 Refine의 CRUD 생산성을 활용하되, 금융 운영 화면 특성상 검색 / 필터 / 상태 뱃지 / 승인 액션을 우선 설계한다.
- user web application은 일반 사용자 경험을 우선해 업무 중심 화면보다 단순하고 명확한 플로우를 유지한다.

## 권장 디렉터리 구조
```text
backend/
  src/main/java/com/{org}/{service}/
    common/
    auth/
    admin/
    user/
    customer/
    account/
    product/
    transaction/
    approval/
    audit/
frontend/
  admin-portal/
    src/
      app/
      pages/
      components/
      features/
      providers/
      routes/
  user-web-app/
    src/
      app/
      pages/
      components/
      features/
      providers/
      routes/
docs/
```

## 백엔드 설계 기준
- 패키지는 기능 단위로 분리한다.
- Controller는 요청 / 응답과 권한 체크 진입점만 담당한다.
- Application Service는 유스케이스 단위를 담당한다.
- Domain은 정책, 상태 전이, 검증 규칙을 가진다.
- Repository는 JPA Repository와 Query Repository를 역할별로 분리한다.
- API는 최소 `admin API`와 `user API`를 논리적으로 분리한다.
- 관리자 전용 기능은 `/api/admin/**`, 사용자 기능은 `/api/user/**` 또는 이에 준하는 경로 체계를 사용한다.
- 주 데이터 저장소는 PostgreSQL을 사용한다.
- Redis는 Refresh Token, 세션성 데이터, 캐시 저장소로 사용한다.
- Querydsl은 검색 조건이 많은 조회에 우선 사용한다.
- JSQL은 통계, 리포트, 복잡 조인, 성능 민감 조회에 제한적으로 사용한다.
- DTO와 Entity는 분리한다.
- 관리자 액션은 가능한 한 모두 Audit 이벤트로 남긴다.

## 인증 / 인가 기준
- 관리자와 사용자는 인증 진입점과 토큰 정책을 분리한다.
- 로그인 성공 시 Access Token + Refresh Token 구조를 사용한다.
- Refresh Token은 재사용 감지와 만료 정책을 고려한 저장소를 둔다.
- 권한은 최소 `SUPER_ADMIN`, `OPS_ADMIN`, `AUDITOR`, `REVIEWER`를 기본값으로 둔다.
- 사용자는 고객 등급, 계좌 상태, 본인 접근 범위를 정책으로 분리한다.
- 민감 기능은 Role 외에 Permission 또는 정책 기반 체크를 고려한다.
- 토큰 기반 인증이어도 관리자 세션 강제 만료 기능을 열어둔다.

## 데이터 / 보안 기준
- PostgreSQL은 시스템 오브 레코드로 사용하고, 거래 / 계좌 / 감사 데이터의 정합성을 우선한다.
- Redis는 캐시와 토큰 저장소로 사용하되, 원본 데이터 저장소로 취급하지 않는다.
- 고객 실명정보, 계좌번호, 거래식별자는 마스킹 규칙을 둔다.
- 관리자 비밀번호, 토큰, 비밀키는 코드에 하드코딩하지 않는다.
- 개인정보 접근, 상태 변경, 승인 / 반려는 감사 로그에 남긴다.
- 정렬 / 페이징 / 검색은 대용량 데이터를 전제로 설계한다.
- 금융 금액은 `BigDecimal`을 기본으로 사용한다.
- 시간값은 저장은 UTC, 표현은 정책에 따라 변환한다.

## 프론트엔드 설계 기준
- Admin Portal
  - Refine의 resource 기반 라우팅을 사용하되, 단순 CRUD 형태로만 끝내지 않는다.
  - 각 화면은 운영 실무 흐름 기준으로 설계한다.
  - 필수 화면 예시
    - 로그인
    - 대시보드
    - 고객 조회
    - 계좌 조회
    - 거래 / 주문 조회
    - 승인 대기함
    - 관리자 관리
    - 감사 로그
- User Web Application
  - 사용자 기준의 핵심 여정에 집중한다.
  - 필수 화면 예시
    - 회원가입 / 로그인
    - 내 자산 요약
    - 계좌 상세
    - 거래 / 주문 내역
    - 입출금 신청 / 결과 조회
    - 프로필 / 보안 설정
- 공통 UI 원칙
  - admin은 검색 조건이 화면 상단에서 바로 보이도록 구성한다.
  - admin 표는 고정 컬럼, 상태 컬러, 금액 포맷, 상세 패널을 고려한다.
  - 위험 액션은 확인 모달과 사유 입력을 기본 제공한다.
  - user는 핵심 정보와 다음 액션이 한 화면에서 명확해야 한다.

## 개발 원칙
- MVP라도 운영자 기준으로 설명 가능한 코드만 남긴다.
- user web application은 불필요한 복잡성을 피하고 본인 데이터 접근 경계를 명확히 한다.
- 추상화는 필요할 때만 한다. 다만 인증, 감사, 예외 처리, 응답 포맷은 일관성 있게 공통화한다.
- 테스트는 도메인 정책, 인증, 권한, 승인 흐름, 감사 로그 적재에 우선 투자한다.
- admin / user API의 권한 경계와 데이터 노출 경계는 테스트로 보장한다.
- 스키마와 API 계약은 문서와 함께 관리한다.

## 우선 구현 모듈
1. 인증 / 관리자 계정 / 사용자 계정 / 권한
2. 감사 로그 / 관리자 액션 추적
3. 고객 / 계좌 / 거래 조회
4. 사용자 자산 조회 / 거래 내역 조회
5. 승인 워크플로우 / 운영 대시보드

## 문서 운영 규칙
- 초기 방향성과 단계별 실행 계획은 `PLANS.md`에 정리한다.
- 실제 진행 체크와 완료 여부는 `task.md`에서 관리한다.
- 큰 방향이 바뀌면 `PLANS.md`를 갱신한다.

## 현재 가정
- 대상은 내부 운영용 admin portal과 외부 사용자용 user web application이다.
- 두 포털은 분리 배포 가능 구조를 우선한다.
- 멀티테넌시, 해외 법인 분리, 실시간 시세 연동은 후속 단계로 둔다.
