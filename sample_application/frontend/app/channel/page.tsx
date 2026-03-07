"use client";

import { FormEvent, useEffect, useState } from "react";
import { api } from "../../components/api";
import { hasRole } from "../../components/auth";
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
    } catch (err) {
      setError((err as Error).message);
    }
  };

  if (!ready) {
    return null;
  }

  if (!isOperator) {
    return <section className="panel">채널계 신청 처리는 OPERATOR 권한이 필요합니다.</section>;
  }

  return (
    <div className="grid">
      <section className="panel">
        <h2>신청 생성</h2>
        <form className="grid" onSubmit={onSubmit}>
          <input value={customerId} onChange={(e) => setCustomerId(e.target.value)} placeholder="고객 ID" />
          <input value={productCode} onChange={(e) => setProductCode(e.target.value)} placeholder="상품 코드" />
          <input value={idempotencyKey} onChange={(e) => setIdempotencyKey(e.target.value)} placeholder="멱등키" />
          <div className="row">
            <button type="submit">신청 처리</button>
            <button type="button" className="secondary" onClick={() => setIdempotencyKey(newId())}>
              멱등키 재생성
            </button>
          </div>
        </form>
      </section>

      <section className="panel">
        <h2>처리 결과</h2>
        {error && <p style={{ color: "#b91c1c" }}>{error}</p>}
        {result && <pre>{JSON.stringify(result, null, 2)}</pre>}
      </section>
    </div>
  );
}
