# spring_sample

Spring 인프라 연동 예제를 모아둔 샘플 작업공간이다. 각 프로젝트는 독립 실행형 Spring Boot 애플리케이션이며, 로컬 Docker 인프라를 기준으로 바로 실행할 수 있게 구성했다.

## 프로젝트 목록

| 프로젝트 | 설명 | 주요 기술 |
| --- | --- | --- |
| `sample_redisson` | Redis를 이용한 키-값 저장, 원자 카운터, 분산 락 샘플 | Spring Boot, Redisson, Redis |
| `sample_batch` | DB 기반 Spring Batch와 Quartz를 함께 사용하는 대용량 처리 샘플 | Spring Boot, Spring Batch, Quartz, PostgreSQL |

## 공통 요구 사항

- Java 21
- Docker
- macOS 또는 Linux 기준 쉘 환경

## 빠른 시작

### 1. Redis 샘플 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_redisson
docker compose up -d
./gradlew bootRun
```

### 2. Batch + Quartz 샘플 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_batch
docker compose up -d
./gradlew bootRun
```

두 프로젝트 모두 기본 HTTP 포트가 `8080` 이므로 동시에 실행하려면 하나의 프로젝트에서 `server.port` 를 변경해야 한다.

예시:

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_batch
./gradlew bootRun --args='--server.port=8081'
```

## 문서 위치

- Redis 샘플 상세 문서: [sample_redisson/README.md](/Users/revy/workspace_codex/spring_sample/sample_redisson/README.md)
- Batch 샘플 상세 문서: [sample_batch/README.md](/Users/revy/workspace_codex/spring_sample/sample_batch/README.md)

## 검증 명령

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_redisson && ./gradlew test
cd /Users/revy/workspace_codex/spring_sample/sample_batch && ./gradlew test
```

Testcontainers 기반 통합 테스트이므로 Docker 데몬에 연결할 수 없는 환경에서는 일부 테스트가 자동 스킵될 수 있다.
