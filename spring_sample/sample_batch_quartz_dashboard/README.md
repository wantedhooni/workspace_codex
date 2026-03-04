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

- 대량 요청 적재 API
- `JdbcPagingItemReader` 기반 배치 처리
- Quartz cron 스케줄 실행
- 수동 즉시 실행 API
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
6. 대시보드에서 요청 건수, 최근 배치 상태, 다음 트리거 시각을 확인한다.

## API 예제

### 요청 적재

```bash
curl -X POST http://localhost:8080/api/dashboard/tasks/seed \
  -H 'Content-Type: application/json' \
  -d '{"size":5000,"truncateBeforeLoad":true}'
```

### 수동 배치 실행

```bash
curl -X POST http://localhost:8080/api/dashboard/jobs/import/run
```

### Quartz 즉시 실행

```bash
curl -X POST http://localhost:8080/api/dashboard/jobs/import/quartz/trigger
```

### 대시보드 데이터 조회

```bash
curl http://localhost:8080/api/dashboard/overview
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
