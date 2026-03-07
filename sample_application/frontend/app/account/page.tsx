"use client";

import { FormEvent, useCallback, useEffect, useState } from "react";
import { api } from "../../components/api";
import { hasRole } from "../../components/auth";
import Modal from "../../components/Modal";
import { useAuthGuard } from "../../components/useAuthGuard";

type Account = {
  id: string;
  accountNo: string;
  customerId: string;
  status: string;
  balance: number;
  createdAt: string;
};

type EodSnapshot = {
  id: string;
  businessDate: string;
  accountCount: number;
  totalBalance: number;
  reconciliationStatus: string;
};

type LedgerEntry = {
  ledgerId: string;
  accountId: string;
  accountNo: string;
  customerId: string;
  type: "DEPOSIT" | "WITHDRAW";
  amount: number;
  balanceAfter: number;
  referenceId: string;
  occurredAt: string;
};

function newId() {
  return globalThis.crypto?.randomUUID?.() ?? `${Date.now()}`;
}

export default function AccountPage() {
  const ready = useAuthGuard();
  const isOperator = hasRole("OPERATOR");

  const [accounts, setAccounts] = useState<Account[]>([]);
  const [snapshots, setSnapshots] = useState<EodSnapshot[]>([]);
  const [ledger, setLedger] = useState<LedgerEntry[]>([]);

  const [customerId, setCustomerId] = useState("CUST-001");
  const [selectedAccount, setSelectedAccount] = useState("");
  const [type, setType] = useState("DEPOSIT");
  const [amount, setAmount] = useState("10000");
  const [referenceId, setReferenceId] = useState("");
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [transactionModalOpen, setTransactionModalOpen] = useState(false);

  const [searchCustomerId, setSearchCustomerId] = useState("");
  const [searchType, setSearchType] = useState("");

  const [error, setError] = useState<string | null>(null);

  const loadAccounts = useCallback(async () => {
    const data = await api<Account[]>("/api/accounts");
    setAccounts(data);
    if (!selectedAccount && data.length > 0) {
      setSelectedAccount(data[0].id);
    }
  }, [selectedAccount]);

  const loadSnapshots = useCallback(async () => {
    const data = await api<EodSnapshot[]>("/api/eod/snapshots");
    setSnapshots(data);
  }, []);

  const loadLedger = useCallback(async () => {
    const data = await api<LedgerEntry[]>("/api/accounts/ledger/search", {
      method: "POST",
      body: JSON.stringify({
        customerId: searchCustomerId || null,
        type: searchType || null,
        limit: 100
      })
    });
    setLedger(data);
  }, [searchCustomerId, searchType]);

  useEffect(() => {
    if (!ready) {
      return;
    }
    setReferenceId(newId());
    Promise.all([loadAccounts(), loadSnapshots(), loadLedger()]).catch((err) => setError((err as Error).message));
  }, [ready, loadAccounts, loadSnapshots, loadLedger]);

  const createAccount = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      await api("/api/accounts", {
        method: "POST",
        body: JSON.stringify({ customerId })
      });
      await loadAccounts();
      setCreateModalOpen(false);
    } catch (err) {
      setError((err as Error).message);
    }
  };

  const transaction = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      const key = referenceId || newId();
      await api(`/api/accounts/${selectedAccount}/transactions`, {
        method: "POST",
        body: JSON.stringify({ type, amount: Number(amount), referenceId: key }),
        headers: { "Reference-Id": key }
      });
      setReferenceId(newId());
      await Promise.all([loadAccounts(), loadSnapshots(), loadLedger()]);
      setTransactionModalOpen(false);
    } catch (err) {
      setError((err as Error).message);
    }
  };

  const runEod = async () => {
    setError(null);
    try {
      await api("/api/eod/run", { method: "POST" });
      await loadSnapshots();
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
        <h2 className="page-title">계정계 업무</h2>
        <p className="page-desc">계좌 개설, 원장 확정, EOD 대사까지 핵심 원장 업무를 처리합니다.</p>
      </div>

      {error && <div className="alert">{error}</div>}

      <section className="kpi-grid">
        <article className="kpi-card">
          <div className="kpi-label">계좌 총수</div>
          <div className="kpi-value">{accounts.length}</div>
        </article>
        <article className="kpi-card">
          <div className="kpi-label">최근 EOD</div>
          <div className="kpi-value">{snapshots[0]?.reconciliationStatus ?? "-"}</div>
        </article>
        <article className="kpi-card">
          <div className="kpi-label">원장 조회건</div>
          <div className="kpi-value">{ledger.length}</div>
        </article>
        <article className="kpi-card">
          <div className="kpi-label">처리 권한</div>
          <div className="kpi-value">{isOperator ? "OPR" : "VIEW"}</div>
        </article>
      </section>

      {isOperator ? (
        <section className="content-split">
          <article className="panel">
            <div className="panel-head">
              <h2>계좌 개설</h2>
              <span className="badge badge-ok">승인 가능</span>
            </div>
            <p className="panel-sub">고객 식별자를 기준으로 신규 계좌를 개설합니다.</p>
            <div className="row">
              <button onClick={() => setCreateModalOpen(true)} type="button">
                계좌 개설 열기
              </button>
            </div>
          </article>

          <article className="panel">
            <div className="panel-head">
              <h2>입출금 확정</h2>
              <span className="badge badge-ok">트랜잭션</span>
            </div>
            <p className="panel-sub">원장 반영 전 referenceId로 중복 여부를 확인합니다.</p>
            <div className="row">
              <button onClick={() => setTransactionModalOpen(true)} type="button">
                입출금 확정 열기
              </button>
            </div>
          </article>
        </section>
      ) : (
        <div className="info">VIEWER 권한으로 접속했습니다. 계좌 개설/입출금/EOD 실행은 허용되지 않습니다.</div>
      )}

      <Modal onClose={() => setCreateModalOpen(false)} open={createModalOpen} title="계좌 개설">
        <p className="panel-sub" style={{ marginTop: 0, marginBottom: 10 }}>
          신규 계좌 생성은 즉시 원장 기준 데이터에 반영됩니다.
        </p>
        <form className="form-grid" onSubmit={createAccount}>
          <input value={customerId} onChange={(e) => setCustomerId(e.target.value)} placeholder="고객 ID" />
          <button type="submit">개설 실행</button>
        </form>
      </Modal>

      <Modal onClose={() => setTransactionModalOpen(false)} open={transactionModalOpen} title="입출금 확정">
        <p className="panel-sub" style={{ marginTop: 0, marginBottom: 10 }}>
          referenceId와 금액을 확인한 뒤 트랜잭션을 확정합니다.
        </p>
        <form className="form-grid" onSubmit={transaction}>
          <select value={selectedAccount} onChange={(e) => setSelectedAccount(e.target.value)}>
            {accounts.map((account) => (
              <option value={account.id} key={account.id}>
                {account.accountNo} ({account.customerId})
              </option>
            ))}
          </select>
          <select value={type} onChange={(e) => setType(e.target.value)}>
            <option value="DEPOSIT">입금</option>
            <option value="WITHDRAW">출금</option>
          </select>
          <input value={amount} onChange={(e) => setAmount(e.target.value)} placeholder="금액" />
          <input value={referenceId} onChange={(e) => setReferenceId(e.target.value)} placeholder="참조 ID" />
          <button type="submit">확정 반영</button>
        </form>
      </Modal>

      <section className="panel">
        <div className="panel-head">
          <h2>EOD 배치/대사</h2>
          <div className="row">
            {isOperator && <button onClick={runEod}>EOD 수동 실행</button>}
            <button className="secondary" onClick={() => loadSnapshots().catch((err) => setError((err as Error).message))}>
              새로고침
            </button>
          </div>
        </div>
        <p className="panel-sub">일 마감 기준 계좌 수와 총잔액을 집계해 대사 상태를 남깁니다.</p>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>영업일</th>
                <th>계좌 수</th>
                <th>총잔액</th>
                <th>대사상태</th>
              </tr>
            </thead>
            <tbody>
              {snapshots.map((snapshot) => (
                <tr key={snapshot.id}>
                  <td>{snapshot.businessDate}</td>
                  <td>{snapshot.accountCount}</td>
                  <td>{snapshot.totalBalance}</td>
                  <td>
                    <span className={snapshot.reconciliationStatus === "MATCHED" ? "badge badge-ok" : "badge badge-danger"}>
                      {snapshot.reconciliationStatus}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section className="panel">
        <div className="panel-head">
          <h2>원장 검색 (Querydsl)</h2>
          <div className="row">
            <button onClick={() => loadLedger().catch((err) => setError((err as Error).message))}>검색 실행</button>
          </div>
        </div>
        <p className="panel-sub">고객 ID와 거래 유형으로 빠르게 필터링해 이상 거래나 복구 대상 건을 확인합니다.</p>
        <div className="form-grid" style={{ marginBottom: 10 }}>
          <input value={searchCustomerId} onChange={(e) => setSearchCustomerId(e.target.value)} placeholder="고객 ID" />
          <select value={searchType} onChange={(e) => setSearchType(e.target.value)}>
            <option value="">전체 유형</option>
            <option value="DEPOSIT">입금</option>
            <option value="WITHDRAW">출금</option>
          </select>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>시각</th>
                <th>계좌번호</th>
                <th>고객ID</th>
                <th>유형</th>
                <th>금액</th>
                <th>거래후잔액</th>
                <th>참조ID</th>
              </tr>
            </thead>
            <tbody>
              {ledger.map((entry) => (
                <tr key={entry.ledgerId}>
                  <td>{new Date(entry.occurredAt).toLocaleString()}</td>
                  <td>{entry.accountNo}</td>
                  <td>{entry.customerId}</td>
                  <td>{entry.type}</td>
                  <td>{entry.amount}</td>
                  <td>{entry.balanceAfter}</td>
                  <td>{entry.referenceId}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section className="panel">
        <div className="panel-head">
          <h2>계좌 목록</h2>
          <button className="secondary" onClick={() => loadAccounts().catch((err) => setError((err as Error).message))}>
            새로고침
          </button>
        </div>
        <p className="panel-sub">현재 활성 계좌와 잔액을 운영 기준으로 점검합니다.</p>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>계좌번호</th>
                <th>고객ID</th>
                <th>상태</th>
                <th>잔액</th>
                <th>개설시각</th>
              </tr>
            </thead>
            <tbody>
              {accounts.map((account) => (
                <tr key={account.id}>
                  <td>{account.accountNo}</td>
                  <td>{account.customerId}</td>
                  <td>{account.status}</td>
                  <td>{account.balance}</td>
                  <td>{new Date(account.createdAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
