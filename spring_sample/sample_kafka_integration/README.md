# sample_kafka_integration

Kafka 기반 주문 지시 메시지를 발행하고, 소비 결과와 실패 격리 토픽까지 확인할 수 있는 실무형 샘플이다. REST API에서 메시지를 발행하고, 컨슈머가 처리 상태를 메모리 저장소에 기록한다.

## 목적

- Spring for Apache Kafka 기본 운영 패턴 예시 제공
- 발행, 소비, 재시도, DLT 분리 흐름 설명
- API 기준으로 메시지 처리 상태를 추적하는 예제 제공

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring Kafka
- Apache Kafka
- Kafka UI
- Gradle

## 주요 기능

- 거래 지시 메시지 발행 API
- JSON 직렬화 기반 Kafka Producer / Consumer
- `DefaultErrorHandler` + Dead Letter Topic 구성
- 메시지별 처리 상태 조회 API
- Kafka 토픽 자동 생성

## 실행

### Kafka 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_kafka_integration
docker compose up -d
```

### 애플리케이션 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_kafka_integration
./gradlew bootRun
```

- 애플리케이션 포트: `8080`
- Kafka 브로커 포트: `9092`
- Kafka UI: `http://localhost:8081`

## 처리 흐름

1. 클라이언트가 거래 지시를 `POST /api/trade-instructions` 로 요청한다.
2. 애플리케이션이 `trade.instructions.v1` 토픽에 메시지를 발행한다.
3. 컨슈머가 메시지를 받아 처리하고 상태를 `PROCESSED` 로 기록한다.
4. 의도적으로 실패한 메시지는 재시도 후 `trade.instructions.v1.dlt` 토픽으로 이동한다.
5. DLT 리스너가 실패 상태를 `FAILED` 로 기록한다.

## API 예제

### 메시지 발행

```bash
curl -X POST http://localhost:8080/api/trade-instructions \
  -H 'Content-Type: application/json' \
  -d '{"accountId":"ACC-1001","instrumentCode":"EURUSD-FWD","quantity":250000,"counterparty":"BANK-A"}'
```

### 실패 메시지 발행

```bash
curl -X POST http://localhost:8080/api/trade-instructions \
  -H 'Content-Type: application/json' \
  -d '{"accountId":"ACC-1002","instrumentCode":"USDKRW-NDF","quantity":100000,"counterparty":"FAIL"}'
```

### 처리 상태 조회

```bash
curl http://localhost:8080/api/trade-instructions/{messageId}
```

## 주요 설정

- `spring.kafka.bootstrap-servers`
- `spring.kafka.consumer.group-id`
- `app.kafka.trade-topic`
- `app.kafka.trade-dlt-topic`

## 확장 방향

- Avro 또는 Protobuf 스키마 레지스트리 연동
- 배치성 재처리 워커 추가
- 컨슈머 lag 모니터링과 알림 연동

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_kafka_integration
./gradlew test
./gradlew build
```
