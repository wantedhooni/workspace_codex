"use client";

import { FormEvent, useCallback, useEffect, useState } from "react";
import { api } from "../../components/api";
import { hasRole } from "../../components/auth";
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
  createdAt: string;
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
    <div className="grid grid-2">
      {isOperator && (
        <>
          <section className="panel">
            <h2>계좌 개설</h2>
            <form className="grid" onSubmit={createAccount}>
              <input value={customerId} onChange={(e) => setCustomerId(e.target.value)} placeholder="고객 ID" />
              <button type="submit">계좌 개설</button>
            </form>
          </section>

          <section className="panel">
            <h2>입출금 처리</h2>
            <form className="grid" onSubmit={transaction}>
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
              <button type="submit">확정 처리</button>
            </form>
          </section>
        </>
      )}

      {!isOperator && (
        <section className="panel" style={{ gridColumn: "1 / -1" }}>
          조회 전용 계정입니다. 계좌개설/입출금/EOD 실행은 OPERATOR 권한이 필요합니다.
        </section>
      )}

      <section className="panel" style={{ gridColumn: "1 / -1" }}>
        <h2>EOD 배치/대사</h2>
        <div className="row" style={{ marginBottom: 12 }}>
          {isOperator && <button onClick={runEod}>EOD 수동 실행</button>}
          <button className="secondary" onClick={() => loadSnapshots().catch((err) => setError((err as Error).message))}>
            스냅샷 새로고침
          </button>
        </div>
        <table>
          <thead>
            <tr>
              <th>영업일</th>
              <th>계좌수</th>
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
                <td>{snapshot.reconciliationStatus}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="panel" style={{ gridColumn: "1 / -1" }}>
        <h2>원장 검색(Querydsl)</h2>
        <div className="row" style={{ marginBottom: 12 }}>
          <input
            value={searchCustomerId}
            onChange={(e) => setSearchCustomerId(e.target.value)}
            placeholder="고객ID(선택)"
          />
          <select value={searchType} onChange={(e) => setSearchType(e.target.value)}>
            <option value="">전체 유형</option>
            <option value="DEPOSIT">입금</option>
            <option value="WITHDRAW">출금</option>
          </select>
          <button onClick={() => loadLedger().catch((err) => setError((err as Error).message))}>검색</button>
        </div>
        <table>
          <thead>
            <tr>
              <th>시각</th>
              <th>계좌번호</th>
              <th>고객ID</th>
              <th>유형</th>
              <th>금액</th>
              <th>거래후잔액</th>
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
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="panel" style={{ gridColumn: "1 / -1" }}>
        <h2>계좌 목록</h2>
        {error && <p style={{ color: "#b91c1c" }}>{error}</p>}
        <table>
          <thead>
            <tr>
              <th>계좌번호</th>
              <th>고객ID</th>
              <th>상태</th>
              <th>잔액</th>
            </tr>
          </thead>
          <tbody>
            {accounts.map((account) => (
              <tr key={account.id}>
                <td>{account.accountNo}</td>
                <td>{account.customerId}</td>
                <td>{account.status}</td>
                <td>{account.balance}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
}
