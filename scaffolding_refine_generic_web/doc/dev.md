# Backend 디자인 패턴 및 코드 분석

## 1. 코드베이스 구조 요약

- 대상 모듈: `backand/server/api-admin-server`
- 주요 패키지:
  - `com.portal.admin.api`: REST 컨트롤러
  - `com.portal.admin.service`: 도메인별 서비스
  - `com.portal.admin.service.base`: 공통 CRUD 추상화
  - `com.portal.admin.repo`: JPA + Querydsl 기반 저장소
  - `com.portal.admin.repo.search`: 검색 인터페이스 계약
  - `com.portal.admin.domain`: 핵심 엔티티
  - `com.portal.admin.menu.*`: 메뉴 도메인 분리 패키지
  - `com.portal.admin.auth`: 인증/요청 로그 필터
  - `com.portal.admin.config`: Web, OpenAPI, Querydsl 설정

현재 구조는 **Controller -> Service -> Repository -> Entity** 흐름으로 정리되어 있고, 컨트롤러에서 JPA 객체를 직접 다루지 않도록 리팩토링되어 있음.

---

## 2. 적용된 패턴/아키텍처 명칭

## 2.1 Layered Architecture (계층형 아키텍처, 공식 명칭)

- 역할 분리:
  - Controller: HTTP 입출력, 파라미터 바인딩
  - Service: 비즈니스 규칙/검증/감사 처리
  - Repository: 데이터 조회/검색 구현
  - Entity/DTO: 상태와 전송 모델 분리
- 효과:
  - 테스트 범위 분리가 쉬움
  - 변경 영향 범위를 계층 단위로 제한 가능

## 2.2 Template Method Pattern (GoF) + Abstract Base Class (구현 방식)

- 핵심 클래스:
  - `com.portal.admin.api.base.BaseCrudController`
  - `com.portal.admin.service.base.BaseCrudService`
- 방식:
  - 공통 CRUD 흐름은 베이스 클래스에서 제공
  - 도메인별 차이(엔티티 생성, 업데이트 반영, 응답 매핑)는 하위 서비스에서 오버라이드
- 장점:
  - 중복 코드 감소
  - CRUD 동작 규약(페이징, 감사 로깅, 예외 처리) 일관화
- 명칭 메모:
  - 공식 패턴 명칭은 `Template Method Pattern`(GoF).
  - `Abstract Base Class`/제네릭 사용은 패턴명이 아니라 구현 기법.

## 2.3 Repository Pattern + Query Object Pattern (Fowler P of EAA)

- 각 리포지토리는 `JpaRepository` + `SearchableRepository` + `FieldSearchableRepository` 조합 사용.
- 실제 검색은 `*RepositoryImpl`에서 Querydsl(`JPAQueryFactory`)로 구현.
- 예시:
  - `AdminUserRepositoryImpl.searchByFields(...)`
  - `BannerItemRepositoryImpl.searchByFields(...)`
- 특징:
  - 문자열 통합 검색 + 필드별 상세 검색을 동시에 지원
  - JPQL 문자열 의존도를 줄이고 타입 안정성 확보
- 명칭 메모:
  - 공식 명칭은 `Repository Pattern` + `Query Object Pattern`.
  - Querydsl은 해당 패턴을 타입 안전하게 구현하는 기술 요소.

## 2.4 Data Transfer Object (DTO) Pattern (공식 명칭)

- 도메인별 DTO를 `*Dtos` 파일에 `record`로 집약.
- 예시:
  - `AdminUserDtos`, `ContentDtos`, `MenuDtos`
- 장점:
  - API 모델 위치가 명확함
  - OpenAPI 스키마 주석(`@Schema`) 적용이 쉬움
- 명칭 메모:
  - `DTO Encapsulation Pattern`보다는 `DTO Pattern`이 표준적으로 통용됨.

## 2.5 Interceptor Pattern + Intercepting Filter/Filter Chain (공식 명칭)

- `AuthInterceptor`:
  - 쿠키 JWT를 검증하고 인증 컨텍스트를 request attribute에 적재.
- `RequestAccessLogFilter`:
  - 요청/응답 이후 접근 로그를 저장 (성공/실패 포함).
- 효과:
  - 인증과 로깅을 비즈니스 코드 밖에서 횡단 관심사로 분리.

## 2.6 Adjacency List Model + In-memory Tree Assembly (데이터 모델/구현 기법)

- 메뉴 엔티티(`menu_items`)는 `parentId` 기반 인접 리스트 모델.
- `MenuCrudService.tree(...)`에서 전체/검색 결과를 노드 맵으로 변환 후 트리로 조립.
- 장점:
  - DB 스키마 단순성 유지
  - UI용 트리 응답(`TreeMenuResponse`) 생성이 용이
- 명칭 메모:
  - 이는 GoF 디자인 패턴보다 `계층형 데이터 모델링/조립 기법`에 가까움.

## 2.7 Startup Data Seeding (CommandLineRunner) + Idempotent Initialization (운영 패턴)

- `SeedData`의 `CommandLineRunner`로 초기 데이터 생성.
- `find...orElseGet`/`exists...` 기반으로 중복 삽입을 회피.
- 효과:
  - 로컬/테스트 환경 재기동 시 데이터 일관성 유지.
- 명칭 메모:
  - `Bootstrapping`은 널리 쓰이는 용어지만 공식 GoF 패턴명은 아님.
  - Spring 맥락에서는 `Startup Runner(CommandLineRunner)` 기반 초기화로 표현하는 편이 정확함.

---

## 3. 요청 처리 흐름 (CRUD 기준)

1. Controller가 `searchParam`, 페이징, 필터 파라미터를 수신.
2. Service(`BaseCrudService`)가 검색/정렬/페이징/검증을 수행.
3. Repository(Querydsl impl)가 조건 기반 조회 실행.
4. Service가 엔티티를 DTO(record)로 변환해 반환.
5. 변경 작업(C/U/D)은 감사 로그(`ServiceAuditLog`)를 기록.

---

## 4. 코드 분석 결과

## 4.1 강점

- 컨트롤러 경량화: API 레이어가 서비스 위임 중심으로 정리됨.
- 검색 일관성: `searchParam` + 필드 필터 규약이 모든 도메인에 공통 적용.
- 확장성: 신규 도메인 추가 시 `XxxCrudService` + `XxxDtos` 패턴 복제만으로 빠르게 확장 가능.
- 운영성: 접근 로그와 서비스 감사 로그가 기본 내장되어 추적성이 높음.

## 4.2 트레이드오프 / 주의점 (보안 포함)

- 인증 비밀번호 검증이 데모 방식(`passwordHash`와 평문 비교)으로 구현됨:
  - 현재는 운영 환경 보안 기준에 맞지 않음.
  - 최소 `BCrypt` 해시 검증으로 교체 필요.
- 인증 쿠키가 `HttpOnly + SameSite=Lax`까지만 설정됨:
  - HTTPS 환경 기준으로 `Secure` 플래그가 필요하고, 정책에 따라 `SameSite=Strict` 검토가 필요함.
  - 토큰 무효화/회전 전략(블랙리스트 또는 버전 관리)도 부재함.
- CORS가 `allowedOriginPatterns("*") + allowCredentials(true)`로 설정됨:
  - 크리덴셜 쿠키를 쓰는 구조에서는 허용 Origin을 명시적으로 제한해야 안전함.
- `BaseCrudService`는 전체 목록 조회 후 애플리케이션 메모리에서 정렬/페이징 수행:
  - 데이터량 증가 시 메모리/응답시간 리스크가 큼.
  - DB 레벨 페이징/정렬(`Pageable`)로 전환 필요.
- 정렬 필드가 요청 파라미터 + 반사 기반으로 처리됨:
  - 오입력/오용 시 예측 불가 동작 가능성이 있어 도메인별 화이트리스트가 필요함.
- 검색 필터가 `Map<String, String>` 동적 구조:
  - 확장성은 좋지만 검증 계층이 약해 잘못된 필드명이 조용히 무시될 수 있음.
- 접근 로그 필터가 요청 경로/IP/사용자명을 그대로 저장:
  - 개인정보/민감 경로 마스킹 정책이 없으면 운영/규제 리스크가 발생할 수 있음.
- 감사 로그(`ServiceAuditLog`)가 CRUD 트랜잭션과 동기 저장됨:
  - 로그 저장 실패가 본 요청 처리에 영향을 줄 수 있는 구조임.

## 4.3 다음 개선 우선순위 제안

1. 인증 보강: 비밀번호 해시(`BCrypt`) + 쿠키 `Secure` + 토큰 무효화 전략 추가.
2. CORS 보강: 허용 Origin 화이트리스트로 제한하고 환경별 분리 설정.
3. 조회 성능 개선: `Pageable` 기반 DB 정렬/페이징으로 교체.
4. 입력 안정성 강화: 필터/정렬 파라미터 화이트리스트 및 유효성 오류 응답 명시.
5. 로그 보안 강화: IP/경로/사용자 정보 마스킹 정책과 보관 정책 정의.
6. 아키텍처 개선: 반사 기반 식별자 추출 제거(명시 인터페이스/매퍼 기반).
7. 유지보수성 개선: `SeedData`를 도메인별 Seeder 클래스로 분리.

---

## 5. 요약

현재 백엔드는 **Generic CRUD 템플릿 + Querydsl 검색 + JWT 인터셉터 + 로그 필터 + 메뉴 트리 조립**의 조합으로 운영형 관리자 포털 백엔드 패턴을 잘 따르고 있음.  
핵심 설계 방향은 일관성/확장성 중심이며, 다음 단계는 타입 안정성과 테스트 커버리지 강화가 가장 효과적임.
