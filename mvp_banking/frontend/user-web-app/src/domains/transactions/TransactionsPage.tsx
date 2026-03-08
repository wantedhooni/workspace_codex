import { useDeferredValue, useState } from "react";
import { formatAmount } from "../../shared/utils/format";
import type { Transaction } from "./types";

type TransactionsPageProps = {
  loading: boolean;
  transactions: Transaction[];
};

export function TransactionsPage({ loading, transactions }: TransactionsPageProps) {
  const [transactionFilter, setTransactionFilter] = useState("");
  const [transactionStatusFilter, setTransactionStatusFilter] = useState("ALL");

  const deferredTransactionFilter = useDeferredValue(transactionFilter);

  const filteredTransactions = transactions.filter((transaction) => {
    const normalized = deferredTransactionFilter.trim().toLowerCase();
    const matchesQuery =
      !normalized ||
      transaction.transactionType.toLowerCase().includes(normalized) ||
      transaction.transactionNumber.toLowerCase().includes(normalized) ||
      transaction.currency.toLowerCase().includes(normalized);

    const matchesStatus = transactionStatusFilter === "ALL" || transaction.status === transactionStatusFilter;
    return matchesQuery && matchesStatus;
  });
  const pendingCount = transactions.filter((transaction) => transaction.status === "PENDING").length;
  const completedCount = transactions.filter((transaction) => transaction.status === "COMPLETED").length;
  const latestTransaction = [...transactions].sort((left, right) => Date.parse(right.occurredAt) - Date.parse(left.occurredAt))[0];

  if (loading) {
    return (
      <section className="timeline-panel">
        <p>거래 정보를 불러오는 중입니다...</p>
      </section>
    );
  }

  return (
    <>
      <section className="summary-grid">
        <article className="summary-card">
          <span className="eyebrow">Visible Transactions</span>
          <strong>{transactions.length}</strong>
          <p>계정에서 조회 가능한 전체 거래 수</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Completed</span>
          <strong>{completedCount}</strong>
          <p>정상 완료되어 원장에 반영된 거래</p>
        </article>
        <article className="summary-card negative">
          <span className="eyebrow">Pending</span>
          <strong>{pendingCount}</strong>
          <p>아직 처리 대기 또는 승인 대기 중인 거래</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Latest Activity</span>
          <strong>{latestTransaction?.transactionType ?? "NO DATA"}</strong>
          <p>{latestTransaction ? new Date(latestTransaction.occurredAt).toLocaleString() : "최근 거래 없음"}</p>
        </article>
      </section>

      <section className="timeline-panel">
        <div className="page-convenience-strip">
          <strong>{filteredTransactions.length}건 거래 표시 중</strong>
          <span>거래번호 검색과 상태 필터를 함께 쓰면 원하는 원장 항목을 빠르게 찾을 수 있습니다.</span>
        </div>
        <div className="section-header">
          <div>
            <p className="eyebrow">Transactions</p>
            <h2>최근 거래</h2>
            <p className="section-copy">
              거래번호, 상태, 금액을 기준으로 최근 원장 흐름을 빠르게 확인할 수 있습니다.
            </p>
          </div>
          <div className="filter-cluster">
            <input
              className="inline-filter"
              placeholder="거래번호 / 유형 / 통화 검색"
              value={transactionFilter}
              onChange={(event) => setTransactionFilter(event.target.value)}
            />
            <select value={transactionStatusFilter} onChange={(event) => setTransactionStatusFilter(event.target.value)}>
              <option value="ALL">전체 상태</option>
              <option value="COMPLETED">COMPLETED</option>
              <option value="PENDING">PENDING</option>
              <option value="REJECTED">REJECTED</option>
            </select>
            <button type="button" className="table-inline-button" onClick={() => {
              setTransactionFilter("");
              setTransactionStatusFilter("ALL");
            }}>
              Reset
            </button>
          </div>
        </div>
        <div className="table-shell">
          <table className="data-table">
            <thead>
              <tr>
                <th>거래 유형</th>
                <th>거래번호</th>
                <th>금액</th>
                <th>상태</th>
                <th>일시</th>
              </tr>
            </thead>
            <tbody>
              {filteredTransactions.length ? (
                filteredTransactions.map((transaction) => (
                  <tr key={transaction.id}>
                    <td>{transaction.transactionType}</td>
                    <td className="table-mono">{transaction.transactionNumber}</td>
                    <td className="table-amount">{formatAmount(transaction.amount, transaction.currency)}</td>
                    <td>
                      <b className={`status-pill ${transaction.status.toLowerCase()}`}>{transaction.status}</b>
                    </td>
                    <td>{new Date(transaction.occurredAt).toLocaleString()}</td>
                  </tr>
                ))
              ) : (
                <tr className="table-empty-row">
                  <td colSpan={5}>
                    <strong>조건에 맞는 거래가 없습니다.</strong>
                    <p>검색어 또는 상태 필터를 조정해 주세요.</p>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
