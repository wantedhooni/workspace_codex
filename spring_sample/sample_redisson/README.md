# sample_redisson

Redisson을 Spring Boot 애플리케이션에 연결해 Redis 기반 자료구조와 분산 락을 다루는 예제다. 실무에서 자주 보는 세 가지 패턴을 최소 구조로 분리해 두었다.

## 포함된 예제

- 키-값 저장: TTL 포함 단건 저장과 조회
- 원자 카운터: Redis `RAtomicLong` 기반 시퀀스 증가
- 분산 락: 동일 키 기준 순차 실행 보장

## 기술 구성

- Spring Boot 3.4
- Redisson
- Redis 7
- Gradle

## 요구 사항

- Java 21
- Docker

## 실행

### 1. Redis 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_redisson
docker compose up -d
```

### 2. 애플리케이션 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_redisson
./gradlew bootRun
```

기본 Redis 접속 주소는 `redis://localhost:6379` 이고, 애플리케이션 기본 포트는 `8080` 이다.

## 주요 클래스

- [SampleRedissonApplication.java](/Users/revy/workspace_codex/spring_sample/sample_redisson/src/main/java/com/example/sampleredisson/SampleRedissonApplication.java): 애플리케이션 시작점
- [RedissonConfig.java](/Users/revy/workspace_codex/spring_sample/sample_redisson/src/main/java/com/example/sampleredisson/config/RedissonConfig.java): Redisson 단일 노드 클라이언트 설정
- [KeyValueService.java](/Users/revy/workspace_codex/spring_sample/sample_redisson/src/main/java/com/example/sampleredisson/kv/KeyValueService.java): TTL 기반 키-값 저장/조회
- [CounterService.java](/Users/revy/workspace_codex/spring_sample/sample_redisson/src/main/java/com/example/sampleredisson/counter/CounterService.java): 원자 카운터 처리
- [LockDemoService.java](/Users/revy/workspace_codex/spring_sample/sample_redisson/src/main/java/com/example/sampleredisson/lock/LockDemoService.java): 분산 락 보호 구간 실행

## 설정

- `app.redisson.address`: Redis 접속 주소
- `app.redisson.password`: 비밀번호가 필요한 경우 사용
- `app.redisson.connect-timeout`: Redis 연결 타임아웃(ms)
- `app.redisson.operation-timeout`: Redis 명령 타임아웃(ms)

환경 변수 예시:

```bash
APP_REDISSON_ADDRESS=redis://127.0.0.1:6379
APP_REDISSON_PASSWORD=
```

## API 예제

### 1. 키-값 저장

```bash
curl -X PUT http://localhost:8080/api/kv/order:1001 \
  -H 'Content-Type: application/json' \
  -d '{"value":"READY","ttlSeconds":300}'
```

응답 예시:

```json
{"key":"order:1001","value":"READY","ttlSeconds":300}
```

### 2. 키-값 조회

```bash
curl http://localhost:8080/api/kv/order:1001
```

### 3. 원자 카운터 증가

```bash
curl -X POST "http://localhost:8080/api/counters/trade-seq/increment?delta=5"
```

### 4. 원자 카운터 조회

```bash
curl http://localhost:8080/api/counters/trade-seq
```

### 5. 분산 락 실행

```bash
curl -X POST http://localhost:8080/api/locks/daily-settlement/execute \
  -H 'Content-Type: application/json' \
  -d '{"waitMillis":500,"leaseMillis":3000,"processingMillis":200}'
```

이 API는 같은 락 이름으로 동시에 요청이 들어와도 한 번에 하나만 보호 구간을 통과하도록 만든 예제다.

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_redisson
./gradlew test
./gradlew build
```

테스트는 Testcontainers Redis를 사용한다. Docker 데몬이 없으면 통합 테스트는 자동 스킵될 수 있다.
