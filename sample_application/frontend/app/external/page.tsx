"use client";

import { FormEvent, useEffect, useState } from "react";
import { api } from "../../components/api";
import { hasRole } from "../../components/auth";
import Modal from "../../components/Modal";
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
  const [sendModalOpen, setSendModalOpen] = useState(false);

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
      setSendModalOpen(false);
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
    <div className="page">
      <div>
        <h2 className="page-title">대외 전문 업무</h2>
        <p className="page-desc">외부기관 송수신 전문 상태를 모니터링하고 실패 건을 재전송합니다.</p>
      </div>

      {!isOperator && <div className="info">VIEWER 권한입니다. 송신/재전송은 OPERATOR만 수행 가능합니다.</div>}
      {error && <div className="alert">{error}</div>}

      <section className="kpi-grid">
        <article className="kpi-card">
          <div className="kpi-label">전문 총건수</div>
          <div className="kpi-value">{messages.length}</div>
        </article>
        <article className="kpi-card">
          <div className="kpi-label">실패 건수</div>
          <div className="kpi-value">{messages.filter((message) => message.status === "FAILED").length}</div>
        </article>
        <article className="kpi-card">
          <div className="kpi-label">재시도 누적</div>
          <div className="kpi-value">{messages.reduce((sum, message) => sum + message.retryCount, 0)}</div>
        </article>
        <article className="kpi-card">
          <div className="kpi-label">처리 권한</div>
          <div className="kpi-value">{isOperator ? "OPR" : "VIEW"}</div>
        </article>
      </section>

      {isOperator && (
        <section className="panel">
          <div className="panel-head">
            <h2>전문 송신</h2>
            <button onClick={() => setSendModalOpen(true)} type="button">
              전문 송신 열기
            </button>
          </div>
          <p className="panel-sub">전문 유형과 payload를 확인한 뒤 외부기관 송신을 수행합니다.</p>
          <div className="info">전문 생성은 모달에서 수행하고, 본문은 송신 결과와 재시도 상태를 확인합니다.</div>
        </section>
      )}

      <Modal onClose={() => setSendModalOpen(false)} open={sendModalOpen} title="대외 전문 송신">
        <form className="form-grid" onSubmit={send}>
          <input value={messageType} onChange={(e) => setMessageType(e.target.value)} placeholder="전문 유형" />
          <button type="submit">전송</button>
          <textarea style={{ gridColumn: "1 / -1" }} value={payload} onChange={(e) => setPayload(e.target.value)} />
        </form>
      </Modal>

      <section className="panel">
        <div className="panel-head">
          <h2>전문 상태</h2>
          <button className="secondary" onClick={() => load().catch((err) => setError((err as Error).message))}>
            새로고침
          </button>
        </div>
        <p className="panel-sub">실패 상태와 재시도 횟수를 우선 확인해 대외기관 정산 지연을 방지합니다.</p>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>유형</th>
                <th>상태</th>
                <th>재시도</th>
                <th>오류</th>
                <th>생성시각</th>
                <th>작업</th>
              </tr>
            </thead>
            <tbody>
              {messages.map((message) => (
                <tr key={message.id}>
                  <td>{message.messageType}</td>
                  <td>
                    <span
                      className={
                        message.status === "SENT"
                          ? "badge badge-ok"
                          : message.status === "FAILED"
                          ? "badge badge-danger"
                          : "badge badge-warn"
                      }
                    >
                      {message.status}
                    </span>
                  </td>
                  <td>{message.retryCount}</td>
                  <td>{message.lastError ?? "-"}</td>
                  <td>{new Date(message.createdAt).toLocaleString()}</td>
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
        </div>
      </section>
    </div>
  );
}
