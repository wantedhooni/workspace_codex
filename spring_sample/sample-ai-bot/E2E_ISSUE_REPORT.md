# E2E 장애 원인 및 처리 내역

## 1. 증상

- `./scripts/e2e-test.sh` 실행 시 아래 단계에서 실패
  - `WebSocket 채팅 시나리오 검증`
- 실패 메시지
  - `WebSocket 응답 타임아웃 (90초)`

## 2. 원인 분석

### 원인 A: 시작 스크립트의 프로세스/PID 판정 불안정

- 기존 `all-start.sh`는 `gradlew bootRun` 백그라운드 PID를 기준으로 백엔드 정상 여부를 판정했다.
- `bootRun` 운용 시점에 따라 PID와 실제 리스닝 프로세스 관계가 불안정해, 서버가 떠도 실패로 판단하거나 반대로 stale 프로세스를 정상으로 오인하는 케이스가 발생했다.

### 원인 B: 포트 점유 stale 프로세스 누적

- `5173`(frontend), `8088`(backend) 포트에 이전 실행의 잔존 프로세스가 남아 재시작/헬스체크 실패를 유발했다.

### 원인 C: E2E WebSocket 단계에서 `chat.response` 미수신

- WebSocket 연결 후 `chat` 요청에 대해 90초 내 응답이 오지 않아 타임아웃이 발생한다.
- 백엔드 로그에서 종료 시점에 `ChatWebSocketHandler`의 send 과정이 인터럽트된 흔적이 있어, 종료 타이밍까지 Ollama 응답 대기 상태였을 가능성이 높다.
- 즉, 현재 E2E 실패의 직접 원인은 "WebSocket 채팅 응답 지연/미도착"이다.

## 3. 처리 완료 항목

### 3.1 Origin 정책 완화

- REST/WebSocket 모두 Origin 전체 허용으로 변경
  - `allowedOriginPatterns("*")`

### 3.2 시작 스크립트 안정화

- `all-start.sh` 개선
  - frontend/backend 포트 점유 프로세스 검사
  - 동일 프로젝트 stale 프로세스 자동 정리
  - 타 프로세스 점유 시 명확한 에러 메시지 출력
- backend 시작 방식을 `bootRun` 중심에서 `bootJar -> java -jar` 실행으로 변경해 상주 안정성 개선

### 3.3 E2E 자동화 스크립트 추가

- `scripts/e2e-test.sh` 추가
  - `all-start` 실행
  - backend/frontend 준비 확인
  - WebSocket `chat -> clear` 시나리오 검증
  - 종료 후 `all-stop` 정리

## 4. 현재 남은 이슈

- WebSocket `chat.response`가 90초 내 도착하지 않아 E2E가 실패할 수 있다.
- 단순 연결/헬스는 통과하지만, LLM 실제 응답 경로가 환경(모델 크기, 리소스, Ollama 상태)에 따라 지연된다.

## 5. 추가 해결안(권장)

1. E2E 타임아웃 환경변수화
- 예: `E2E_WS_TIMEOUT_MS=300000` (5분)
- 현재 90초 고정 값을 환경에 맞게 조정 가능하도록 개선

2. Ollama 호출 타임아웃/예외 응답 표준화
- 백엔드에서 Ollama 요청 타임아웃을 명시하고, 초과 시 `chat.error`를 즉시 반환하도록 처리

3. 모델 워밍업 단계 추가
- E2E 시작 직전 짧은 warm-up 질의를 수행해 첫 응답 지연 완화

4. 경량 모델 선택 옵션 제공
- E2E 전용으로 더 가벼운 모델을 `.env`에서 선택 가능하도록 가이드

## 6. 재현 명령

```bash
cd /Users/revy/workspace_codex/spring_sample/sample-ai-bot
./scripts/e2e-test.sh
```

