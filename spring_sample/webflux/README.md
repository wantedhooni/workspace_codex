# webflux

WebFlux 기반 운영 대시보드 API 샘플이다. 여러 비동기 소스를 조합해 대시보드 응답을 만들고, SSE 스트림으로 상태 이벤트를 지속적으로 전달하는 흐름을 담았다.

## 목적

- WebFlux에서 `Mono.zip`과 `Flux` 스트림을 이용한 조합형 API 예시 제공
- REST 조회와 SSE push를 같은 서비스 레이어에서 다루는 패턴 설명
- 블로킹 없이 지연 시간이 다른 백엔드 응답을 합성하는 구조 제공

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring WebFlux
- Reactor
- Gradle

## 제공 기능

- 운영 도메인별 대시보드 요약 조회
- 운영 이벤트 SSE 스트림 조회
- 지연 시간 기준 SLA 충족 여부 계산

## 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/webflux
./gradlew bootRun
```

- 애플리케이션 포트: `8083`

## API 예제

```bash
curl 'http://localhost:8083/api/operations/dashboard?domain=payments'
```

```bash
curl -N 'http://localhost:8083/api/operations/events/stream?domain=payments'
```

```bash
curl -X POST http://localhost:8083/api/operations/latency-check \
  -H 'Content-Type: application/json' \
  -d '{
    "domain": "payments",
    "thresholdMillis": 140
  }'
```

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/webflux
./gradlew test
./gradlew build
```
