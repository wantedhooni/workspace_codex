# sample_recommend

Spring Boot 기반 상품 추천 시스템 서버 샘플이다. 고객 프로필, 최근 행동 이력, 상품 인기도/재고를 함께 고려해 추천 점수를 계산하고 추천 결과를 반환한다.

## 목적

- 추천 엔진의 기본 점수 계산 구조를 계층형 서버로 예시화
- 고객 행동 적재 후 추천 결과가 바뀌는 흐름 제공
- 운영에서 필요한 요청 로그, 실행 스크립트, 테스트 문서 포함

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring Web MVC
- Spring Data JPA
- H2 Database
- Gradle

## 주요 기능

- 고객 프로필 기반 카테고리/가격대 선호 반영
- 최근 행동(view/cart/purchase) 기반 관심도 가중치 반영
- 재고 부족 패널티와 상품 인기도 반영
- 추천 요청 로그 저장
- 고객 행동 적재 API 제공
- 데모 상품 카탈로그 조회 API 제공

## 프로젝트 구조

```text
sample_recommend
├── src/main/java/com/example/samplerecommend
│   ├── common
│   ├── config
│   ├── domain
│   ├── dto
│   ├── repository
│   ├── service
│   └── web
├── scripts
├── AGENTS.md
├── PLANS.md
├── TASK.md
└── README.md
```

## 실행 방법

### 1. 애플리케이션 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_recommend
./gradlew bootRun
```

### 2. 전체 실행 스크립트 사용

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_recommend
./scripts/all-start.sh
```

### 3. 중지 / 재시작

```bash
./scripts/all-stop.sh
./scripts/all-restart.sh
```

## 접속 정보

- 애플리케이션: `http://localhost:8080`
- H2 콘솔: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:recommend`
- 사용자명: `sa`
- 비밀번호: 빈 값

## 데모 고객 / 상품

- `CUST-001`: AUDIO 선호, 최대 300000원, VIP
- `CUST-002`: OFFICE 선호, 최대 500000원, BASIC
- `CUST-003`: WEARABLE 선호, 최대 150000원, BASIC

주요 상품 예시:

- `P-1001`: 노이즈 캔슬링 헤드폰
- `P-1002`: 무선 블루투스 스피커
- `P-2001`: 인체공학 기계식 키보드
- `P-3001`: 스마트 워치 프로

## API 예시

### 추천 조회

```bash
curl "http://localhost:8080/api/recommendations?customerId=CUST-001&limit=3"
```

### 고객 행동 적재

```bash
curl -X POST "http://localhost:8080/api/customers/CUST-003/actions" \
  -H "Content-Type: application/json" \
  -d '{
    "productCode": "P-3001",
    "actionType": "PURCHASE",
    "weight": 5
  }'
```

### 상품 목록 조회

```bash
curl "http://localhost:8080/api/catalog/products"
```

## 추천 점수 기준

1. 상품 기본 인기도 점수의 35%를 반영한다.
2. 고객 선호 카테고리 일치 시 35점을 추가한다.
3. 고객 선호 가격대 이내면 20점을 추가하고, 초과 시 5점을 차감한다.
4. 최근 행동 가중치 합계에 6배를 곱해 관심도 점수로 더한다.
5. VIP 고객은 4점을 추가한다.
6. 재고가 5개 미만이면 10점을 차감한다.

## 테스트

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_recommend
./gradlew test
```

## 확장 방향

- 추천 요청/클릭 로그를 별도 분석 저장소로 적재
- 협업 필터링 또는 임베딩 기반 추천으로 확장
- Redis 캐시로 고객별 추천 결과 캐싱
- 배치/스트림 기반 특징량(feature) 파이프라인 연결

