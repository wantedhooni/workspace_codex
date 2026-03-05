# sample_batch_quartz_dashboard

Spring Batch와 Quartz를 DB 기반으로 운영하면서, 상태를 대시보드 화면과 API에서 함께 확인할 수 있는 샘플이다. 단순 실행 예제를 넘어서 운영자가 확인하는 메타데이터와 트리거 상태를 같이 보여준다.

## 목적

- Spring Batch + Quartz 조합의 운영형 구조 제공
- PostgreSQL 기반 메타데이터 저장 예시 제공
- 대시보드 화면에서 배치 처리량과 스케줄 상태를 확인하는 예제 제공

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring Batch
- Quartz
- PostgreSQL 16
- Actuator
- Gradle

## 주요 기능

- 합성/실제 샘플 데이터 적재 API
- `JdbcPagingItemReader` 기반 배치 처리
- Quartz cron 스케줄 실행
- 수동 즉시 실행 API
- 실제 데이터 기준 end-to-end 배치 실행 예제 API
- Quartz trigger pause/resume, scheduler standby/start, cron 변경 제어 API
- `/dashboard` 운영 화면
- Batch/Quartz 메타데이터 DB 저장

## 실행

### PostgreSQL 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_batch_quartz_dashboard
docker compose up -d
```

### 애플리케이션 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_batch_quartz_dashboard
./gradlew bootRun
```

- 애플리케이션 포트: `8080`
- PostgreSQL 포트: `5432`
- 대시보드: `http://localhost:8080/dashboard`

## 처리 흐름

1. 운영자가 요청 적재 API로 처리 대상을 넣는다.
2. Quartz가 `taskImportQuartzJob` 을 스케줄에 따라 실행한다.
3. Quartz Job이 Spring Batch `taskImportJob` 을 호출한다.
4. Batch가 미처리 요청을 페이지 단위로 읽고 감사 테이블에 기록한다.
5. 원본 요청은 `processed = true` 로 업데이트된다.
6. 대시보드에서 요청 건수, 최근 배치 상태, 다음 트리거 시각, Quartz 상태를 확인한다.

## 실제 데이터 예제

프로젝트는 `src/main/resources/sample-data/task-import-real-data.csv` 를 포함한다. 금융 거래 정산 배치에서 자주 보이는 항목(소스 시스템, 계좌, 상품코드, 시장구분, 통화, 원금, 우선순위)을 샘플로 제공한다.

- `task_import_request` 핵심 컬럼
  - `external_id`, `source_system`, `account_no`, `instrument_code`, `market`
  - `settlement_currency`, `notional_amount`, `priority`, `payload_size`
- 배치 처리 후 `task_import_audit` 에 `risk_bucket`, `processing_latency_ms` 를 기록한다.

## API 예제

### 요청 적재

```bash
curl -X POST http://localhost:8080/api/dashboard/tasks/seed \
  -H 'Content-Type: application/json' \
  -d '{"size":5000,"truncateBeforeLoad":true}'
```

### 실제 샘플 데이터 적재

```bash
curl -X POST "http://localhost:8080/api/dashboard/tasks/seed/real?truncateBeforeLoad=true"
```

### 수동 배치 실행

```bash
curl -X POST http://localhost:8080/api/dashboard/jobs/import/run
```

### Quartz 즉시 실행

```bash
curl -X POST http://localhost:8080/api/dashboard/jobs/import/quartz/trigger
```

### 실제 데이터 end-to-end 실행 예제

```bash
curl -X POST http://localhost:8080/api/dashboard/examples/real/run
```

### 대시보드 데이터 조회

```bash
curl http://localhost:8080/api/dashboard/overview
```

### 최근 감사 로그 조회

```bash
curl "http://localhost:8080/api/dashboard/audits/recent?limit=10"
```

### Quartz 상태 조회

```bash
curl http://localhost:8080/api/dashboard/quartz/status
```

### Quartz 제어 (pause/resume/standby/start)

```bash
curl -X POST http://localhost:8080/api/dashboard/quartz/pause
curl -X POST http://localhost:8080/api/dashboard/quartz/resume
curl -X POST http://localhost:8080/api/dashboard/quartz/standby
curl -X POST http://localhost:8080/api/dashboard/quartz/start
```

### Quartz cron 변경

```bash
curl -X PUT http://localhost:8080/api/dashboard/quartz/cron \
  -H 'Content-Type: application/json' \
  -d '{"cronExpression":"0 0/2 * * * ?"}'
```

## 주요 테이블

- `task_import_request`
- `task_import_audit`
- `BATCH_*`
- `QRTZ_*`

## 확장 방향

- 사용자별 배치 실행 이력 조회
- 실패 레코드 재처리 큐 추가
- Quartz 클러스터 다중 노드 확장

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_batch_quartz_dashboard
./gradlew test
./gradlew build
```
