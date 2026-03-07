"use client";

import { useEffect, useState } from "react";
import { api } from "../../components/api";
import { useAuthGuard } from "../../components/useAuthGuard";

type Event = {
  id: string;
  eventType: string;
  aggregateId: string;
  payload: string;
  confirmedAt: string;
};

export default function InformationPage() {
  const ready = useAuthGuard();
  const [events, setEvents] = useState<Event[]>([]);
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    const data = await api<Event[]>("/api/information/events");
    setEvents(data);
  };

  useEffect(() => {
    if (!ready) {
      return;
    }
    load().catch((err) => setError((err as Error).message));
  }, [ready]);

  if (!ready) {
    return null;
  }

  return (
    <div className="page">
      <div>
        <h2 className="page-title">정보계 조회</h2>
        <p className="page-desc">확정 이벤트를 확인하고 리포팅/감사 분석의 입력 데이터를 점검합니다.</p>
      </div>

      {error && <div className="alert">{error}</div>}

      <section className="kpi-grid">
        <article className="kpi-card">
          <div className="kpi-label">이벤트 건수</div>
          <div className="kpi-value">{events.length}</div>
        </article>
        <article className="kpi-card">
          <div className="kpi-label">최신 집계 유형</div>
          <div className="kpi-value">{events[0]?.eventType ?? "-"}</div>
        </article>
      </section>

      <section className="panel">
        <div className="panel-head">
          <h2>확정 이벤트</h2>
          <button onClick={() => load().catch((err) => setError((err as Error).message))}>새로고침</button>
        </div>
        <p className="panel-sub">분석계와 감사 추적에서 사용하는 정본 이벤트만 조회합니다.</p>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>유형</th>
                <th>집계ID</th>
                <th>확정시각</th>
                <th>Payload</th>
              </tr>
            </thead>
            <tbody>
              {events.map((event) => (
                <tr key={event.id}>
                  <td>{event.eventType}</td>
                  <td>{event.aggregateId}</td>
                  <td>{new Date(event.confirmedAt).toLocaleString()}</td>
                  <td>{event.payload}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
