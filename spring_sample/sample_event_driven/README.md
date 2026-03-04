# sample_event_driven

주문 생성부터 결제 결과 반영까지를 이벤트 기반으로 연결하는 샘플이다. 주문 생성 시 DB에 주문과 아웃박스 이벤트를 함께 저장하고, 발행 단계가 Kafka로 이벤트를 내보낸다.

## 목적

- Outbox 패턴과 비동기 흐름 예시 제공
- 주문, 결제, 상태 반영을 느슨하게 연결하는 구조 설명
- idempotency key와 수동 발행 API를 함께 제공

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring Web MVC
- Spring Data JPA
- Spring for Apache Kafka
- PostgreSQL 16
- Gradle

## 주요 기능

- 주문 생성과 idempotency key 중복 방지
- Outbox 이벤트 저장
- Kafka 토픽 발행
- 결제 승인/실패 소비 후 주문 상태 반영
- 운영용 Outbox 조회/수동 발행 API

## 실행

### 인프라 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_event_driven
docker compose up -d
```

### 애플리케이션 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_event_driven
./gradlew bootRun
```

- 애플리케이션 포트: `8080`
- PostgreSQL 포트: `5433`
- Kafka 포트: `9092`

## 처리 흐름

1. 주문 API가 주문과 Outbox 이벤트를 같은 트랜잭션으로 저장한다.
2. Outbox 발행기가 미발행 이벤트를 읽어 Kafka 토픽으로 발행한다.
3. 결제 리스너가 주문 생성 이벤트를 소비한다.
4. 결제 결과 이벤트를 다시 발행한다.
5. 주문 상태 프로젝션이 결제 결과를 반영한다.

## API 예제

### 주문 생성

```bash
curl -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"accountId":"ACC-100","amount":1200.50,"idempotencyKey":"order-100-1"}'
```

### 주문 조회

```bash
curl http://localhost:8080/api/orders/1
```

### Outbox 수동 발행

```bash
curl -X POST http://localhost:8080/api/admin/outbox/publish
```

### 미발행 Outbox 조회

```bash
curl http://localhost:8080/api/admin/outbox/pending
```

## 주요 설정

- `spring.datasource.*`
- `spring.kafka.bootstrap-servers`
- `app.kafka.enabled`
- `app.outbox.scheduler-enabled`
- `app.outbox.fixed-delay`
- `app.events.orders-topic`
- `app.events.payments-topic`

## 확장 방향

- 재시도 / DLQ 처리
- 소비자 idempotency 테이블
- Debezium 기반 Outbox Relay
- Saga 오케스트레이션

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_event_driven
./gradlew test
./gradlew build
```
