# HELP

실무에서 자주 마주치는 이슈를 빠르게 해결하기 위한 가이드입니다.

## 1. 서버 기동 실패
### 증상
- DB 연결 오류
- Hibernate schema validation 오류

### 확인 순서
1. PostgreSQL 기동 여부 확인
2. `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 값 확인
3. Flyway 마이그레이션 테이블 생성 여부 확인

### 빠른 점검
```bash
./gradlew :api:bootRun
```

## 2. 테스트 실패
### 증상
- Testcontainers 관련 실패
- 통합 테스트에서 DB 초기화 실패

### 확인 순서
1. Docker 실행 여부 확인
2. 로컬 네트워크/권한 확인
3. 단일 테스트로 원인 축소

```bash
./gradlew :api:test --tests 'com.curd.template.api.item.ItemControllerIntegrationTest.searchWithFilters'
```

## 3. 필터가 동작하지 않음
### 현재 정책
- `and` 파라미터만 사용
- 모든 조건은 AND로 평가
- `sort`는 허용 필드 화이트리스트만 통과

### 점검 포인트
1. 형식이 `field:op:value`인지 확인
2. policy에 해당 field/operator가 허용되어 있는지 확인
3. value 길이/절 개수 제한 초과 여부 확인

## 4. CUD에서 필터 파라미터 사용 시 4xx
정상 동작입니다.
- 정책상 필터는 조회(`GET /api/v1/items`) 전용
- `POST/PUT/PATCH/DELETE`는 ID 기반 처리만 허용

## 5. OpenAPI 문서가 비정상
### 확인 순서
1. `/swagger-ui.html` 접속 확인
2. `api` 모듈의 springdoc 버전 확인
3. Boot 버전과 호환 여부 확인

## 6. 신규 리소스 추가 체크리스트
1. `domain`: Aggregate + Repository Port
2. `app`: CRUD 서비스 + 명령/결과 DTO
3. `infra`: JPA Entity + Adapter + Querydsl predicate factory + Flyway SQL
4. `api`: Controller + Request/Response + 예외 매핑 확인
5. 테스트: Unit/Slice/Integration 추가

## 7. 운영 권장
- `sort` 화이트리스트 유지
- 페이지 크기 상한 유지
- ProblemDetails의 `traceId`를 로그와 연계
- 정책(`FilterPolicy`) 변경 시 테스트 케이스 함께 보강
