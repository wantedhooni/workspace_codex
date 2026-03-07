"use client";

import Link from "next/link";
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
    <div className="page">
      <div>
        <h2 className="page-title">운영 대시보드</h2>
        <p className="page-desc">업무 처리량, 정합성 상태, 전문 처리 지연을 우선순위 기준으로 확인합니다.</p>
      </div>

      <section className="kpi-grid">
        <article className="kpi-card">
          <div className="kpi-label">계좌 수</div>
          <div className="kpi-value">{summary.accountCount}</div>
        </article>
        <article className="kpi-card">
          <div className="kpi-label">확정 이벤트</div>
          <div className="kpi-value">{summary.eventCount}</div>
        </article>
        <article className="kpi-card">
          <div className="kpi-label">대외 전문</div>
          <div className="kpi-value">{summary.messageCount}</div>
        </article>
        <article className="kpi-card">
          <div className="kpi-label">EOD 스냅샷</div>
          <div className="kpi-value">{summary.eodCount}</div>
        </article>
      </section>

      <section className="ops-grid">
        <article className="panel">
          <div className="panel-head">
            <h2>빠른 업무 이동</h2>
            <span className="badge badge-ok">즉시 실행</span>
          </div>
          <div className="quick-actions">
            <Link className="action-tile" href="/account">
              <h3>계정계 업무</h3>
              <p>계좌 개설, 입출금 확정, EOD 대사</p>
            </Link>
            <Link className="action-tile" href="/channel">
              <h3>채널계 접수</h3>
              <p>신청 접수와 멱등 처리 결과 확인</p>
            </Link>
            <Link className="action-tile" href="/external">
              <h3>대외 전문</h3>
              <p>전문 송신 상태와 실패 재전송 관리</p>
            </Link>
            <Link className="action-tile" href="/information">
              <h3>정보계 조회</h3>
              <p>확정 이벤트와 분석 입력 데이터 조회</p>
            </Link>
          </div>
        </article>

        <article className="panel">
          <div className="panel-head">
            <h2>세션 및 운영 기준</h2>
            <span className="badge badge-warn">관리 기준</span>
          </div>
          <div className="meta-list">
            <div className="meta-row">
              <span className="meta-label">접속 권한</span>
              <strong>{roles.join(", ") || "없음"}</strong>
            </div>
            <div className="meta-row">
              <span className="meta-label">우선 복구 순위</span>
              <strong>계정 정합성 &gt; 대외 대사 &gt; 채널 UX</strong>
            </div>
            <div className="meta-row">
              <span className="meta-label">계정계 정책</span>
              <strong>ACID 확정 / referenceId 중복 방지</strong>
            </div>
            <div className="meta-row">
              <span className="meta-label">대외계 정책</span>
              <strong>실패 추적 / 재전송 / 대사</strong>
            </div>
          </div>
          <div className="row" style={{ marginTop: 12 }}>
            <button
              className="danger"
              onClick={() => {
                clearToken();
                location.href = "/login";
              }}
            >
              로그아웃
            </button>
          </div>
        </article>
      </section>

      <section className="layout-2">
        <article className="panel">
          <div className="panel-head">
            <h2>운영 포커스</h2>
          </div>
          <p className="panel-copy">업무 시스템은 신청 유입보다 확정과 복구를 우선합니다. 계정계 수치와 대외 실패 건이 우선 모니터링 대상입니다.</p>
        </article>
        <article className="panel">
          <div className="panel-head">
            <h2>오늘 확인 항목</h2>
          </div>
          <p className="panel-copy">EOD 스냅샷 존재 여부, 실패 전문 재전송 여부, 조회 계정 오조작 방지가 핵심 점검 항목입니다.</p>
        </article>
      </section>

      {error && <div className="alert">{error}</div>}
    </div>
  );
}
