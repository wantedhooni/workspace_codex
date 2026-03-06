# backend

로컬 Ollama를 호출하는 Spring Boot 백엔드다.

## WebSocket

- 엔드포인트: `ws://localhost:8088/ws/chat`
- 메시지 형식
  - 채팅 요청: `{"type":"chat","sessionId":"...","message":"..."}` (`sessionId` 생략 가능)
  - 세션 초기화: `{"type":"clear","sessionId":"..."}`

## 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample-ai-bot/backend
./gradlew bootRun
```

## 테스트/빌드

```bash
./gradlew test
./gradlew build
```
