# sample_redisson

Redisson을 Spring Boot 애플리케이션에 연결해 Redis 기반 자료구조와 분산 락을 다루는 샘플이다. 실무에서 자주 쓰는 Redis 활용 패턴을 최소 단위로 나눠 두었다.

## 목적

- Redis 키-값 저장과 TTL 처리 예시 제공
- 원자 카운터와 분산 락 사용법 설명
- Redisson 단일 노드 설정과 서비스 분리 구조 제공

## 기술 스택

- Java 21
- Spring Boot 3.4
- Redisson
- Redis 7
- Gradle

## 주요 기능

- TTL 포함 키-값 저장과 조회
- `RAtomicLong` 기반 시퀀스 증가
- `RLock` 기반 분산 락 보호 구간 실행

## 실행

### Redis 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_redisson
docker compose up -d
```

### 애플리케이션 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_redisson
./gradlew bootRun
```

- 기본 포트: `8080`
- Redis 주소 기본값: `redis://localhost:6379`

## API 예제

### 키-값 저장

```bash
curl -X PUT http://localhost:8080/api/kv/order:1001 \
  -H 'Content-Type: application/json' \
  -d '{"value":"READY","ttlSeconds":300}'
```

### 키-값 조회

```bash
curl http://localhost:8080/api/kv/order:1001
```

### 카운터 증가

```bash
curl -X POST "http://localhost:8080/api/counters/trade-seq/increment?delta=5"
```

### 카운터 조회

```bash
curl http://localhost:8080/api/counters/trade-seq
```

### 분산 락 실행

```bash
curl -X POST http://localhost:8080/api/locks/daily-settlement/execute \
  -H 'Content-Type: application/json' \
  -d '{"waitMillis":500,"leaseMillis":3000,"processingMillis":200}'
```

## 주요 설정

- `app.redisson.address`
- `app.redisson.password`
- `app.redisson.connect-timeout`
- `app.redisson.operation-timeout`

## 핵심 구성

- `RedissonConfig`: RedissonClient 생성
- `KeyValueService`: TTL 저장/조회
- `CounterService`: 카운터 증가
- `LockDemoService`: 락 보호 구간 실행

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_redisson
./gradlew test
./gradlew build
```

Testcontainers Redis를 사용하므로 Docker 데몬이 없으면 일부 통합 테스트가 자동 스킵될 수 있다.
