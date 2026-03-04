# r2dbc

Spring Data R2DBC와 WebFlux를 사용해 고객 계정 정보를 반응형으로 관리하는 샘플이다. PostgreSQL을 연결하고 논블로킹 CRUD API를 구성하는 기본 패턴에 집중했다.

## 목적

- R2DBC 기반 반응형 Repository와 Service 계층 구조 예시 제공
- PostgreSQL 스키마 초기화와 애플리케이션 설정 분리 방식 설명
- 실무형 API에서 자주 필요한 코드 중복 검증, 조회, 상태 변경 흐름 제공

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring WebFlux
- Spring Data R2DBC
- PostgreSQL
- Gradle

## 제공 기능

- 고객 계정 목록 조회
- 고객 코드 기준 단건 조회
- 신규 고객 계정 등록
- 고객 등급 변경

## 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/r2dbc
docker compose up -d
./gradlew bootRun
```

- 애플리케이션 포트: `8081`
- PostgreSQL 포트: `5433`

## API 예제

```bash
curl http://localhost:8081/api/customers
```

```bash
curl -X POST http://localhost:8081/api/customers \
  -H 'Content-Type: application/json' \
  -d '{
    "customerCode": "CUST-300",
    "name": "Han Sujin",
    "email": "sujin.han@example.com",
    "tier": "VIP"
  }'
```

```bash
curl -X PATCH http://localhost:8081/api/customers/CUST-300/tier \
  -H 'Content-Type: application/json' \
  -d '{"tier":"ENTERPRISE"}'
```

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/r2dbc
./gradlew test
./gradlew build
```
