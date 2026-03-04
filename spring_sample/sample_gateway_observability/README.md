# sample_gateway_observability

Spring Cloud Gateway MVC와 Observability를 함께 보여주는 샘플이다. 게이트웨이 라우팅과 메트릭 노출을 한 프로젝트에서 확인할 수 있다.

## 목적

- Gateway 계층에서 라우팅과 관측성을 함께 다루는 예시 제공
- Actuator/Prometheus 노출과 커스텀 메트릭 구성 설명
- 내부 백엔드로 프록시하는 간단한 gateway route 예시 제공

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring Cloud Gateway Server MVC
- Actuator
- Micrometer
- Prometheus Registry
- Gradle

## 제공 기능

- `/api/trades/**` 게이트웨이 라우팅
- `/api/risk/**` 게이트웨이 라우팅
- 커스텀 게이트웨이 요청 카운터
- `/actuator/prometheus` 메트릭 노출

## 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_gateway_observability
./gradlew bootRun
```

## API 예제

```bash
curl http://localhost:8080/api/trades/TR-100
```

```bash
curl http://localhost:8080/api/risk/ACC-100
```

```bash
curl http://localhost:8080/admin/gateway-metrics
```

```bash
curl http://localhost:8080/actuator/prometheus
```

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_gateway_observability
./gradlew test
./gradlew build
```
