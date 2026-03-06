import { FormEvent, KeyboardEvent, useEffect, useMemo, useRef, useState } from 'react';

type Role = 'user' | 'assistant';

type UiMessage = {
  id: string;
  role: Role;
  content: string;
};

type SocketState = 'connecting' | 'connected' | 'disconnected';

type ChatSocketResponse = {
  type: string;
  sessionId?: string;
  answer?: string;
  respondedAt?: string;
  historySize?: number;
  cleared?: boolean;
  error?: string;
};

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8088';
const socketUrl = import.meta.env.VITE_WS_URL ?? `${apiBaseUrl.replace(/^http/, 'ws')}/ws/chat`;

function App() {
  const [sessionId, setSessionId] = useState('');
  const [input, setInput] = useState('');
  const [messages, setMessages] = useState<UiMessage[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [widgetOpen, setWidgetOpen] = useState(true);
  const [isComposing, setIsComposing] = useState(false);
  const [socketState, setSocketState] = useState<SocketState>('connecting');

  const socketRef = useRef<WebSocket | null>(null);

  const canSubmit = useMemo(
    () => input.trim().length > 0 && !loading && socketState === 'connected',
    [input, loading, socketState]
  );

  useEffect(() => {
    const socket = new WebSocket(socketUrl);
    socketRef.current = socket;
    setSocketState('connecting');

    socket.onopen = () => {
      setSocketState('connected');
      setError('');
    };

    socket.onmessage = (event) => {
      let payload: ChatSocketResponse;
      try {
        payload = JSON.parse(event.data as string) as ChatSocketResponse;
      } catch {
        setLoading(false);
        setError('WebSocket 응답 형식을 해석할 수 없습니다.');
        return;
      }

      if (payload.type === 'chat.response') {
        setLoading(false);
        if (payload.sessionId) {
          setSessionId(payload.sessionId);
        }
        const answer = payload.answer;
        if (answer) {
          setMessages((prev) => [
            ...prev,
            {
              id: crypto.randomUUID(),
              role: 'assistant',
              content: answer
            }
          ]);
        }
        return;
      }

      if (payload.type === 'chat.cleared') {
        setLoading(false);
        setSessionId('');
        return;
      }

      if (payload.type === 'chat.error') {
        setLoading(false);
        setError(payload.error ?? '채팅 요청 처리 중 오류가 발생했습니다.');
      }
    };

    socket.onerror = () => {
      setSocketState('disconnected');
    };

    socket.onclose = () => {
      setSocketState('disconnected');
      setLoading(false);
      if (socketRef.current === socket) {
        socketRef.current = null;
      }
    };

    return () => {
      socket.onopen = null;
      socket.onmessage = null;
      socket.onerror = null;
      socket.onclose = null;
      socket.close();
      if (socketRef.current === socket) {
        socketRef.current = null;
      }
    };
  }, []);

  const sendSocketMessage = (payload: object) => {
    const socket = socketRef.current;
    if (!socket || socket.readyState !== WebSocket.OPEN) {
      throw new Error('WebSocket 연결이 끊어졌습니다. 새로고침 후 다시 시도하세요.');
    }
    socket.send(JSON.stringify(payload));
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const text = input.trim();
    if (!text || loading) {
      return;
    }
    setWidgetOpen(true);

    setInput('');
    setError('');
    setLoading(true);
    const userMessage: UiMessage = {
      id: crypto.randomUUID(),
      role: 'user',
      content: text
    };
    setMessages((prev) => [...prev, userMessage]);

    try {
      sendSocketMessage({
        type: 'chat',
        sessionId: sessionId || undefined,
        message: text
      });
    } catch (exception) {
      setLoading(false);
      const message = exception instanceof Error ? exception.message : '알 수 없는 오류가 발생했습니다.';
      setError(message);
    }
  };

  const clearSession = () => {
    setMessages([]);
    setError('');

    if (!sessionId) {
      return;
    }

    try {
      sendSocketMessage({
        type: 'clear',
        sessionId
      });
    } catch (exception) {
      const message = exception instanceof Error ? exception.message : '세션 초기화 중 오류가 발생했습니다.';
      setError(message);
    }
  };

  const handleComposerKeyDown = (event: KeyboardEvent<HTMLTextAreaElement>) => {
    if (isComposing || event.nativeEvent.isComposing) {
      return;
    }

    if (event.key !== 'Enter' || event.shiftKey) {
      return;
    }

    event.preventDefault();
    if (!canSubmit) {
      return;
    }

    event.currentTarget.form?.requestSubmit();
  };

  const socketStatusText =
    socketState === 'connected' ? 'WebSocket 연결됨' : socketState === 'connecting' ? 'WebSocket 연결 중' : 'WebSocket 끊김';

  return (
    <div className="page-shell">
      <main className="content">
        <h1>Sample AI Bot Widget</h1>
        <p>오른쪽 사이드 위젯에서 Ollama 기반 챗봇을 사용할 수 있습니다.</p>
      </main>

      <button
        type="button"
        className="widget-launcher"
        onClick={() => setWidgetOpen((prev) => !prev)}
      >
        {widgetOpen ? '챗봇 닫기' : '챗봇 열기'}
        {!widgetOpen && messages.length > 0 ? <span className="widget-badge">{messages.length}</span> : null}
      </button>

      <aside className={`chat-widget ${widgetOpen ? 'open' : 'closed'}`}>
        <header className="widget-header">
          <div>
            <h2>AI Bot</h2>
            <p>{socketStatusText}</p>
          </div>
          <button className="text-button" type="button" onClick={() => setWidgetOpen(false)}>
            접기
          </button>
        </header>

        <section className="chat-log">
          {messages.length === 0 ? (
            <div className="empty">메시지를 입력하면 대화가 시작됩니다.</div>
          ) : (
            messages.map((message) => (
              <article key={message.id} className={`bubble ${message.role}`}>
                <h3>{message.role === 'user' ? '나' : 'AI'}</h3>
                <p>{message.content}</p>
              </article>
            ))
          )}
        </section>

        <form className="composer" onSubmit={handleSubmit}>
          <textarea
            value={input}
            onChange={(event) => setInput(event.target.value)}
            onKeyDown={handleComposerKeyDown}
            onCompositionStart={() => setIsComposing(true)}
            onCompositionEnd={() => setIsComposing(false)}
            placeholder="메시지를 입력하세요"
            rows={3}
          />
          <div className="actions">
            <button className="secondary" type="button" onClick={clearSession}>
              세션 초기화
            </button>
            <button type="submit" disabled={!canSubmit}>
              {loading ? '응답 생성 중...' : '전송'}
            </button>
          </div>
        </form>

        {error ? <p className="error">오류: {error}</p> : null}
      </aside>
    </div>
  );
}

export default App;
