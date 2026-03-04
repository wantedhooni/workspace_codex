# sample_websocket_realtime

STOMP 기반 WebSocket 실시간 샘플이다. 가격 업데이트와 작업 진행률을 토픽으로 푸시한다.

## 목적

- Spring WebSocket/STOMP 기본 구성 예시 제공
- REST 입력과 실시간 push 를 결합하는 구조 설명
- 가격 스트림과 작업 진행률 스트림의 공통 패턴 제공

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring WebSocket
- STOMP
- Gradle

## 제공 기능

- `/topic/prices` 가격 토픽 발행
- `/topic/jobs` 작업 진행률 토픽 발행
- REST API로 테스트용 메시지 입력

## 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_websocket_realtime
./gradlew bootRun
```

- WebSocket 엔드포인트: `/ws/realtime`
- SockJS 엔드포인트: `/ws/realtime-sockjs`

## REST 예제

```bash
curl -X POST http://localhost:8080/api/realtime/prices \
  -H 'Content-Type: application/json' \
  -d '{"instrument":"AAPL","price":198.25}'
```

```bash
curl -X POST http://localhost:8080/api/realtime/jobs \
  -H 'Content-Type: application/json' \
  -d '{"jobName":"settlement-batch","progress":65,"status":"RUNNING"}'
```

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_websocket_realtime
./gradlew test
./gradlew build
```
