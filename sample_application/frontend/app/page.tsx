"use client";

import { useEffect, useState } from "react";
import { api } from "../components/api";
import { clearToken, getRoles } from "../components/auth";
import { useAuthGuard } from "../components/useAuthGuard";

export default function HomePage() {
  const ready = useAuthGuard();
  const [summary, setSummary] = useState({ accountCount: 0, eventCount: 0, messageCount: 0, eodCount: 0 });
  const [roles, setRoles] = useState<string[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!ready) {
      return;
    }

    setRoles(getRoles());

    Promise.all([
      api<unknown[]>("/api/accounts"),
      api<unknown[]>("/api/information/events"),
      api<unknown[]>("/api/external/messages"),
      api<unknown[]>("/api/eod/snapshots")
    ])
      .then(([accounts, events, messages, eod]) => {
        setSummary({
          accountCount: accounts.length,
          eventCount: events.length,
          messageCount: messages.length,
          eodCount: eod.length
        });
      })
      .catch((err) => setError((err as Error).message));
  }, [ready]);

  if (!ready) {
    return null;
  }

  return (
    <div className="grid grid-2">
      <section className="panel">
        <h2>운영 요약</h2>
        <p>권한: {roles.join(", ") || "없음"}</p>
        <p>계좌 수: {summary.accountCount}</p>
        <p>확정 이벤트 수: {summary.eventCount}</p>
        <p>대외 전문 수: {summary.messageCount}</p>
        <p>EOD 스냅샷 수: {summary.eodCount}</p>
        <button
          className="danger"
          onClick={() => {
            clearToken();
            location.href = "/login";
          }}
        >
          로그아웃
        </button>
      </section>
      <section className="panel">
        <h2>정합성 정책</h2>
        <p>채널계: 멱등키 기반 중복 방지</p>
        <p>계정계: 트랜잭션 + 원장 확정 + 잔액 검증</p>
        <p>정보계: 확정 이벤트 적재</p>
        <p>대외계: 실패/재전송 상태 추적</p>
        <p>EOD: 일 단위 스냅샷/대사 상태 기록</p>
        {error && <p style={{ color: "#b91c1c" }}>{error}</p>}
      </section>
    </div>
  );
}
