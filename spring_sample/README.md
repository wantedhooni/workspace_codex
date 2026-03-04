# spring_sample

Spring Boot 기반 실전형 샘플 프로젝트 모음이다. 각 폴더는 독립 실행형 프로젝트이며, 웹/API, 보안, AI, 배치, 이벤트 드리븐, 실시간 통신, 게이트웨이, Native Image까지 주제별로 분리했다.

## 프로젝트 지도

### 웹 / BFF / API

| 프로젝트 | 설명 | 주요 기술 |
| --- | --- | --- |
| `sample_ai_bff` | 내부 지식 문서를 검색하고 답변을 조합하는 AI BFF 샘플 | Spring Boot, Web, SSE |
| `sample_secure_bff` | 인증/인가와 API composition을 적용한 Secure BFF 샘플 | Spring Boot, Security, RestClient, HTTP Interface |
| `sample_observability_api` | 메트릭과 observation 중심의 운영형 REST API 샘플 | Spring Boot, Actuator, Micrometer, Prometheus |
| `sample_websocket_realtime` | STOMP 기반 가격/작업 진행률 실시간 전송 샘플 | Spring Boot, WebSocket, STOMP |
| `sample_gateway_observability` | Gateway 라우팅과 메트릭 노출을 결합한 샘플 | Spring Boot, Spring Cloud Gateway, Prometheus |

### 데이터 / 인프라 / 배치

| 프로젝트 | 설명 | 주요 기술 |
| --- | --- | --- |
| `sample_redisson` | Redis 기반 키-값 저장, 원자 카운터, 분산 락 샘플 | Spring Boot, Redisson, Redis |
| `sample_batch` | DB 기반 Spring Batch + Quartz 대용량 처리 샘플 | Spring Boot, Spring Batch, Quartz, PostgreSQL |
| `sample_native_image` | GraalVM Native Image 빌드 준비 샘플 | Spring Boot, AOT, RuntimeHints |

### 아키텍처 / 메시징 / AI Tooling

| 프로젝트 | 설명 | 주요 기술 |
| --- | --- | --- |
| `sample_modular_monolith` | 주문, 재고, 청구를 모듈 단위로 나눈 업무 시스템 샘플 | Spring Boot, JPA, Domain Event, H2 |
| `sample_event_driven` | 주문 생성, outbox, Kafka 발행, 결제 결과 반영 흐름 샘플 | Spring Boot, JPA, Kafka, PostgreSQL |
| `sample_mcp_server` | 운영 도구를 MCP Tool 로 노출하는 샘플 | Spring Boot, Spring AI MCP Server |
| `sample_mcp_client` | MCP 서버에 연결해 tool 호출을 감싸는 샘플 | Spring Boot, Spring AI MCP Client |

## 공통 요구 사항

- Java 21
- Docker
- macOS 또는 Linux 기준 쉘 환경

## 빠른 시작

### 인프라가 필요한 프로젝트

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_redisson && docker compose up -d
cd /Users/revy/workspace_codex/spring_sample/sample_batch && docker compose up -d
cd /Users/revy/workspace_codex/spring_sample/sample_event_driven && docker compose up -d
```

### 바로 실행 가능한 대표 샘플

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_ai_bff && ./gradlew bootRun
cd /Users/revy/workspace_codex/spring_sample/sample_secure_bff && ./gradlew bootRun --args='--server.port=8081'
cd /Users/revy/workspace_codex/spring_sample/sample_websocket_realtime && ./gradlew bootRun --args='--server.port=8082'
```

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
| `sample_mcp_server` | `8090` |
| `sample_mcp_client` | `8091` |

여러 프로젝트를 동시에 실행하려면 `--server.port=` 로 포트를 조정해야 한다.

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
```

`sample_redisson`, `sample_batch` 일부 테스트는 Testcontainers를 사용하므로 Docker 데몬에 연결할 수 없는 환경에서는 자동 스킵될 수 있다.
