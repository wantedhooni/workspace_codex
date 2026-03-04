# sample_ai_bff

내부 지식 문서를 검색하고 답변을 조합하는 AI BFF 샘플이다. 외부 LLM 없이도 동작하며, 추후 실제 LLM 게이트웨이나 RAG 계층으로 교체하기 쉽게 구성했다.

## 목적

- 내부 문서 검색형 코파일럿 구조 예시 제공
- 일반 응답과 스트리밍 응답 패턴 동시 제공
- 웹 기반 BFF에서 AI 응답 오케스트레이션 구조 설명

## 기술 스택

- Java 21
- Spring Boot 3.4
- Spring Web MVC
- SSE
- Gradle

## 주요 기능

- 질문과 태그 기반 지식 문서 검색
- 참고 문서 목록 포함 응답 생성
- `text/event-stream` 기반 토큰 스트리밍
- 샘플 지식 베이스 조회 API

## 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_ai_bff
./gradlew bootRun
```

- 기본 포트: `8080`

## API 예제

### 질의 응답

```bash
curl -X POST http://localhost:8080/api/copilot/ask \
  -H 'Content-Type: application/json' \
  -d '{"userId":"ops01","question":"해외주식 주문 장애가 나면 어떻게 대응해?","tags":["incident","ops"]}'
```

### 스트리밍 응답

```bash
curl -N -X POST http://localhost:8080/api/copilot/ask/stream \
  -H 'Content-Type: application/json' \
  -d '{"userId":"ops01","question":"배포 체크리스트를 알려줘","tags":["release"]}'
```

### 지식 문서 목록

```bash
curl http://localhost:8080/api/copilot/knowledge
```

## 핵심 구성

- `KnowledgeBaseService`: 샘플 문서 저장소와 검색 점수 계산
- `CopilotService`: 검색 결과 기반 답변/참고 문서 조합
- `CopilotController`: 일반 응답과 SSE 스트리밍 엔드포인트 제공

## 확장 방향

- 실제 LLM API 호출 계층 추가
- 벡터 검색 저장소 연동
- 사용자 권한별 문서 필터링
- 프롬프트 템플릿 분리

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample_ai_bff
./gradlew test
./gradlew build
```
