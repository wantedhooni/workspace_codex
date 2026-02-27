# AGENTS.md

> 이 문서는 AI 코딩 에이전트(예: ChatGPT, 코드 생성 에이전트)가 이 저장소에서 일관된 방식으로 작업하기 위한 운영 가이드다.  
> 기본 스택: Spring Boot + JPA + Querydsl / Next.js + React

---

## 1) 목표와 우선순위

에이전트는 아래 우선순위를 지켜서 작업한다.

1. 정확성: 요구사항과 기존 코드 맥락을 우선 반영
2. 안전성: 데이터 무결성/보안/회귀 위험 최소화
3. 가독성: 팀 규칙과 일관된 네이밍/구조
4. 테스트 가능성: 변경사항마다 검증 코드/절차 포함
5. 작은 단위 변경: 한 번에 큰 리팩터링 금지, 단계적 변경

---

## 2) 기술 스택 기준

| 영역 | 기술 | 원칙 |
|---|---|---|
| Backend | Spring Boot, JPA, Querydsl | 트랜잭션 경계 명확화, N+1 방지, 명시적 조회 전략 |
| Frontend | Next.js, React | 서버/클라이언트 경계 명확화, 상태 최소화, 재사용 가능한 컴포넌트 |
| DB | MariaDB | MariaDB 문법/인덱스/실행계획 기준으로 쿼리 검토 |
| DB 접근 | JPA + Querydsl | 단순 CRUD는 JPA 기본 메서드, 커스텀/동적 조회는 Querydsl |
| API | REST(JSON) | 에러 포맷 일관성, DTO 기반 I/O, 엔티티 직접 노출 금지 |

---

## 3) 디렉터리/레이어 규칙

### 3.1 Backend 권장 구조
- `domain`: 엔티티, 값 객체, 도메인 규칙
- `application`: 유스케이스(Service), 트랜잭션 경계
- `infrastructure`: Repository 구현, 외부 연동
- `presentation`: Controller, Request/Response DTO

### 3.2 Frontend 권장 구조 (Next.js App Router)
- `app/`: 라우팅, 페이지, 레이아웃
- `features/`: 도메인 기능 단위(예: users, orders)
- `entities/`: 공통 도메인 모델/타입
- `shared/`: UI 컴포넌트, 유틸, API 클라이언트

> 기존 프로젝트 구조가 있으면 기존 구조를 우선 따른다. 구조 개편은 명시적 요청 시에만 수행한다.

---

## 4) 백엔드 구현 규칙 (Spring Boot/JPA/Querydsl)

### 4.1 엔티티
- `@Setter` 지양, 생성자/의도 메서드로 상태 변경
- 연관관계는 기본 `LAZY`
- 컬렉션 필드는 초기화 (`new ArrayList<>()`)
- equals/hashCode는 식별자 전략 충돌 없이 신중히 사용

### 4.2 서비스/트랜잭션
- 읽기 전용 조회: `@Transactional(readOnly = true)`
- 상태 변경: 서비스 계층에서만 수행
- Controller에서 비즈니스 로직 금지

### 4.3 Repository/JPA/Querydsl 핵심 규칙
- 단순 CRUD: Spring Data JPA 기본 메서드 사용 (`save`, `findById`, `delete` 등)
- 커스텀 조회/검색/통계/동적 조건: 반드시 Querydsl 사용
- JPQL 문자열 사용 금지 (`EntityManager.createQuery`, `@NamedQuery` 포함)
- `@Query` 사용 금지 (JPQL/nativeQuery 모두 금지)
- 페이징 시 count 쿼리 최적화 고려
- fetch join 남용 금지 (카디널리티/중복 주의)
- N+1 의심 구간은 해결 전략을 코드/리뷰 코멘트로 명시

### 4.4 Repository 네이밍 규약 (필수)
- 기본 Repository: `XxxRepository`
  - `JpaRepository<Xxx, ID>` 상속
  - 기본 CRUD 및 단순 파생 메서드만 포함
- Querydsl 인터페이스: `XxxQueryRepository`
  - 커스텀 조회 시그니처 정의
- Querydsl 구현체: `XxxQueryRepositoryImpl`
  - `XxxQueryRepository` 구현
  - `JPAQueryFactory` 기반 구현
- 조합 원칙:
  - `XxxRepository extends JpaRepository<Xxx, ID>, XxxQueryRepository`
- 금지:
  - `XxxRepositoryImpl` 네이밍으로 임의 구현체 생성 금지
  - 서비스 계층에서 `JPAQueryFactory` 직접 사용 금지

---

## 5) Querydsl 페이징 표준 유틸 규칙

### 5.1 공통 원칙
- 목록 조회는 `Pageable` 입력을 기본으로 받는다.
- 정렬은 화이트리스트 매핑 방식만 허용한다.
- 동적 조건은 공통 Predicate 빌더로 조합한다.
- count 쿼리는 필요 시 content 쿼리와 분리 최적화한다.

### 5.2 표준 유틸 구성
- `QuerydslSortMapper`
  - `Pageable.getSort()` → `OrderSpecifier<?>[]` 변환
  - 허용 필드만 매핑 (예: `"createdAt" -> qUser.createdAt`)
  - 미허용 필드는 예외 또는 기본 정렬 대체
- `QuerydslPredicateBuilder`
  - nullable 조건을 일관되게 조합
  - 권장 메서드 예:
    - `eqIfPresent`
    - `likeIfPresent`
    - `betweenIfPresent`
    - `inIfNotEmpty`

### 5.3 표준 적용 절차
1. 요청 DTO를 검색조건 객체로 변환
2. PredicateBuilder로 where 조건 생성
3. SortMapper로 정렬 생성
4. content 쿼리 실행 (`offset`, `limit`, `orderBy`)
5. count 쿼리 실행 (필요 시 최적화)
6. `PageImpl<>(content, pageable, total)` 반환

### 5.4 금지 패턴
- 클라이언트 정렬 필드를 경로 검증 없이 직접 사용
- 문자열 기반 컬럼명 직접 주입
- null 조건 조합 로직을 Repository마다 중복 작성

---

## 6) DTO/매핑 규칙 (Static Mapper 필수)

### 6.1 기본 원칙
- DTO ↔ Entity 매핑은 Static Mapper 클래스 사용
- 매퍼 클래스는 인스턴스화 금지
- private 생성자에서 아래 예외를 던진다.
  - `UnsupportedOperationException("This class should never be instantiated")`
- 매핑 메서드는 static으로 선언
- 메서드명은 명확하게 작성
  - 예: `toDto`, `toEntity`, `toSummaryDto`, `toDetailDto`
- null 입력은 방어적으로 처리 (기본: null 반환)
- 매퍼는 순수 변환만 담당 (사이드이펙트 금지)

### 6.2 네이밍/위치
- 클래스명: `{도메인명}Mapper` (예: `UserMapper`, `OrderMapper`)
- 컬렉션 변환 메서드(선택): `toDtoList`, `toEntityList`
- 패키지: `application.mapper` 또는 `presentation.mapper` 중 하나로 일관화

### 6.3 금지 사항
- 매퍼를 Bean(`@Component`)으로 등록 금지
- 매퍼 내부 Repository/Service 호출 금지
- 매퍼 내부 트랜잭션/영속성 제어 금지
- 매퍼 내부에서 입력 객체 mutate 금지

### 6.4 Java 예시 코드

    public final class UserMapper {
    
        private UserMapper() {
            throw new UnsupportedOperationException("This class should never be instantiated");
        }
    
        public static UserDTO toDto(final User user) {
            if (user == null) {
                return null;
            }
            return UserDTO.builder()
                    .withId(user.getId())
                    .withEmailAddress(user.getEmail())
                    .build();
        }
    
        public static User toEntity(final UserDTO userDto) {
            if (userDto == null) {
                return null;
            }
            return User.builder()
                    .withId(userDto.getId())
                    .withEmail(userDto.getEmailAddress())
                    .build();
        }
    }

---

## 7) Audit User 정책 (필수)

### 7.1 목적
- 생성자/수정자/생성시각/수정시각을 일관되게 기록한다.
- 모든 변경 이력은 애플리케이션 인증 주체 기준으로 추적 가능해야 한다.

### 7.2 공통 규칙
- JPA Auditing 활성화: `@EnableJpaAuditing(auditorAwareRef = "auditorAware")`
- 공통 베이스 엔티티 사용:
  - `createdAt` (`@CreatedDate`)
  - `updatedAt` (`@LastModifiedDate`)
  - `createdBy` (`@CreatedBy`)
  - `updatedBy` (`@LastModifiedBy`)
- 시간 타입은 `Instant` 또는 `LocalDateTime(UTC 고정)` 중 하나로 통일
- DB 컬럼은 nullable 정책 명시:
  - `createdAt`, `updatedAt`: NOT NULL
  - `createdBy`, `updatedBy`: 시스템 정책에 따라 NOT NULL 권장

### 7.3 Auditor 식별 정책
- `AuditorAware<String>` 기본 반환값:
  1. 로그인 사용자 ID(예: userId, email, accountId)
  2. 시스템 배치/스케줄러 실행 시 `"system"`
  3. 식별 불가 시 `"anonymous"` (운영 정책으로 허용 시)
- `createdBy`/`updatedBy`에는 표시명(name) 금지, 변경 가능성 낮은 고유 식별자 사용
- 사용자 탈퇴/변경과 무관하게 감사 로그 정합성이 유지되어야 함

### 7.4 보안/신뢰 규칙
- 클라이언트 입력으로 `createdBy`/`updatedBy`를 받지 않는다.
- Request DTO에 audit 필드 노출 금지
- API 응답에 audit 필드 노출 여부는 도메인별 정책으로 통제(기본 비노출)
- 테스트에서 임의 auditor 주입 가능하도록 test config 제공

### 7.5 구현 표준
- `BaseAuditEntity` (시간) + `BaseAuditUserEntity` (사용자) 분리 가능
- 엔티티는 직접 값 세팅 금지, Auditing으로만 채움
- 수동 SQL/배치 업데이트 시 `updatedAt`/`updatedBy` 누락 금지

### 7.6 점검 항목
- 신규 엔티티 생성 시 audit 베이스 상속 여부 확인
- DB 스키마에 audit 컬럼 및 인덱스(필요 시) 확인
- 통합 테스트에서 생성/수정 시 audit 값 자동 반영 검증

---

## 8) 예외/에러 응답 규칙
- 전역 예외 처리: `@RestControllerAdvice` 사용
- 비즈니스 예외/시스템 예외 분리
- 에러 응답 포맷 일관성 유지
  - `timestamp`
  - `code`
  - `message`
  - `details` (선택)

---

## 9) 로깅 규칙
- INFO: 주요 비즈니스 이벤트
- WARN/ERROR: 예외 상황
- 민감정보(토큰/비밀번호/개인정보) 로그 금지

---

## 10) MariaDB 규칙

### 10.1 쿼리/스키마
- 문자셋/콜레이션: `utf8mb4` 계열 사용
- PK/FK/검색 조건 컬럼 인덱스 점검
- 페이징/정렬 쿼리는 `EXPLAIN`으로 실행계획 확인
- 대량 IN/정렬/집계 시 임시테이블/filesort 유발 여부 점검
- 시간 저장 정책(UTC 권장) 일관성 유지

### 10.2 로컬 개발 DB (Docker Compose 고정)
- 로컬 개발 DB는 MariaDB로 고정
- 로컬 설치 DB 직접 사용 금지 (환경 편차 방지)
- `docker compose`로 기동/종료/초기화 표준화

### 10.3 docker-compose 표준 예시

    version: "3.9"
    
    services:
      mariadb:
        image: mariadb:11.4
        container_name: app-mariadb
        restart: unless-stopped
        environment:
          MARIADB_ROOT_PASSWORD: root
          MARIADB_DATABASE: app
          MARIADB_USER: app
          MARIADB_PASSWORD: app
          TZ: Asia/Seoul
        ports:
          - "3306:3306"
        command:
          - --character-set-server=utf8mb4
          - --collation-server=utf8mb4_unicode_ci
        volumes:
          - mariadb_data:/var/lib/mysql
          # - ./docker/mariadb/init:/docker-entrypoint-initdb.d
    
    volumes:
      mariadb_data:

### 10.4 Spring local 프로필 권장값
- `spring.datasource.url=jdbc:mariadb://localhost:3306/app`
- `spring.datasource.username=app`
- `spring.datasource.password=app`
- `spring.jpa.database-platform=org.hibernate.dialect.MariaDBDialect`

---

## 11) 프론트엔드 구현 규칙 (Next.js/React)

### 11.1 컴포넌트
- 프레젠테이셔널/컨테이너 역할 분리
- 단일 책임 유지
- Props 타입 명확화 (TypeScript 우선)

### 11.2 상태 관리
- 로컬 상태 우선
- 전역 상태 최소화
- 서버 상태와 UI 상태 분리

### 11.3 데이터 패칭
- 서버/클라이언트 컴포넌트 경계 명확화
- API 클라이언트 계층 분리 (`shared/api`)
- 로딩/에러/빈 상태 UI 필수 처리

### 11.4 폼/검증 및 접근성
- 검증 스키마 기반 처리
- 서버 에러/필드 에러 구분
- 시맨틱 태그, 키보드 접근성, 포커스 흐름 준수

---

## 12) 코딩 컨벤션

### 12.1 공통
- 의미 있는 이름 사용(축약어 최소화)
- 매직 넘버/문자열 상수화
- 주석은 무엇보다 왜를 설명
- 한 PR에 과도한 관심사 혼합 금지

### 12.2 Java
- null 반환보다 Optional/빈 컬렉션 우선 (상황별 선택)
- Stream 남용 금지 (가독성 우선)
- Lombok 사용 의도 명확화

### 12.3 TypeScript/React
- `any` 금지 (불가피 시 사유 명시)
- 유틸 함수 순수성 유지
- 비동기 에러 처리 누락 금지

---

## 13) API 규약

| 항목 | 규칙 |
|---|---|
| URL | 복수형 리소스 네이밍 (`/api/users`) |
| 메서드 | GET/POST/PUT/PATCH/DELETE 의미 준수 |
| 상태코드 | 2xx/4xx/5xx 의미 준수 |
| 응답 | 성공/실패 포맷 일관성 |
| 버전 | 필요 시 `/api/v1` |

---

## 14) 보안 체크리스트
- [ ] 인증/인가 없는 민감 API 노출 여부
- [ ] 입력값 검증(길이/형식/범위)
- [ ] SQL Injection/XSS/CSRF 기본 방어
- [ ] 민감정보 마스킹 및 로그 제외
- [ ] CORS 정책 점검
- [ ] 파일 업로드 타입/사이즈 제한

---

## 15) 테스트 규칙

### 15.1 Backend
- 단위 테스트: 서비스/도메인 규칙
- 통합 테스트: Repository/Controller 핵심 시나리오
- Querydsl 쿼리: 조건/정렬/페이징 검증
- MariaDB 호환성: 방언/함수/정렬/인덱스 검증
- Audit: 생성/수정 시각 및 사용자 자동 주입 검증

### 15.2 Frontend
- 컴포넌트 렌더링/이벤트 테스트
- 핵심 사용자 플로우 우선 검증
- API 실패/빈 데이터/로딩 상태 검증

### 15.3 최소 검증 기준
- [ ] 정상 시나리오 1개 이상
- [ ] 실패 시나리오 1개 이상
- [ ] 회귀 위험 포인트 확인

---

## 16) 성능 가이드

### 16.1 Backend
- 불필요한 연관 로딩 금지
- 인덱스 필요한 조회 조건 점검
- 대량 조회 페이징/배치 처리
- Querydsl 생성 쿼리 실행계획 확인

### 16.2 Frontend
- 불필요한 리렌더 최소화
- 큰 리스트 가상화 고려
- 번들 크기 증가 원인 점검

---

## 17) 에이전트 작업 절차
1. 요구사항 해석
2. 영향 범위 파악
3. 구현 (작은 단위)
4. 검증 (테스트/빌드/린트)
5. 결과 보고 (요약/리스크)

---

## 18) PR 메시지 템플릿

아래 형식을 그대로 사용한다.

### 변경 목적
- 

### 주요 변경점
- 
- 

### 고려사항
- 

### 테스트/검증
- [ ] 단위 테스트
- [ ] 통합 테스트
- [ ] 수동 검증
- 실행 결과:
  - 

---

## 19) 금지 사항
- 엔티티 API 응답 직접 노출
- 검증/예외처리 없는 컨트롤러 로직 추가
- 근거 없는 대규모 리팩터링
- 실패 케이스 테스트 누락
- 민감정보 하드코딩/로그 출력
- 기존 규약 무시한 신규 패턴 도입
- JPQL 작성/사용
- `@Query` 사용 (JPQL/native 모두)
- 서비스에서 `JPAQueryFactory` 직접 사용
- 매퍼 Bean 등록/매퍼 내 비즈니스 로직 구현
- 클라이언트 입력으로 audit user 직접 주입
- 로컬 DB를 MariaDB 외 엔진으로 대체

---

## 20) 에이전트 응답 형식 규칙
에이전트는 작업 결과를 아래 형식으로 보고한다.

1. 요구사항 해석
2. 변경 파일 목록
3. 핵심 구현 내용
4. 테스트/검증 결과
5. 리스크 및 후속 제안

---

## 21) 빠른 체크리스트
- [ ] 기존 아키텍처/코드 스타일 준수
- [ ] API/DTO/예외 포맷 일관성
- [ ] Querydsl 필요 구간에만 사용
- [ ] JPQL / `@Query` 미사용
- [ ] Repository 네이밍 규약 준수 (`XxxRepository`, `XxxQueryRepository`, `XxxQueryRepositoryImpl`)
- [ ] Querydsl 페이징 표준 유틸 사용 (정렬 매핑/Predicate 빌더)
- [ ] Static Mapper 규약 준수 (private 생성자, static 메서드, 명확한 메서드명)
- [ ] Audit User 자동 주입 적용 (`@CreatedBy`, `@LastModifiedBy`, `AuditorAware`)
- [ ] N+1/페이징/성능 확인
- [ ] MariaDB 실행계획 확인
- [ ] 로컬 Docker Compose MariaDB 기동 확인
- [ ] 테스트 포함
- [ ] 보안/민감정보 점검
