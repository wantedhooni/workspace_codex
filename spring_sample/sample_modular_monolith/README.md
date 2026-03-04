# sample_modular_monolith

하나의 Spring Boot 애플리케이션 안에서 모듈 경계를 유지하는 업무 시스템 샘플이다. 주문 생성 후 재고 예약과 청구 발행이 순차적으로 반응하는 흐름을 담고 있다.

## 목적

- 모듈형 모놀리스 구조 예시 제공
- 도메인 이벤트로 모듈 간 결합도 낮추기
- 단일 배포 단위 안에서 업무 분리를 유지하는 방식 설명

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring Web MVC
- Spring Data JPA
- H2
- Gradle

## 모듈 구성

- `sales`: 주문 접수와 주문 상태 관리
- `inventory`: 재고 조회와 예약 처리
- `billing`: 청구서 발행

## 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_modular_monolith
./gradlew bootRun
```

- 기본 포트: `8080`
- H2 콘솔: `/h2-console`

## 처리 흐름

1. 주문 생성 API가 `SalesOrder` 를 저장한다.
2. `OrderPlacedEvent` 가 발행된다.
3. 재고 모듈이 이벤트를 받아 수량을 예약한다.
4. 재고 예약 성공 시 `InventoryReservedEvent` 가 발행된다.
5. 청구 모듈이 청구서를 생성하고 주문 상태를 `INVOICED` 로 변경한다.

## API 예제

### 주문 생성

```bash
curl -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerId":"CUS-100","sku":"AAPL","quantity":10,"unitPrice":182.50}'
```

### 주문 조회

```bash
curl http://localhost:8080/api/orders/{orderId}
```

### 재고 조회

```bash
curl http://localhost:8080/api/inventory/AAPL
```

### 청구 목록 조회

```bash
curl http://localhost:8080/api/invoices
```

## 핵심 구성

- `OrderApplicationService`: 주문 생성
- `InventoryReservationService`: 주문 이벤트 기반 재고 예약
- `BillingService`: 재고 예약 이벤트 기반 청구 발행

## 확장 방향

- 실제 모듈 경계 검증 도구 도입
- outbox/event log 추가
- 모듈별 패키지 접근 제한
- 읽기 모델 분리

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_modular_monolith
./gradlew test
./gradlew build
```
