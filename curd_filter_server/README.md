# curd-filter-server

Spring Boot 4 + JPA + Querydsl 기반 엔터프라이즈 CRUD 템플릿입니다.

## Stack
- Java 21
- Spring Boot 4.0.3
- Spring Data JPA
- Querydsl 5.1.0 (`jakarta`)
- PostgreSQL
- Flyway
- OpenAPI 3 (`springdoc`)
- Testcontainers (integration test)

## Module
- `core`: 공통 에러, ProblemDetails, 필터/정렬/페이지 정책, ULID, 감사 베이스 엔티티
- `domain`: Aggregate/Port (`Item`, `ItemRepositoryPort`)
- `app`: UseCase 서비스, CRUD 템플릿 메서드, 정책 등록
- `infra`: JPA/Querydsl 어댑터, Flyway SQL
- `api`: REST Controller, 예외 매핑, OpenAPI, 실행 앱

## Run
```bash
./gradlew :api:bootRun
```

기본 DB 접속값:
- `DB_URL=jdbc:postgresql://localhost:5432/curd_filter`
- `DB_USERNAME=curd`
- `DB_PASSWORD=curd`

## API
- `GET /api/v1/items`
- `GET /api/v1/items/{id}`
- `POST /api/v1/items`
- `PUT /api/v1/items/{id}`
- `PATCH /api/v1/items/{id}`
- `DELETE /api/v1/items/{id}`

Swagger UI:
- `/swagger-ui.html`

## Filter Query Convention
- AND: `and=<field>:<op>:<value>` (반복)
- 현재 버전은 모든 필터를 AND로만 처리
- sort: `sort=field,-createdAt`
- paging: `page=0&size=20`

예시:
```bash
curl 'http://localhost:8080/api/v1/items?and=name:contains:phone&and=price:gte:100&and=status:eq:ACTIVE&sort=-price&page=0&size=20'
```

## Error Contract (ProblemDetails extension)
- `orgCode`
- `appCode`
- `traceId`
- `errors[]`

## Policy 추가 방법
1. `app` 모듈에서 `FilterPolicy`를 리소스/오퍼레이션 단위로 정의
2. `InMemoryFilterPolicyRegistry`에 등록
3. `infra` 모듈에서 field-type map 및 Querydsl predicate factory를 추가
4. `api` 모듈에서 컨트롤러 엔드포인트 연결

## 신규 리소스 온보딩 절차
1. `domain`: Aggregate + Port 정의
2. `app`: Command/Result/Service 작성 (`CrudApplicationService` 상속)
3. `infra`: JPA Entity/Repository/Adapter/PredicateFactory + Flyway migration
4. `api`: Request/Response DTO + Controller + OpenAPI 문서화
5. 테스트: unit/slice/integration 추가

## Tests
```bash
./gradlew test
```
