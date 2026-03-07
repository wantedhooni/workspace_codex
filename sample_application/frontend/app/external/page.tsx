"use client";

import { FormEvent, useEffect, useState } from "react";
import { api } from "../../components/api";
import { hasRole } from "../../components/auth";
import { useAuthGuard } from "../../components/useAuthGuard";

type Message = {
  id: string;
  messageType: string;
  payload: string;
  status: string;
  retryCount: number;
  lastError: string | null;
  createdAt: string;
};

export default function ExternalPage() {
  const ready = useAuthGuard();
  const isOperator = hasRole("OPERATOR");
  const [messages, setMessages] = useState<Message[]>([]);
  const [messageType, setMessageType] = useState("SETTLEMENT");
  const [payload, setPayload] = useState('{"tradeId":"T-1001","amount":120000}');
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    const data = await api<Message[]>("/api/external/messages");
    setMessages(data);
  };

  useEffect(() => {
    if (!ready) {
      return;
    }
    load().catch((err) => setError((err as Error).message));
  }, [ready]);

  const send = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      await api("/api/external/messages", {
        method: "POST",
        body: JSON.stringify({ messageType, payload })
      });
      await load();
    } catch (err) {
      setError((err as Error).message);
    }
  };

  const retry = async (id: string) => {
    setError(null);
    try {
      await api(`/api/external/messages/${id}/retry`, { method: "POST" });
      await load();
    } catch (err) {
      setError((err as Error).message);
    }
  };

  if (!ready) {
    return null;
  }

  return (
    <div className="grid">
      {isOperator && (
        <section className="panel">
          <h2>대외 전문 송신</h2>
          <form className="grid" onSubmit={send}>
            <input value={messageType} onChange={(e) => setMessageType(e.target.value)} placeholder="전문 유형" />
            <textarea value={payload} onChange={(e) => setPayload(e.target.value)} />
            <button type="submit">전송</button>
          </form>
        </section>
      )}

      {!isOperator && <section className="panel">조회 전용 계정입니다. 전송/재전송은 OPERATOR 권한이 필요합니다.</section>}

      <section className="panel">
        <h2>전문 상태</h2>
        {error && <p style={{ color: "#b91c1c" }}>{error}</p>}
        <table>
          <thead>
            <tr>
              <th>유형</th>
              <th>상태</th>
              <th>재시도</th>
              <th>오류</th>
              <th>작업</th>
            </tr>
          </thead>
          <tbody>
            {messages.map((message) => (
              <tr key={message.id}>
                <td>{message.messageType}</td>
                <td>{message.status}</td>
                <td>{message.retryCount}</td>
                <td>{message.lastError ?? "-"}</td>
                <td>
                  {isOperator ? (
                    <button className="secondary" onClick={() => retry(message.id)}>
                      재전송
                    </button>
                  ) : (
                    "-"
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
}
