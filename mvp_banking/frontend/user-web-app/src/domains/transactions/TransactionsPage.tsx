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

  if (loading) {
    return (
      <section className="timeline-panel">
        <p>거래 정보를 불러오는 중입니다...</p>
      </section>
    );
  }

  return (
    <section className="timeline-panel">
      <div className="section-header">
        <div>
          <p className="eyebrow">Transactions</p>
          <h2>최근 거래</h2>
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
  );
}
