"use client";

import { FormEvent, useEffect, useState } from "react";
import { api } from "../../components/api";
import { hasRole } from "../../components/auth";
import Modal from "../../components/Modal";
import { useAuthGuard } from "../../components/useAuthGuard";

type ApplicationResponse = {
  id: string;
  customerId: string;
  productCode: string;
  status: string;
  accountId: string | null;
  createdAt: string;
};

function newId() {
  return globalThis.crypto?.randomUUID?.() ?? `${Date.now()}`;
}

export default function ChannelPage() {
  const ready = useAuthGuard();
  const isOperator = hasRole("OPERATOR");
  const [customerId, setCustomerId] = useState("CUST-001");
  const [productCode, setProductCode] = useState("EQUITY_BASIC");
  const [idempotencyKey, setIdempotencyKey] = useState("");
  const [result, setResult] = useState<ApplicationResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [createModalOpen, setCreateModalOpen] = useState(false);

  useEffect(() => {
    setIdempotencyKey(newId());
  }, []);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    try {
      const key = idempotencyKey || newId();
      const response = await api<ApplicationResponse>("/api/channel/applications", {
        method: "POST",
        body: JSON.stringify({ customerId, productCode, idempotencyKey: key }),
        headers: { "Idempotency-Key": key }
      });
      setResult(response);
      setIdempotencyKey(newId());
      setCreateModalOpen(false);
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
        <h2 className="page-title">채널계 접수</h2>
        <p className="page-desc">신청 접수, 멱등 처리, 계정계 전달 전 단계를 운영합니다.</p>
      </div>

      {!isOperator && <div className="info">VIEWER 권한으로 접속했습니다. 접수 처리는 OPERATOR만 가능합니다.</div>}
      {error && <div className="alert">{error}</div>}

      {isOperator && (
        <section className="panel">
          <div className="panel-head">
            <h2>신청 등록</h2>
            <button onClick={() => setCreateModalOpen(true)} type="button">
              접수 등록 열기
            </button>
          </div>
          <p className="panel-sub">접수 단계에서 동일 요청의 중복 반영을 차단합니다.</p>
          <div className="info">신규 접수는 모달에서 수행합니다. 본문에서는 처리 결과를 확인합니다.</div>
        </section>
      )}

      <Modal onClose={() => setCreateModalOpen(false)} open={createModalOpen} title="채널계 신청 등록">
        <div className="row" style={{ marginBottom: 10 }}>
          <button className="secondary" onClick={() => setIdempotencyKey(newId())} type="button">
            멱등키 재생성
          </button>
        </div>
        <form className="form-grid" onSubmit={onSubmit}>
          <input value={customerId} onChange={(e) => setCustomerId(e.target.value)} placeholder="고객 ID" />
          <input value={productCode} onChange={(e) => setProductCode(e.target.value)} placeholder="상품 코드" />
          <input value={idempotencyKey} onChange={(e) => setIdempotencyKey(e.target.value)} placeholder="멱등키" />
          <button type="submit">접수 처리</button>
        </form>
      </Modal>

      <section className="panel">
        <div className="panel-head">
          <h2>처리 결과</h2>
          <span className={result?.status === "APPROVED" ? "badge badge-ok" : "badge badge-warn"}>{result?.status ?? "대기"}</span>
        </div>
        <p className="panel-sub">계정계 전달 전 접수 결과와 생성된 계좌 연결 여부를 확인합니다.</p>
        <div className="code-box">{result ? JSON.stringify(result, null, 2) : "아직 처리 결과가 없습니다."}</div>
      </section>
    </div>
  );
}
