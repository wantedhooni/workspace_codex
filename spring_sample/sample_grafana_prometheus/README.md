# sample_grafana_prometheus

Prometheus와 Grafana를 붙여 Spring Boot 애플리케이션 메트릭을 수집하고 시각화하는 샘플이다. 대시보드 프로비저닝 파일을 함께 제공해 실행 직후 바로 지표를 확인할 수 있다.

## 목적

- Actuator Prometheus 엔드포인트 연동 예시 제공
- 비즈니스 메트릭과 레이턴시 메트릭 수집 패턴 설명
- Grafana provisioning 기반 즉시 실행 가능한 운영 화면 제공

## 기술 스택

- Java 21
- Spring Boot 3.4
- Actuator
- Micrometer
- Prometheus
- Grafana
- Gradle

## 주요 기능

- 작업 처리 API
- 채널별 처리 건수와 실패 건수 카운터
- 처리 시간 타이머 기록
- Prometheus 스크랩 설정 포함
- Grafana 대시보드 자동 프로비저닝

## 실행

### Prometheus / Grafana 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_grafana_prometheus
docker compose up -d
```

### 애플리케이션 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_grafana_prometheus
./gradlew bootRun
```

- 애플리케이션 포트: `8080`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- Grafana 계정: `admin / admin`

## API 예제

### 작업 처리 요청

```bash
curl -X POST http://localhost:8080/api/ops/workloads \
  -H 'Content-Type: application/json' \
  -d '{"channel":"trading-api","workType":"pricing","volume":1200,"expectedDurationMs":250}'
```

### 지표 요약 조회

```bash
curl http://localhost:8080/api/ops/metrics/summary
```

### Prometheus 메트릭 조회

```bash
curl http://localhost:8080/actuator/prometheus
```

## 포함된 리소스

- `docker-compose.yml`
- `prometheus/prometheus.yml`
- `grafana/provisioning/datasources/datasource.yml`
- `grafana/provisioning/dashboards/dashboard.yml`
- `grafana/dashboards/workload-overview.json`

## 확장 방향

- Loki 연동과 로그 상관관계 추가
- OpenTelemetry trace 대시보드 확장
- SLA/SLO 기반 알림 룰 추가

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_grafana_prometheus
./gradlew test
./gradlew build
```
