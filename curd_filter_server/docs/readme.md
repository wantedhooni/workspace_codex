# CURD Filter Server Docs

`curd-filter-server`는 Spring Boot 4 + JPA + Querydsl 기반의 엔터프라이즈용 CRUD 템플릿입니다.

## 1. 목적
- 반복되는 CRUD API를 빠르게 시작
- 필터 정책/정렬 정책을 오퍼레이션별로 통제
- 멀티모듈 경계(`core/domain/app/infra/api`)를 유지하며 확장

## 2. 기술 기준
- Java 21
- Spring Boot 4.0.3
- Spring Data JPA 4.x
- Querydsl 5.1.0 (`jakarta`)
- PostgreSQL
- Flyway
- OpenAPI 3 (springdoc)

## 3. 모듈 요약
- `core`: 공통 에러, ProblemDetails, 필터/정렬/페이지 정책, ULID, 감사 베이스 엔티티
- `domain`: Aggregate/Repository Port
- `app`: UseCase 서비스, CRUD 템플릿 메서드, 필터 정책 등록
- `infra`: JPA/Querydsl 어댑터, DB 마이그레이션
- `api`: Controller, 예외 핸들러, OpenAPI, 애플리케이션 엔트리

## 4. 실행
```bash
./gradlew :api:bootRun
```

환경 변수 기본값:
- `DB_URL=jdbc:postgresql://localhost:5432/curd_filter`
- `DB_USERNAME=curd`
- `DB_PASSWORD=curd`

## 5. API 엔드포인트
- `GET /api/v1/items`
- `GET /api/v1/items/{id}`
- `POST /api/v1/items`
- `PUT /api/v1/items/{id}`
- `PATCH /api/v1/items/{id}`
- `DELETE /api/v1/items/{id}`

Swagger UI:
- `/swagger-ui.html`

## 6. 필터 규약 (현재)
현재는 **AND-only** 입니다.
- `and=<field>:<op>:<value>` 반복
- `sort=field,-createdAt`
- `page`, `size`

예시:
```bash
curl 'http://localhost:8080/api/v1/items?and=name:contains:phone&and=price:gte:100&sort=-price&page=0&size=20'
```

## 7. 테스트
```bash
./gradlew test
```

## 8. 확장 시작점
1. `domain`에 Aggregate/Port 추가
2. `app`에 Command/Result/Service 추가
3. `infra`에 Entity/Repository/Adapter/PredicateFactory/Flyway 추가
4. `api`에 DTO/Controller/OpenAPI 추가
