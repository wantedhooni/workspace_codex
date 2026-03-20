# sample_Observability

Spring Boot 기반 주문 운영 API에 JPA, JWT 인증, Prometheus 수집, Grafana 대시보드를 함께 붙인 관측성 샘플이다. 로컬에서 바로 실행하면 운영 주문 조회와 상태 변경, 메트릭 수집, 대시보드 시각화를 한 번에 확인할 수 있다.

## 목적

- JWT 기반 무상태 인증이 붙은 실무형 API를 관측성 스택과 함께 예제로 제공한다.
- JPA 기반 주문 데이터, 비즈니스 메트릭, HTTP 메트릭을 Prometheus에서 수집하도록 구성한다.
- Grafana provisioning 기반 대시보드를 포함해 실행 직후 바로 운영 화면을 확인할 수 있게 한다.

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring Web
- Spring Security
- Spring Data JPA
- H2 File Database
- JWT (`jjwt`)
- Spring Actuator
- Micrometer Prometheus Registry
- Prometheus
- Grafana
- Gradle

## 디렉터리 구조

```text
sample_Observability
├── AGENTS.md
├── PLANS.md
├── TASK.md
├── README.md
├── build.gradle
├── docker-compose.yml
├── gradle
├── grafana
├── prometheus
├── runtime
├── scripts
└── src
```

## 데모 계정

| Tenant | Username | Password | 역할 |
| --- | --- | --- | --- |
| `ops` | `ops.admin` | `demo1234` | 운영 관리자 |
| `ops` | `ops.viewer` | `demo1234` | 운영 조회 사용자 |
| `biz` | `biz.admin` | `demo1234` | 사업 관리자 |
| `biz` | `biz.viewer` | `demo1234` | 사업 조회 사용자 |

## 실행 방법

### 전체 시작

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_Observability
./scripts/all-start.sh
```

실행 후 확인 정보:

- 애플리케이션: `http://localhost:8089`
- H2 Console: `http://localhost:8089/h2-console`
- Prometheus: `http://localhost:9099`
- Grafana: `http://localhost:3010`
- Grafana 계정: `admin / admin`

### 전체 중지

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_Observability
./scripts/all-stop.sh
```

### 전체 재시작

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_Observability
./scripts/all-restart.sh
```

## 개별 실행

### 애플리케이션만 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_Observability
./gradlew bootRun
```

### Prometheus / Grafana만 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_Observability
docker compose up -d
```

## 주요 API

### 로그인

```bash
curl -X POST http://localhost:8089/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "tenantId": "ops",
    "username": "ops.admin",
    "password": "demo1234"
  }'
```

### 주문 목록 조회

```bash
curl http://localhost:8089/api/orders \
  -H "Authorization: Bearer <JWT>"
```

### 대시보드 요약 조회

```bash
curl http://localhost:8089/api/orders/dashboard \
  -H "Authorization: Bearer <JWT>"
```

### 신규 주문 등록

```bash
curl -X POST http://localhost:8089/api/orders \
  -H "Authorization: Bearer <JWT>" \
  -H 'Content-Type: application/json' \
  -d '{
    "orderNumber": "OBS-3001",
    "title": "신규 알림 정책 구성",
    "customerName": "홍길동",
    "priority": "HIGH",
    "amount": 1500000,
    "dueAt": "2026-03-21T18:00:00+09:00"
  }'
```

### 주문 상태 변경

```bash
curl -X PATCH http://localhost:8089/api/orders/1/status \
  -H "Authorization: Bearer <JWT>" \
  -H 'Content-Type: application/json' \
  -d '{
    "status": "COMPLETED"
  }'
```

### Prometheus 메트릭 확인

```bash
curl http://localhost:8089/actuator/prometheus
```

## 수집 메트릭

- `http_server_requests_seconds_*`: Spring Boot HTTP 응답 시간/건수
- `sample_order_created_total`: 테넌트/우선순위별 신규 주문 생성 건수
- `sample_order_status_changed_total`: 상태 전이 누적 건수
- `sample_order_expected_lead_time_seconds_*`: 신규 주문 예상 리드타임
- `sample_order_processing_time_seconds_*`: 주문 상태별 처리 시간

## Grafana 대시보드

프로비저닝 시 자동으로 `Sample Observability Overview` 대시보드가 생성된다.

- 전체 신규 주문 수
- 상태 변경 누적 수
- HTTP 평균 응답 시간
- 테넌트/우선순위별 신규 주문 추이
- 상태별 평균 처리 시간

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_Observability
./gradlew test
./gradlew build
sh -n scripts/all-start.sh
sh -n scripts/all-stop.sh
sh -n scripts/all-restart.sh
```

## 확인 포인트

- `ops.admin` 로그인 후 주문 목록, 대시보드, 등록/상태 변경이 정상 동작하는지 확인
- `ops.viewer` 로그인 후 조회는 가능하지만 등록은 거부되는지 확인
- `actuator/prometheus`에서 비즈니스 메트릭과 JVM/HTTP 메트릭이 함께 노출되는지 확인
- Grafana 대시보드에서 주문 생성과 상태 변경 후 패널 수치가 증가하는지 확인
