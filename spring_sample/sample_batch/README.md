# sample_batch

Spring Batch와 Quartz를 함께 사용해 대용량 거래 데이터를 PostgreSQL에서 처리하는 샘플이다. Spring Batch 메타데이터와 Quartz JobStore를 모두 DB에 저장하도록 구성했다.

## 목적

- DB 기반 Batch + Scheduler 조합 예시 제공
- 대량 적재, 페이징 리더, 청크 쓰기 패턴 설명
- 운영 관점에서 배치 실행과 스케줄 상태 조회 API 제공

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring Batch
- Quartz Scheduler
- PostgreSQL 16
- Gradle

## 주요 기능

- 대량 거래 원천 데이터 적재 API
- `JdbcPagingItemReader` 기반 미처리 건 조회
- `JdbcBatchItemWriter` 기반 정산 결과 저장
- Quartz cron 스케줄과 수동 실행 동시 지원
- Batch/Quartz 메타데이터 DB 저장

## 실행

### PostgreSQL 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_batch
docker compose up -d
```

### 애플리케이션 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_batch
./gradlew bootRun
```

- 기본 포트: `8080`
- PostgreSQL 포트: `5432`

## 처리 흐름

1. `trade_raw_event` 테이블에 대량 데이터를 적재한다.
2. Quartz가 `tradeSettlementQuartzJob` 을 실행한다.
3. Quartz Job이 Spring Batch `tradeSettlementJob` 을 호출한다.
4. Batch Step이 `processed = false` 조건으로 데이터를 페이지 단위로 읽는다.
5. 정산 금액을 계산해 `trade_settlement_summary` 에 저장한다.
6. 처리 완료 건은 원본 테이블에서 `processed = true` 로 변경한다.

## 주요 테이블

- `trade_raw_event`
- `trade_settlement_summary`
- `BATCH_*`
- `QRTZ_*`

## API 예제

### 대량 데이터 적재

```bash
curl -X POST http://localhost:8080/api/admin/datasets/trades \
  -H 'Content-Type: application/json' \
  -d '{"size":100000,"batchSize":5000,"truncateBeforeLoad":true}'
```

### 수동 배치 실행

```bash
curl -X POST http://localhost:8080/api/admin/jobs/trade-settlement/run
```

### Quartz 즉시 트리거

```bash
curl -X POST http://localhost:8080/api/admin/jobs/trade-settlement/quartz/trigger
```

### 처리 현황 조회

```bash
curl http://localhost:8080/api/admin/datasets/trades/metrics
```

### 스케줄 상태 조회

```bash
curl http://localhost:8080/api/admin/jobs/trade-settlement/scheduler
```

## 주요 설정

- `spring.datasource.*`
- `spring.batch.jdbc.initialize-schema`
- `spring.quartz.job-store-type=jdbc`
- `app.trade-settlement.chunk-size`
- `app.trade-settlement.page-size`
- `app.trade-settlement.default-seed-batch-size`
- `app.trade-settlement.cron`

## 확장 방향

- 멀티 스텝 배치
- 파티셔닝 / 병렬 처리
- 다중 인스턴스 Quartz 클러스터
- Job parameter 기반 증분 처리

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_batch
./gradlew test
./gradlew build
```

Testcontainers PostgreSQL을 사용하므로 Docker 데몬이 없으면 통합 테스트가 자동 스킵될 수 있다.
