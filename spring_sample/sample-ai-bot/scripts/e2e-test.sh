#!/usr/bin/env sh
set -eu

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"

if [ -f "$ENV_FILE" ]; then
  set -a
  # shellcheck disable=SC1090
  . "$ENV_FILE"
  set +a
fi

BACKEND_PORT="${BACKEND_PORT:-8088}"
FRONTEND_PORT="${FRONTEND_PORT:-5173}"
WS_URL="${VITE_WS_URL:-ws://localhost:${BACKEND_PORT}/ws/chat}"
E2E_MESSAGE="${E2E_MESSAGE:-E2E 웹소켓 채팅 검증 메시지}"

wait_for_http() {
  name="$1"
  url="$2"
  retries="$3"

  i=0
  while [ "$i" -lt "$retries" ]; do
    if curl -fsS "$url" >/dev/null 2>&1; then
      echo "[OK] $name 준비 완료: $url"
      return 0
    fi
    i=$((i + 1))
    sleep 1
  done

  echo "[ERROR] $name 준비 실패: $url" >&2
  return 1
}

cleanup() {
  if [ "${KEEP_RUNNING_AFTER_E2E:-false}" = "true" ]; then
    echo "[INFO] KEEP_RUNNING_AFTER_E2E=true 이므로 서비스를 유지합니다."
    return 0
  fi

  echo "[INFO] E2E 종료 후 서비스 정리"
  "$ROOT_DIR/scripts/all-stop.sh" >/dev/null 2>&1 || true
}

trap cleanup EXIT INT TERM

echo "[STEP] 서비스 시작"
"$ROOT_DIR/scripts/all-start.sh"

echo "[STEP] HTTP 준비 상태 확인"
wait_for_http "Backend" "http://localhost:${BACKEND_PORT}/actuator/health" 30
wait_for_http "Frontend" "http://localhost:${FRONTEND_PORT}" 30

echo "[STEP] WebSocket 채팅 시나리오 검증"
WS_URL="$WS_URL" E2E_MESSAGE="$E2E_MESSAGE" node <<'NODE'
const wsUrl = process.env.WS_URL;
const message = process.env.E2E_MESSAGE;

if (!wsUrl) {
  console.error('[ERROR] WS_URL이 비어 있습니다.');
  process.exit(1);
}

const socket = new WebSocket(wsUrl);
let finished = false;
let sessionId;

const done = (ok, detail) => {
  if (finished) {
    return;
  }
  finished = true;
  clearTimeout(timeout);
  try {
    socket.close();
  } catch {
    // noop
  }
  if (ok) {
    console.log(`[OK] ${detail}`);
    process.exit(0);
  }
  console.error(`[ERROR] ${detail}`);
  process.exit(1);
};

const timeout = setTimeout(() => {
  done(false, 'WebSocket 응답 타임아웃 (90초)');
}, 90000);

socket.addEventListener('open', () => {
  socket.send(JSON.stringify({
    type: 'chat',
    message
  }));
});

socket.addEventListener('message', (event) => {
  let payload;
  try {
    payload = JSON.parse(event.data);
  } catch {
    done(false, 'JSON 파싱 실패');
    return;
  }

  if (payload.type === 'connected' || payload.type === 'pong') {
    return;
  }

  if (payload.type === 'chat.error') {
    done(false, `chat.error 수신: ${payload.error ?? 'unknown error'}`);
    return;
  }

  if (payload.type === 'chat.response') {
    if (!payload.answer || typeof payload.answer !== 'string' || payload.answer.trim().length === 0) {
      done(false, 'chat.response의 answer가 비어 있습니다.');
      return;
    }
    if (!payload.sessionId || typeof payload.sessionId !== 'string') {
      done(false, 'chat.response의 sessionId가 없습니다.');
      return;
    }

    sessionId = payload.sessionId;
    socket.send(JSON.stringify({
      type: 'clear',
      sessionId
    }));
    return;
  }

  if (payload.type === 'chat.cleared') {
    if (!sessionId) {
      done(false, '세션 초기화 응답 전에 sessionId가 설정되지 않았습니다.');
      return;
    }
    if (payload.sessionId !== sessionId) {
      done(false, `chat.cleared sessionId 불일치 (${payload.sessionId} != ${sessionId})`);
      return;
    }
    done(true, 'WebSocket chat/clear E2E 통과');
    return;
  }
});

socket.addEventListener('error', () => {
  done(false, 'WebSocket 연결 오류');
});

socket.addEventListener('close', () => {
  if (!finished) {
    done(false, 'WebSocket이 조기 종료되었습니다.');
  }
});
NODE

echo "[DONE] E2E 테스트 통과"
