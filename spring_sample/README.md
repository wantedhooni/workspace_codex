# spring_sample

Spring Boot 기반 실전형 샘플 프로젝트 모음이다. 각 폴더는 독립 실행형 프로젝트이며, 웹/API, 보안, AI, 배치, 이벤트 드리븐, 실시간 통신, 게이트웨이, Native Image, 배포 자동화까지 주제별로 분리했다.

최근 추가된 `r2dbc`, `r2dbc-auditlog`, `webflux`, `apigateway-webflux`는 반응형 데이터 처리, 감사 로그, SSE, API Gateway를 한 번에 확인할 수 있는 reactive 샘플 묶음이다.
`blue-green`은 Nginx + Docker Compose 기반 Blue-Green 배포와 롤백 자동화 스크립트를 포함한 운영 배포 샘플이다.
`blue-green-nginx`는 Docker 없이 Nginx + 로컬 프로세스 기반 Blue-Green 배포를 `sh` 스크립트로 운영하는 샘플이다.

## 프로젝트 지도

### 웹 / BFF / API

| 프로젝트 | 설명 | 주요 기술 |
| --- | --- | --- |
| `sample_ai_bff` | 내부 지식 문서를 검색하고 답변을 조합하는 AI BFF 샘플 | Spring Boot, Web, SSE |
| `sample_secure_bff` | 인증/인가와 API composition을 적용한 Secure BFF 샘플 | Spring Boot, Security, RestClient, HTTP Interface |
| `sample_observability_api` | 메트릭과 observation 중심의 운영형 REST API 샘플 | Spring Boot, Actuator, Micrometer, Prometheus |
| `sample_websocket_realtime` | STOMP 기반 가격/작업 진행률 실시간 전송 샘플 | Spring Boot, WebSocket, STOMP |
| `sample_gateway_observability` | Gateway 라우팅과 메트릭 노출을 결합한 샘플 | Spring Boot, Spring Cloud Gateway, Prometheus |
| `sample_spring_admin` | Spring Boot Admin UI와 자기 등록형 모니터링 샘플 | Spring Boot Admin, Actuator |
| `webflux` | 운영 대시보드 집계와 SSE 스트림을 제공하는 WebFlux 샘플 | Spring Boot, WebFlux, Reactor |
| `apigateway-webflux` | Spring Cloud Gateway WebFlux 기반 API Gateway 샘플 | Spring Boot, Spring Cloud Gateway, WebFlux |

### 데이터 / 인프라 / 배치

| 프로젝트 | 설명 | 주요 기술 |
| --- | --- | --- |
| `sample_redisson` | Redis 기반 키-값 저장, 원자 카운터, 분산 락 샘플 | Spring Boot, Redisson, Redis |
| `sample_batch` | DB 기반 Spring Batch + Quartz 대용량 처리 샘플 | Spring Boot, Spring Batch, Quartz, PostgreSQL |
| `sample_batch_quartz_dashboard` | Batch/Quartz 메타데이터와 운영 화면을 함께 제공하는 샘플 | Spring Boot, Spring Batch, Quartz, PostgreSQL |
| `sample_native_image` | GraalVM Native Image 빌드 준비 샘플 | Spring Boot, AOT, RuntimeHints |
| `sample_grafana_prometheus` | Prometheus 수집과 Grafana 대시보드 프로비저닝 샘플 | Spring Boot, Micrometer, Prometheus, Grafana |
| `r2dbc` | PostgreSQL 기반 반응형 고객 계정 CRUD 샘플 | Spring Boot, WebFlux, Spring Data R2DBC, PostgreSQL |
| `r2dbc-auditlog` | 승인 요청과 감사 로그를 함께 저장하는 반응형 샘플 | Spring Boot, WebFlux, Spring Data R2DBC, PostgreSQL |
| `blue-green` | Nginx 업스트림 스위칭 기반 Blue-Green 배포/롤백 샘플 | Spring Boot, Docker Compose, Nginx, Bash |
| `blue-green-nginx` | Docker 없이 로컬 프로세스 Blue-Green 배포/롤백 샘플 | Spring Boot, Nginx, POSIX Shell |

### 아키텍처 / 메시징 / AI Tooling

| 프로젝트 | 설명 | 주요 기술 |
| --- | --- | --- |
| `sample_modular_monolith` | 주문, 재고, 청구를 모듈 단위로 나눈 업무 시스템 샘플 | Spring Boot, JPA, Domain Event, H2 |
| `sample_event_driven` | 주문 생성, outbox, Kafka 발행, 결제 결과 반영 흐름 샘플 | Spring Boot, JPA, Kafka, PostgreSQL |
| `sample_kafka_integration` | 거래 지시 메시지 발행, 소비, DLT 처리 샘플 | Spring Boot, Spring Kafka, Kafka UI |
| `sample_rabbitmq_integration` | 정산 요청 발행, 소비, DLQ 처리 샘플 | Spring Boot, Spring AMQP, RabbitMQ |
| `sample_mcp_server` | 운영 도구를 MCP Tool 로 노출하는 샘플 | Spring Boot, Spring AI MCP Server |
| `sample_mcp_client` | MCP 서버에 연결해 tool 호출을 감싸는 샘플 | Spring Boot, Spring AI MCP Client |

## 공통 요구 사항

- Java 21
- Docker
- macOS 또는 Linux 기준 쉘 환경

## 빠른 시작

### 이번에 추가한 reactive 샘플 먼저 실행하기

```bash
cd /Users/revy/workspace_codex/spring_sample/r2dbc && docker compose up -d && ./gradlew bootRun
cd /Users/revy/workspace_codex/spring_sample/r2dbc-auditlog && docker compose up -d && ./gradlew bootRun
cd /Users/revy/workspace_codex/spring_sample/webflux && ./gradlew bootRun
cd /Users/revy/workspace_codex/spring_sample/apigateway-webflux && ./gradlew bootRun
```

- `r2dbc`: 고객 계정 CRUD 샘플, 애플리케이션 `8081`, PostgreSQL `5433`
- `r2dbc-auditlog`: 승인 요청 + 감사 로그 샘플, 애플리케이션 `8082`, PostgreSQL `5434`
- `webflux`: 운영 대시보드 + SSE 샘플, 애플리케이션 `8083`
- `apigateway-webflux`: Spring Cloud Gateway WebFlux 샘플, 애플리케이션 `8084`

### 인프라가 필요한 프로젝트

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_redisson && docker compose up -d
cd /Users/revy/workspace_codex/spring_sample/sample_batch && docker compose up -d
cd /Users/revy/workspace_codex/spring_sample/sample_event_driven && docker compose up -d
cd /Users/revy/workspace_codex/spring_sample/sample_kafka_integration && docker compose up -d
cd /Users/revy/workspace_codex/spring_sample/sample_rabbitmq_integration && docker compose up -d
cd /Users/revy/workspace_codex/spring_sample/sample_grafana_prometheus && docker compose up -d
cd /Users/revy/workspace_codex/spring_sample/sample_batch_quartz_dashboard && docker compose up -d
cd /Users/revy/workspace_codex/spring_sample/r2dbc && docker compose up -d
cd /Users/revy/workspace_codex/spring_sample/r2dbc-auditlog && docker compose up -d
```

### 바로 실행 가능한 대표 샘플

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_ai_bff && ./gradlew bootRun
cd /Users/revy/workspace_codex/spring_sample/sample_secure_bff && ./gradlew bootRun --args='--server.port=8081'
cd /Users/revy/workspace_codex/spring_sample/sample_websocket_realtime && ./gradlew bootRun --args='--server.port=8085'
cd /Users/revy/workspace_codex/spring_sample/sample_spring_admin && ./gradlew bootRun --args='--server.port=8086'
```

위 명령은 동시 실행 기준 예시다. 개별 프로젝트만 실행할 때는 각 프로젝트 `README.md`의 기본 포트를 그대로 사용하면 된다.

### Blue-Green (Nginx, Non-Docker) 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/blue-green-nginx
./scripts/deploy.sh blue
./scripts/status.sh
```

두 번째 배포부터는 `./scripts/deploy.sh`만 실행하면 반대 슬롯으로 자동 전환된다.

### Blue-Green 배포 절차 예시

```bash
cd /Users/revy/workspace_codex/spring_sample/blue-green-nginx

# 1) 초기 배포 (blue 활성화)
./scripts/deploy.sh blue

# 2) 신규 버전 배포 (반대 슬롯 자동 전환)
DEPLOY_VERSION=v2026.03.05 ./scripts/deploy.sh

# 3) 상태 확인
./scripts/status.sh
curl http://localhost:8088/api/deployment

# 4) 문제 발생 시 즉시 롤백
./scripts/rollback.sh
```

- Nginx ingress 주소: `http://localhost:8088`
- Blue 직접 확인: `http://localhost:18081/api/deployment`
- Green 직접 확인: `http://localhost:18082/api/deployment`

### MCP 서버/클라이언트 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_mcp_server && ./gradlew bootRun
cd /Users/revy/workspace_codex/spring_sample/sample_mcp_client && ./gradlew bootRun
```

## 기본 포트

| 프로젝트 | 기본 포트 |
| --- | --- |
| `sample_redisson` | `8080` |
| `sample_batch` | `8080` |
| `sample_ai_bff` | `8080` |
| `sample_secure_bff` | `8080` |
| `sample_observability_api` | `8080` |
| `sample_modular_monolith` | `8080` |
| `sample_event_driven` | `8080` |
| `sample_websocket_realtime` | `8080` |
| `sample_native_image` | `8080` |
| `sample_gateway_observability` | `8080` |
| `sample_kafka_integration` | `8080` |
| `sample_rabbitmq_integration` | `8080` |
| `sample_grafana_prometheus` | `8080` |
| `sample_spring_admin` | `8080` |
| `sample_batch_quartz_dashboard` | `8080` |
| `sample_mcp_server` | `8090` |
| `sample_mcp_client` | `8091` |
| `r2dbc` | `8081` |
| `r2dbc-auditlog` | `8082` |
| `webflux` | `8083` |
| `apigateway-webflux` | `8084` |
| `blue-green` | `8088` (Nginx ingress), `18081` (blue), `18082` (green) |
| `blue-green-nginx` | `8088` (Nginx ingress), `18081` (blue), `18082` (green) |

여러 프로젝트를 동시에 실행하려면 `--server.port=` 로 포트를 조정해야 한다. 위 빠른 시작 예시는 신규 reactive 샘플의 기본 포트 `8081`~`8084`를 우선 유지하도록 맞췄다.

## 신규 reactive 샘플 비교

| 프로젝트 | 핵심 시나리오 | 확인 포인트 |
| --- | --- | --- |
| `r2dbc` | PostgreSQL 반응형 CRUD | R2DBC Repository, SQL 초기화, WebFlux API |
| `r2dbc-auditlog` | 상태 변경 + 감사 로그 저장 | 반응형 트랜잭션, audit trail, 이력 조회 |
| `webflux` | 비동기 집계 + SSE | `Mono.zip`, `Flux`, `text/event-stream` |
| `apigateway-webflux` | Gateway 라우팅 | Spring Cloud Gateway, GlobalFilter, 메트릭 헤더 |

## 문서 위치

- [sample_redisson/README.md](/Users/revy/workspace_codex/spring_sample/sample_redisson/README.md)
- [sample_batch/README.md](/Users/revy/workspace_codex/spring_sample/sample_batch/README.md)
- [sample_ai_bff/README.md](/Users/revy/workspace_codex/spring_sample/sample_ai_bff/README.md)
- [sample_secure_bff/README.md](/Users/revy/workspace_codex/spring_sample/sample_secure_bff/README.md)
- [sample_observability_api/README.md](/Users/revy/workspace_codex/spring_sample/sample_observability_api/README.md)
- [sample_modular_monolith/README.md](/Users/revy/workspace_codex/spring_sample/sample_modular_monolith/README.md)
- [sample_event_driven/README.md](/Users/revy/workspace_codex/spring_sample/sample_event_driven/README.md)
- [sample_mcp_server/README.md](/Users/revy/workspace_codex/spring_sample/sample_mcp_server/README.md)
- [sample_mcp_client/README.md](/Users/revy/workspace_codex/spring_sample/sample_mcp_client/README.md)
- [sample_websocket_realtime/README.md](/Users/revy/workspace_codex/spring_sample/sample_websocket_realtime/README.md)
- [sample_native_image/README.md](/Users/revy/workspace_codex/spring_sample/sample_native_image/README.md)
- [sample_gateway_observability/README.md](/Users/revy/workspace_codex/spring_sample/sample_gateway_observability/README.md)
- [sample_kafka_integration/README.md](/Users/revy/workspace_codex/spring_sample/sample_kafka_integration/README.md)
- [sample_rabbitmq_integration/README.md](/Users/revy/workspace_codex/spring_sample/sample_rabbitmq_integration/README.md)
- [sample_grafana_prometheus/README.md](/Users/revy/workspace_codex/spring_sample/sample_grafana_prometheus/README.md)
- [sample_spring_admin/README.md](/Users/revy/workspace_codex/spring_sample/sample_spring_admin/README.md)
- [sample_batch_quartz_dashboard/README.md](/Users/revy/workspace_codex/spring_sample/sample_batch_quartz_dashboard/README.md)
- [r2dbc/README.md](/Users/revy/workspace_codex/spring_sample/r2dbc/README.md)
- [r2dbc-auditlog/README.md](/Users/revy/workspace_codex/spring_sample/r2dbc-auditlog/README.md)
- [webflux/README.md](/Users/revy/workspace_codex/spring_sample/webflux/README.md)
- [apigateway-webflux/README.md](/Users/revy/workspace_codex/spring_sample/apigateway-webflux/README.md)
- [blue-green/README.md](/Users/revy/workspace_codex/spring_sample/blue-green/README.md)
- [blue-green-nginx/README.md](/Users/revy/workspace_codex/spring_sample/blue-green-nginx/README.md)

## 검증 명령

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_redisson && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_batch && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_ai_bff && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_secure_bff && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_observability_api && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_modular_monolith && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_event_driven && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_mcp_server && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_mcp_client && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_websocket_realtime && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_native_image && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_gateway_observability && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_kafka_integration && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_rabbitmq_integration && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_grafana_prometheus && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_spring_admin && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_batch_quartz_dashboard && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/r2dbc && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/r2dbc-auditlog && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/webflux && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/apigateway-webflux && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/blue-green/app && ./gradlew test
bash -n /Users/revy/workspace_codex/spring_sample/blue-green/scripts/lib.sh
bash -n /Users/revy/workspace_codex/spring_sample/blue-green/scripts/deploy.sh
bash -n /Users/revy/workspace_codex/spring_sample/blue-green/scripts/rollback.sh
bash -n /Users/revy/workspace_codex/spring_sample/blue-green/scripts/status.sh
cd /Users/revy/workspace_codex/spring_sample/blue-green-nginx/app && ./gradlew test
sh -n /Users/revy/workspace_codex/spring_sample/blue-green-nginx/scripts/common.sh
sh -n /Users/revy/workspace_codex/spring_sample/blue-green-nginx/scripts/deploy.sh
sh -n /Users/revy/workspace_codex/spring_sample/blue-green-nginx/scripts/rollback.sh
sh -n /Users/revy/workspace_codex/spring_sample/blue-green-nginx/scripts/status.sh
```

`sample_redisson`, `sample_batch`, `sample_rabbitmq_integration`, `sample_batch_quartz_dashboard` 일부 테스트는 Testcontainers를 사용하므로 Docker 데몬에 연결할 수 없는 환경에서는 자동 스킵될 수 있다. `r2dbc`, `r2dbc-auditlog`는 실행 시 로컬 PostgreSQL 컨테이너가 필요하지만 현재 테스트는 DB 연결 없이 통과하도록 구성했다.
