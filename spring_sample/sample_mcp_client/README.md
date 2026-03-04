# sample_mcp_client

Spring AI MCP Client 샘플이다. 별도 MCP 서버에 연결해 도구 목록 조회, 도구 호출, 리소스 조회를 REST API로 감싸 보여준다.

## 목적

- Spring AI MCP Client 구성 예시 제공
- 서버와 별도 프로세스로 MCP 호출 흐름 설명
- 내부 BFF에서 MCP 도구를 감싸는 패턴 제공

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring AI MCP Client
- Spring Web MVC
- Gradle

## 제공 기능

- MCP 도구 목록 조회
- MCP 도구 호출
- MCP 리소스 조회

## 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_mcp_server
./gradlew bootRun
```

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_mcp_client
./gradlew bootRun
```

- MCP 서버 포트: `8090`
- MCP 클라이언트 포트: `8091`

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_mcp_client
./gradlew test
./gradlew build
```
