# 공통 메트릭 운영 가이드

## 목적

`common-metrics`는 모든 Spring Boot 애플리케이션이 동일한 Micrometer 규칙을 사용하도록 관리하는 Java 라이브러리 모듈이다. 실행 애플리케이션이 아니므로 독립 Docker 이미지나 Kubernetes Deployment를 만들지 않는다.

공통 모듈이 제공하는 기능:

- Actuator와 Prometheus Registry 의존성
- `application`, `environment` 공통 태그
- HTTP `uri` 태그와 업무 `operation` 태그의 카디널리티 제한
- `@BusinessMetric` 서비스 메서드 처리시간, 호출 수, 성공·실패 결과
- Spring Boot Auto-configuration

## 모듈 사용

애플리케이션 모듈은 다음 의존성만 선언한다.

```groovy
dependencies {
    implementation project(':common-metrics')
}
```

Actuator와 Prometheus 의존성을 각 서비스에서 다시 선언하지 않는다. 트레이싱 의존성은 메트릭과 독립적인 선택 사항이므로 현재 각 실행 모듈에서 유지한다.

## 공통 태그

| 태그 | 값 | 설명 |
| --- | --- | --- |
| `application` | `spring.application.name` | 서비스 이름 |
| `environment` | `local`, `dev`, `staging`, `production` | 배포 환경 |
| `operation` | 애너테이션에 선언한 고정값 | 업무 작업 |
| `outcome` | `success`, `failure` | 업무 작업 결과 |

Kubernetes overlay는 다음 환경변수를 주입한다.

```text
COMMERCE_OBSERVABILITY_METRICS_ENVIRONMENT
```

로컬 기본 환경은 `local`이다.

사용자 ID, 계좌 ID, 콘텐츠 ID, 이메일, 계좌번호, URL 원문, 예외 메시지는 메트릭 태그로 사용하지 않는다. 이런 값은 시계열 수를 통제할 수 없어 Mimir 메모리와 저장 비용을 급격히 증가시킨다.

## 업무 메트릭 사용

측정할 애플리케이션 서비스의 public 메서드에 고정된 작업명을 선언한다.

```java
@Transactional
@BusinessMetric("account.deposit")
public AccountTransaction deposit(...) {
    // 업무 처리
}
```

공통 Aspect는 정상 반환을 `success`, 예외 발생을 `failure`로 기록하고 예외는 변경하지 않고 다시 전달한다. 트랜잭션 commit 또는 rollback 시간도 포함하도록 트랜잭션 Aspect 바깥에서 측정한다.

현재 작업명:

```text
user.create
user.get
user.list
account.create
account.get
account.list
account.deposit
account.withdraw
account.transactions.list
contents.create
contents.get
contents.update
contents.publish
contents.archive
contents.list
```

Spring AOP 프록시를 거치지 않는 private 메서드와 같은 객체 내부의 self-invocation에는 `@BusinessMetric`을 사용하지 않는다.

## 생성되는 Prometheus 메트릭

업무 Timer 이름:

```text
commerce.business.operation.duration
```

Prometheus 노출 이름:

```text
commerce_business_operation_duration_seconds_count
commerce_business_operation_duration_seconds_sum
commerce_business_operation_duration_seconds_bucket
```

HTTP 메트릭은 Spring Boot 기본 이름을 유지한다.

```text
http_server_requests_seconds_count
http_server_requests_seconds_sum
http_server_requests_seconds_bucket
```

## 설정

```yaml
commerce:
  observability:
    metrics:
      enabled: true
      environment: local
      max-uri-tags: 200
      max-operation-tags: 100
```

| 설정 | 기본값 | 역할 |
| --- | ---: | --- |
| `enabled` | `true` | 공통 자동 구성 활성화 |
| `environment` | `local` | 환경 공통 태그 |
| `max-uri-tags` | `200` | 서비스별 HTTP URI 태그 최대 수 |
| `max-operation-tags` | `100` | 서비스별 업무 작업 태그 최대 수 |

제한을 초과한 신규 태그 조합은 등록하지 않는다. 제한을 늘리기 전에 동적 URI 또는 작업명이 유입됐는지 먼저 확인한다.

## PromQL

서비스별 HTTP 요청률:

```promql
sum by (environment, application) (
  rate(http_server_requests_seconds_count[5m])
)
```

업무 작업 처리율:

```promql
sum by (environment, application, operation) (
  rate(commerce_business_operation_duration_seconds_count[5m])
)
```

업무 작업 실패율:

```promql
sum by (environment, application, operation) (
  rate(commerce_business_operation_duration_seconds_count{outcome="failure"}[5m])
)
/
clamp_min(
  sum by (environment, application, operation) (
    rate(commerce_business_operation_duration_seconds_count[5m])
  ),
  0.000001
)
```

업무 작업 p95:

```promql
histogram_quantile(
  0.95,
  sum by (le, environment, application, operation) (
    rate(commerce_business_operation_duration_seconds_bucket[5m])
  )
)
```

## 검증

로컬 애플리케이션의 Prometheus endpoint:

```bash
curl -fsS http://localhost:8081/actuator/prometheus \
  | grep commerce_business_operation
```

메트릭은 해당 업무 API를 한 번 이상 호출한 뒤 생성된다. Kubernetes에서는 Pod annotation을 기준으로 OpenTelemetry Collector가 endpoint를 scrape하고 Mimir로 remote write한다.
