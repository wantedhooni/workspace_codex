# sample_rabbitmq_integration

RabbitMQ 기반 정산 요청 메시지를 발행하고, 정상 큐와 DLQ를 함께 운영하는 샘플이다. 실무에서 자주 쓰는 `exchange + queue + dlq + JSON converter` 구성을 기준으로 잡았다.

## 목적

- Spring AMQP 기반 메시징 기본 구성 예시 제공
- Dead Letter Exchange / Queue 흐름 설명
- API에서 발행하고 소비 상태를 조회하는 운영 패턴 제시

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring AMQP
- RabbitMQ 3
- RabbitMQ Management UI
- Gradle

## 주요 기능

- 정산 요청 발행 API
- Direct Exchange 라우팅
- 실패 시 Dead Letter Queue 전송
- 메시지별 처리 상태 조회 API
- JSON 메시지 컨버터 구성

## 실행

### RabbitMQ 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_rabbitmq_integration
docker compose up -d
```

### 애플리케이션 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_rabbitmq_integration
./gradlew bootRun
```

- 애플리케이션 포트: `8080`
- RabbitMQ AMQP 포트: `5672`
- RabbitMQ 관리 콘솔: `http://localhost:15672`

## 처리 흐름

1. 클라이언트가 `POST /api/settlements` 로 정산 요청을 보낸다.
2. 애플리케이션이 `settlement.exchange` 로 메시지를 발행한다.
3. 컨슈머가 정산 요청을 처리하고 상태를 `PROCESSED` 로 기록한다.
4. 실패 메시지는 DLX를 거쳐 `settlement.dlq` 로 이동한다.
5. DLQ 리스너가 상태를 `FAILED` 로 기록한다.

## API 예제

### 메시지 발행

```bash
curl -X POST http://localhost:8080/api/settlements \
  -H 'Content-Type: application/json' \
  -d '{"settlementId":"SET-1001","bookCode":"FX-BOOK","currency":"USD","amount":500000,"counterparty":"BANK-A"}'
```

### 실패 메시지 발행

```bash
curl -X POST http://localhost:8080/api/settlements \
  -H 'Content-Type: application/json' \
  -d '{"settlementId":"SET-1002","bookCode":"FX-BOOK","currency":"KRW","amount":100000,"counterparty":"FAIL"}'
```

### 처리 상태 조회

```bash
curl http://localhost:8080/api/settlements/SET-1001
```

## 주요 설정

- `spring.rabbitmq.host`
- `spring.rabbitmq.listener.simple.default-requeue-rejected`
- `app.rabbitmq.exchange`
- `app.rabbitmq.queue`
- `app.rabbitmq.dlq`

## 확장 방향

- Publisher confirm / return callback 적용
- quorum queue 도입
- 소비자별 concurrency 분리

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_rabbitmq_integration
./gradlew test
./gradlew build
```
