# sample_mcp_server

Spring AI MCP Server 샘플이다. 운영 체크리스트와 사고 보고 초안을 MCP tool 로 노출한다.

## 목적

- Spring AI MCP Server 기본 구성 예시 제공
- Tool 노출 방식 설명
- 운영형 내부 도구 서버의 최소 구조 제공

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring AI MCP Server
- Spring Web MVC
- Gradle

## 제공 기능

- `releaseChecklist`: 배포 체크리스트 도구
- `incidentDraft`: 사고 보고 초안 생성 도구

## 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_mcp_server
./gradlew bootRun
```

- 기본 포트: `8090`

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_mcp_server
./gradlew test
./gradlew build
```
