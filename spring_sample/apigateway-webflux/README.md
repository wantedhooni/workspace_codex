# apigateway-webflux

Spring Cloud Gateway WebFlux 기반 API 게이트웨이 샘플이다. 라우팅, 공통 응답 헤더, 간단한 메트릭, 내부 mock downstream 연동까지 한 프로젝트에서 확인할 수 있다.

## 목적

- WebFlux 기반 API Gateway의 기본 라우팅 구성 예시 제공
- GlobalFilter를 이용한 공통 헤더 주입과 요청 카운팅 방식 설명
- 다운스트림 서비스가 없더라도 로컬 mock endpoint로 게이트웨이 동작을 검증할 수 있게 구성

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring Cloud Gateway
- WebFlux
- Actuator
- Micrometer Prometheus
- Gradle

## 제공 기능

- `/api/customer-service/**` 고객 서비스 라우팅
- `/api/order-service/**` 주문 서비스 라우팅
- 게이트웨이 공통 응답 헤더 주입
- `/admin/routes` 라우트 설명 조회
- `/actuator/prometheus` 메트릭 노출

## 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/apigateway-webflux
./gradlew bootRun
```

- 애플리케이션 포트: `8084`

## API 예제

```bash
curl http://localhost:8084/api/customer-service/customers/CUST-100
```

```bash
curl http://localhost:8084/api/order-service/orders/ORD-200
```

```bash
curl http://localhost:8084/admin/routes
```

```bash
curl http://localhost:8084/actuator/prometheus
```

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/apigateway-webflux
./gradlew test
./gradlew build
```
