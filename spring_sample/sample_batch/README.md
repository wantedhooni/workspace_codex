# sample_batch

Spring Batch와 Quartz를 함께 사용해 대용량 거래 데이터를 PostgreSQL에서 읽고, 가공하고, 스케줄링하는 예제다. Batch 메타데이터와 Quartz JobStore 모두 DB에 저장하도록 맞춰 두었다.

## 포함된 예제

- 대량 원천 거래 데이터 적재 API
- `JdbcPagingItemReader` 기반 미처리 데이터 페이징 조회
- chunk 기반 정산 데이터 저장 및 원본 처리 상태 갱신
- Quartz JDBC JobStore 기반 주기 실행
- 운영 확인용 메트릭/스케줄 조회 API

## 기술 구성

- Spring Boot 3.4
- Spring Batch
- Quartz Scheduler
- PostgreSQL 16
- Gradle

## 요구 사항

- Java 21
- Docker

## 실행

### 1. PostgreSQL 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_batch
docker compose up -d
```

### 2. 애플리케이션 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_batch
./gradlew bootRun
```

기본 애플리케이션 포트는 `8080` 이고, PostgreSQL 포트는 `5432` 이다.

## 처리 흐름

1. `trade_raw_event` 테이블에 원천 거래 데이터를 대량 적재한다.
2. Quartz가 cron 스케줄에 따라 `tradeSettlementQuartzJob` 을 실행한다.
3. Quartz Job은 Spring Batch `tradeSettlementJob` 을 기동한다.
4. Batch Step은 `processed = false` 조건으로 데이터를 페이지 단위 조회한다.
5. 거래별 총액, 수수료, 순정산금액을 계산해 `trade_settlement_summary` 에 적재한다.
6. 성공한 건은 원천 테이블에서 `processed = true` 로 변경한다.

## 주요 클래스

- [TradeSettlementJobConfig.java](/Users/revy/workspace_codex/spring_sample/sample_batch/src/main/java/com/example/samplebatch/batch/TradeSettlementJobConfig.java): 배치 잡/스텝/리더/라이터 정의
- [QuartzConfig.java](/Users/revy/workspace_codex/spring_sample/sample_batch/src/main/java/com/example/samplebatch/config/QuartzConfig.java): JDBC JobStore 기반 Quartz 스케줄 설정
- [TradeSettlementQuartzJob.java](/Users/revy/workspace_codex/spring_sample/sample_batch/src/main/java/com/example/samplebatch/quartz/TradeSettlementQuartzJob.java): Quartz에서 Batch 실행 연결
- [TradeDatasetService.java](/Users/revy/workspace_codex/spring_sample/sample_batch/src/main/java/com/example/samplebatch/trade/TradeDatasetService.java): 대량 데이터 생성/적재
- [TradeBatchAdminController.java](/Users/revy/workspace_codex/spring_sample/sample_batch/src/main/java/com/example/samplebatch/trade/TradeBatchAdminController.java): 운영/실행 API

## 기본 설정

- DB URL: `jdbc:postgresql://localhost:5432/sample_batch`
- 계정: `sample_batch / sample_batch`
- Spring Batch schema 자동 생성: 활성화
- Quartz schema 자동 생성: 활성화
- Quartz cron: 1분마다 실행
- Batch chunk size: `1000`
- Batch page size: `1000`

## 주요 테이블

- `trade_raw_event`: 원천 거래 데이터
- `trade_settlement_summary`: 배치 처리 결과
- `BATCH_*`: Spring Batch 메타데이터
- `QRTZ_*`: Quartz JobStore 메타데이터

## API 예제

### 1. 대량 데이터 적재

```bash
curl -X POST http://localhost:8080/api/admin/datasets/trades \
  -H 'Content-Type: application/json' \
  -d '{"size":100000,"batchSize":5000,"truncateBeforeLoad":true}'
```

### 2. 수동 배치 실행

```bash
curl -X POST http://localhost:8080/api/admin/jobs/trade-settlement/run
```

### 3. Quartz 즉시 트리거

```bash
curl -X POST http://localhost:8080/api/admin/jobs/trade-settlement/quartz/trigger
```

### 4. 데이터 처리 현황 조회

```bash
curl http://localhost:8080/api/admin/datasets/trades/metrics
```

### 5. Quartz 스케줄 상태 조회

```bash
curl http://localhost:8080/api/admin/jobs/trade-settlement/scheduler
```

## 주요 프로퍼티

- `spring.datasource.*`: PostgreSQL 접속 정보
- `spring.batch.jdbc.initialize-schema`: Batch 메타데이터 테이블 자동 생성
- `spring.quartz.job-store-type=jdbc`: Quartz JDBC JobStore 사용
- `app.trade-settlement.chunk-size`: 청크 크기
- `app.trade-settlement.page-size`: DB 페이지 크기
- `app.trade-settlement.default-seed-batch-size`: 적재 배치 크기 기본값
- `app.trade-settlement.cron`: Quartz cron 스케줄

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_batch
./gradlew test
./gradlew build
```

테스트는 Testcontainers PostgreSQL을 사용한다. Docker 데몬이 없으면 통합 테스트는 자동 스킵될 수 있다.
