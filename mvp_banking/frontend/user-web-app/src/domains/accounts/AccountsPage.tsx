import { useDeferredValue, useState } from "react";
import type { Transaction } from "../transactions/types";
import type { Account } from "./types";
import { formatAmount } from "../../shared/utils/format";
import { sortTransactionsByRecent } from "../../shared/utils/transactions";

type AccountsPageProps = {
  loading: boolean;
  accounts: Account[];
  transactions: Transaction[];
};

export function AccountsPage({ loading, accounts, transactions }: AccountsPageProps) {
  const [accountFilter, setAccountFilter] = useState("");
  const deferredAccountFilter = useDeferredValue(accountFilter);

  const filteredAccounts = accounts.filter((account) => {
    const normalized = deferredAccountFilter.trim().toLowerCase();
    if (!normalized) {
      return true;
    }

    return (
      account.accountType.toLowerCase().includes(normalized) ||
      account.status.toLowerCase().includes(normalized) ||
      account.accountNumber.toLowerCase().includes(normalized)
    );
  });

  if (loading) {
    return (
      <section className="timeline-panel">
        <p>계좌 정보를 불러오는 중입니다...</p>
      </section>
    );
  }

  return (
    <section className="timeline-panel">
      <div className="section-header">
        <div>
          <p className="eyebrow">Accounts</p>
          <h2>내 계좌</h2>
        </div>
        <input
          className="inline-filter"
          placeholder="계좌 유형 / 상태 / 번호 검색"
          value={accountFilter}
          onChange={(event) => setAccountFilter(event.target.value)}
        />
      </div>
      <div className="table-shell">
        <table className="data-table">
          <thead>
            <tr>
              <th>계좌번호</th>
              <th>유형</th>
              <th>잔액</th>
              <th>상태</th>
              <th>고객 ID</th>
              <th>최근 연결 거래</th>
            </tr>
          </thead>
          <tbody>
            {filteredAccounts.length ? (
              filteredAccounts.map((account) => {
                const linkedTransactions = transactions
                  .filter((transaction) => transaction.accountId === account.id)
                  .sort(sortTransactionsByRecent);

                return (
                  <tr key={account.id}>
                    <td className="table-mono">{account.accountNumber}</td>
                    <td>{account.accountType}</td>
                    <td className="table-amount">{formatAmount(account.balance, account.currency)}</td>
                    <td>
                      <b className={`status-pill ${account.status.toLowerCase()}`}>{account.status}</b>
                    </td>
                    <td className="table-mono">{account.customerId.slice(0, 8)}</td>
                    <td>
                      {linkedTransactions.length ? (
                        <div className="table-cell-stack">
                          {linkedTransactions.slice(0, 3).map((transaction) => (
                            <div key={transaction.id}>
                              <strong>{transaction.transactionType}</strong>
                              <p>
                                {formatAmount(transaction.amount, transaction.currency)} · {new Date(transaction.occurredAt).toLocaleDateString()}
                              </p>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <span className="table-muted">연결된 거래 없음</span>
                      )}
                    </td>
                  </tr>
                );
              })
            ) : (
              <tr className="table-empty-row">
                <td colSpan={6}>
                  <strong>조건에 맞는 계좌가 없습니다.</strong>
                  <p>검색어를 지우거나 상태를 다시 확인해 주세요.</p>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}
