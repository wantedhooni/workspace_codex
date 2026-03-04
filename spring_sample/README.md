# spring_sample

실무에서 참고하기 좋은 Spring Boot 샘플 프로젝트를 주제별로 모아둔 작업공간이다. 각 폴더는 독립 실행형 프로젝트이며, 웹/API, 인프라 연동, 배치, 이벤트 드리븐, 모듈형 설계까지 나눠서 볼 수 있게 구성했다.

## 프로젝트 목록

| 프로젝트 | 설명 | 주요 기술 |
| --- | --- | --- |
| `sample_redisson` | Redis 기반 키-값 저장, 원자 카운터, 분산 락 샘플 | Spring Boot, Redisson, Redis |
| `sample_batch` | DB 기반 Spring Batch + Quartz 대용량 처리 샘플 | Spring Boot, Spring Batch, Quartz, PostgreSQL |
| `sample_ai_bff` | 내부 지식 문서를 검색하고 답변을 조합하는 AI BFF 샘플 | Spring Boot, Web, SSE |
| `sample_secure_bff` | 인증/인가와 API composition을 적용한 Secure BFF 샘플 | Spring Boot, Security, RestClient, HTTP Interface |
| `sample_observability_api` | 메트릭과 observation 중심의 운영형 REST API 샘플 | Spring Boot, Actuator, Micrometer, Prometheus |
| `sample_modular_monolith` | 주문, 재고, 청구를 모듈 단위로 나눈 업무 시스템 샘플 | Spring Boot, JPA, Domain Event, H2 |
| `sample_event_driven` | 주문 생성, outbox, Kafka 발행, 결제 결과 반영 흐름 샘플 | Spring Boot, JPA, Kafka, PostgreSQL |

## 공통 요구 사항

- Java 21
- Docker
- macOS 또는 Linux 기준 쉘 환경

## 빠른 시작

### 1. Redis 샘플

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_redisson
docker compose up -d
./gradlew bootRun
```

### 2. Batch + Quartz 샘플

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_batch
docker compose up -d
./gradlew bootRun
```

### 3. Event-Driven 샘플

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_event_driven
docker compose up -d
./gradlew bootRun
```

### 4. 나머지 웹 샘플

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_ai_bff && ./gradlew bootRun
cd /Users/revy/workspace_codex/spring_sample/sample_secure_bff && ./gradlew bootRun --args='--server.port=8081'
cd /Users/revy/workspace_codex/spring_sample/sample_observability_api && ./gradlew bootRun --args='--server.port=8082'
cd /Users/revy/workspace_codex/spring_sample/sample_modular_monolith && ./gradlew bootRun --args='--server.port=8083'
```

기본 HTTP 포트는 대부분 `8080` 이므로 동시에 실행하려면 `server.port` 를 조정해야 한다.

## 문서 위치

- [sample_redisson/README.md](/Users/revy/workspace_codex/spring_sample/sample_redisson/README.md)
- [sample_batch/README.md](/Users/revy/workspace_codex/spring_sample/sample_batch/README.md)
- [sample_ai_bff/README.md](/Users/revy/workspace_codex/spring_sample/sample_ai_bff/README.md)
- [sample_secure_bff/README.md](/Users/revy/workspace_codex/spring_sample/sample_secure_bff/README.md)
- [sample_observability_api/README.md](/Users/revy/workspace_codex/spring_sample/sample_observability_api/README.md)
- [sample_modular_monolith/README.md](/Users/revy/workspace_codex/spring_sample/sample_modular_monolith/README.md)
- [sample_event_driven/README.md](/Users/revy/workspace_codex/spring_sample/sample_event_driven/README.md)

## 검증 명령

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_redisson && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_batch && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_ai_bff && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_secure_bff && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_observability_api && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_modular_monolith && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_event_driven && ./gradlew test
```

`sample_redisson`, `sample_batch` 일부 테스트는 Testcontainers를 사용하므로 Docker 데몬에 연결할 수 없는 환경에서는 자동 스킵될 수 있다.
