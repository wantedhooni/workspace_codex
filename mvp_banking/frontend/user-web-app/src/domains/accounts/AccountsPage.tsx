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
  const activeAccounts = accounts.filter((account) => account.status === "ACTIVE");
  const bankingAccounts = accounts.filter((account) => account.accountType === "BANKING");
  const securitiesAccounts = accounts.filter((account) => account.accountType === "SECURITIES");
  const currencyCoverage = new Set(accounts.map((account) => account.currency)).size;

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
    <>
      <section className="summary-grid">
        <article className="summary-card">
          <span className="eyebrow">All Accounts</span>
          <strong>{accounts.length}</strong>
          <p>전체 계좌 수와 접근 가능한 자산 채널</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Active Accounts</span>
          <strong>{activeAccounts.length}</strong>
          <p>정상 상태로 즉시 거래 가능한 계좌</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Banking / Securities</span>
          <strong>{`${bankingAccounts.length} / ${securitiesAccounts.length}`}</strong>
          <p>현금 계좌와 증권 계좌 분포</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Currency Coverage</span>
          <strong>{currencyCoverage}</strong>
          <p>현재 계좌에서 관리 중인 통화 종류 수</p>
        </article>
      </section>

      <section className="timeline-panel">
        <div className="page-convenience-strip">
          <strong>{filteredAccounts.length}개 계좌 표시 중</strong>
          <span>검색어를 바꾸면 계좌번호, 상태, 유형 기준으로 바로 좁혀집니다.</span>
        </div>
        <div className="section-header">
          <div>
            <p className="eyebrow">Account Registry</p>
            <h2>내 계좌</h2>
            <p className="section-copy">
              계좌 상태, 유형, 연결 거래를 한 번에 확인할 수 있도록 운영형 표로 정리했습니다.
            </p>
          </div>
          <div className="filter-cluster">
            <input
              className="inline-filter"
              placeholder="계좌 유형 / 상태 / 번호 검색"
              value={accountFilter}
              onChange={(event) => setAccountFilter(event.target.value)}
            />
            <button type="button" className="table-inline-button" onClick={() => setAccountFilter("")}>
              Reset
            </button>
          </div>
        </div>
        <div className="table-shell">
          <table className="data-table">
            <colgroup>
              <col style={{ width: 190 }} />
              <col style={{ width: 150 }} />
              <col style={{ width: 180 }} />
              <col style={{ width: 150 }} />
              <col style={{ width: 150 }} />
              <col style={{ width: 320 }} />
            </colgroup>
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
    </>
  );
}
