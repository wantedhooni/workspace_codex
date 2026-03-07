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
    <section className="panel">
      <h2>확정 이벤트 조회</h2>
      <div className="row" style={{ marginBottom: 12 }}>
        <button onClick={() => load().catch((err) => setError((err as Error).message))}>새로고침</button>
      </div>
      {error && <p style={{ color: "#b91c1c" }}>{error}</p>}
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
    </section>
  );
}
