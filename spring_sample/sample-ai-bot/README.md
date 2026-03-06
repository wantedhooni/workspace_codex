# sample-ai-bot

로컬 Ollama를 `docker-compose`로 구동하고, `backend(Spring Boot)` + `frontend(React)`로 채팅 위젯을 제공하는 샘플 프로젝트다.

## 목적

- Ollama를 컨테이너로 운영하는 실무형 로컬 개발 흐름 제공
- `backend`, `frontend`, `docker-compose` 설정을 `.env` 기반으로 일원화
- 우측 사이드 위젯 형태의 챗봇 UI 예시 제공

## 디렉터리 구조

```text
sample-ai-bot
├── backend         # Spring Boot API 서버
├── frontend        # Vite + React 채팅 위젯 UI
├── scripts         # 전체 시작/중지 스크립트
├── docker-compose.yml
├── .env            # 실행/모델/포트 기본값
└── runtime         # PID/로그 파일
```

## 환경 변수(.env)

기본값은 `llama3.2`로 설정되어 있으며, 아래 값은 모두 `.env`에서 변경 가능하다.

```env
OLLAMA_IMAGE=ollama/ollama:latest
OLLAMA_CONTAINER_NAME=sample-ai-bot-ollama
OLLAMA_PORT=21434
OLLAMA_MODEL=llama3.2:1b
OLLAMA_NUM_PARALLEL=1
OLLAMA_MAX_LOADED_MODELS=1
OLLAMA_WARMUP=true
OLLAMA_WARMUP_PROMPT=안녕

BACKEND_PORT=8088
FRONTEND_PORT=5173
OLLAMA_BASE_URL=http://localhost:21434
OLLAMA_MAX_HISTORY=6
OLLAMA_TEMPERATURE=0.2
OLLAMA_NUM_PREDICT=128
OLLAMA_KEEP_ALIVE=30m
OLLAMA_REQUEST_TIMEOUT_SECONDS=180
VITE_API_BASE_URL=http://localhost:8088
VITE_WS_URL=ws://localhost:8088/ws/chat
```

- 모델은 프론트 입력 없이 백엔드에서 `OLLAMA_MODEL`로 관리한다.
- 속도 튜닝 핵심
  - `OLLAMA_MODEL`: 더 작은 모델일수록 빠름 (예: `llama3.2:1b`)
  - `OLLAMA_NUM_PREDICT`: 생성 토큰 수 제한 (작을수록 빠름)
  - `OLLAMA_MAX_HISTORY`: 대화 이력 컨텍스트 크기 (작을수록 빠름)

- 샘플 파일: `.env.example`
- 현재 프로젝트는 `.env`를 바로 읽어 실행한다.

## Ollama docker-compose 실행

```bash
cd /Users/revy/workspace_codex/spring_sample/sample-ai-bot
docker compose --env-file .env up -d ollama
```

- `OLLAMA_MODEL` 변경 시 `scripts/all-start.sh` 실행 중 자동 `ollama pull` 수행
- 수동으로 모델 다운로드하려면:

```bash
docker compose --env-file .env exec -T ollama ollama pull llama3.2
```

## 전체 시작/중지 스크립트

### 전체 시작

```bash
cd /Users/revy/workspace_codex/spring_sample/sample-ai-bot
./scripts/all-start.sh
```

- 수행 내용
  - docker-compose로 Ollama 컨테이너 시작
  - `.env`의 `OLLAMA_MODEL` 자동 pull
  - Backend 기동 + 헬스체크
  - Frontend 기동

### 전체 중지

```bash
cd /Users/revy/workspace_codex/spring_sample/sample-ai-bot
./scripts/all-stop.sh
```

- Frontend/Backend 프로세스 종료
- Ollama 컨테이너 `docker compose stop ollama` 실행

## 개별 실행

### 백엔드

```bash
cd /Users/revy/workspace_codex/spring_sample/sample-ai-bot/backend
./gradlew bootRun
```

### 프론트엔드

```bash
cd /Users/revy/workspace_codex/spring_sample/sample-ai-bot/frontend
npm install
npm run dev
```

## WebSocket 예시

```bash
wscat -c ws://localhost:8088/ws/chat
# 연결 후 메시지 전송
> {"type":"chat","message":"오늘 해야 할 일을 3개로 정리해줘"}
# 세션 초기화
> {"type":"clear","sessionId":"<session-id>"}
```

## WebSocket 동작 방식

1. 클라이언트가 `ws://localhost:8088/ws/chat`에 연결한다.
2. 서버는 연결 직후 `type=connected` 메시지를 보낸다.
3. 클라이언트는 `type=chat` 메시지로 사용자 질문을 전송한다.
4. 서버는 Ollama 호출 결과를 `type=chat.response`로 반환한다.
5. 대화 초기화가 필요하면 `type=clear`를 전송하고, 서버는 `type=chat.cleared`를 반환한다.

요청 메시지 형식:
- `chat`: `{"type":"chat","sessionId":"선택","message":"질문"}`
- `clear`: `{"type":"clear","sessionId":"초기화할 세션 ID"}`
- `ping`: `{"type":"ping"}` (헬스체크 용도)

응답 메시지 형식:
- `connected`: 소켓 연결 성공 알림
- `chat.response`: `sessionId`, `answer`, `historySize` 포함
- `chat.cleared`: 세션 초기화 성공 여부(`cleared`) 포함
- `chat.error`: 처리 실패 시 에러 메시지(`error`) 포함
- `pong`: `ping` 요청에 대한 응답

세션 처리 규칙:
- 첫 `chat` 요청에서 `sessionId`를 비우면 서버가 세션 ID를 생성한다.
- 이후 응답으로 받은 `sessionId`를 같은 대화에서 재사용하면 이력이 누적된다.
- `clear` 요청 시 해당 세션 메모리가 초기화된다.

## 로그/PID

- 로그: `runtime/logs`
- PID: `runtime/pids`

## 검증

```bash
cd /Users/revy/workspace_codex/spring_sample/sample-ai-bot/backend
./gradlew test
./gradlew build

cd /Users/revy/workspace_codex/spring_sample/sample-ai-bot/frontend
npm run build

cd /Users/revy/workspace_codex/spring_sample/sample-ai-bot
sh -n scripts/all-start.sh
sh -n scripts/all-stop.sh
docker compose --env-file .env config
sh -n scripts/e2e-test.sh

# 실제 E2E (서비스 시작/검증/정리 포함)
./scripts/e2e-test.sh
```
