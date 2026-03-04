# sample_observability_api

운영 관측성을 우선하는 REST API 샘플이다. 메트릭, observation, 지연 시간, 실패 호출을 코드 수준에서 직접 다루는 예제를 포함한다.

## 목적

- 운영형 REST API에서 관측 포인트를 어디에 심는지 설명
- Micrometer 메트릭과 Observation 사용 예시 제공
- Prometheus 수집 경로와 관리 API를 함께 제공

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring Web MVC
- Actuator
- Micrometer
- Prometheus Registry
- Gradle

## 주요 기능

- 성공/실패 호출 카운터
- 호출 시간 타이머
- ObservationRegistry 기반 관측 이벤트
- 느린 API 시뮬레이션
- Actuator Prometheus 엔드포인트 제공

## 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_observability_api
./gradlew bootRun
```

- 기본 포트: `8080`

## API 예제

### 일반 처리

```bash
curl -X POST http://localhost:8080/api/workloads/process \
  -H 'Content-Type: application/json' \
  -d '{"workloadId":"SYNC-1001","units":12,"simulateFailure":false}'
```

### 지연 처리

```bash
curl http://localhost:8080/api/workloads/slow/300
```

### 메트릭 요약

```bash
curl http://localhost:8080/api/admin/metrics-summary
```

### Prometheus 메트릭

```bash
curl http://localhost:8080/actuator/prometheus
```

## 관측 포인트

- `sample.workload.processed.total`
- `sample.workload.failed.total`
- `sample.workload.duration`
- Actuator `health`, `metrics`, `prometheus`

## 확장 방향

- OpenTelemetry trace 연동
- 구조화 로그와 traceId 결합
- business tag 추가
- readiness/liveness 세분화

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_observability_api
./gradlew test
./gradlew build
```
