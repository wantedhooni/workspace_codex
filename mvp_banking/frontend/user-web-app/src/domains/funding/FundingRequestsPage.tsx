import { useEffect, useMemo, useState, type FormEvent } from "react";
import type { Account } from "../accounts/types";
import type { CreateFundingRequestPayload, FundingRequest } from "./types";
import { formatAmount } from "../../shared/utils/format";

type FundingRequestsPageProps = {
  loading: boolean;
  submitting: boolean;
  accounts: Account[];
  fundingRequests: FundingRequest[];
  onCreate: (payload: CreateFundingRequestPayload) => Promise<void>;
};

export function FundingRequestsPage({
  loading,
  submitting,
  accounts,
  fundingRequests,
  onCreate,
}: FundingRequestsPageProps) {
  const activeAccounts = useMemo(
    () => accounts
      .filter((account) => account.status === "ACTIVE")
      .sort((left, right) => left.accountType.localeCompare(right.accountType) || left.accountNumber.localeCompare(right.accountNumber)),
    [accounts],
  );
  const [form, setForm] = useState({
    accountId: "",
    requestType: "DEPOSIT",
    amount: "1000000",
    note: "",
  });

  useEffect(() => {
    if (activeAccounts.length && !form.accountId) {
      setForm((current) => ({ ...current, accountId: activeAccounts[0].id }));
    }
  }, [activeAccounts, form.accountId]);

  const selectedAccount = activeAccounts.find((account) => account.id === form.accountId);
  const parsedAmount = Number(form.amount || 0);
  const isWithdrawal = form.requestType === "WITHDRAWAL";
  const projectedBalance = selectedAccount
    ? isWithdrawal
      ? Number(selectedAccount.balance) - parsedAmount
      : Number(selectedAccount.balance) + parsedAmount
    : null;
  const hasEnoughBalance = !selectedAccount || !isWithdrawal || Number(selectedAccount.balance) >= parsedAmount;
  const canSubmit = Boolean(form.accountId && parsedAmount > 0 && hasEnoughBalance);
  const pendingCount = fundingRequests.filter((item) => item.status === "PENDING_APPROVAL").length;

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await onCreate({
      accountId: form.accountId,
      requestType: form.requestType,
      amount: Number(form.amount),
      note: form.note.trim() || undefined,
    });
    setForm((current) => ({
      ...current,
      amount: "1000000",
      note: "",
    }));
  }

  if (loading) {
    return (
      <section className="timeline-panel">
        <p>입출금 요청 정보를 불러오는 중입니다...</p>
      </section>
    );
  }

  return (
    <>
      <section className="summary-grid">
        <article className="summary-card">
          <span className="eyebrow">Active Cash Accounts</span>
          <strong>{activeAccounts.length}</strong>
          <p>입출금 요청 가능한 활성 계좌 수</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Pending Requests</span>
          <strong>{pendingCount}</strong>
          <p>운영 승인 대기 중인 입출금 요청</p>
        </article>
        <article className={`summary-card ${isWithdrawal && !hasEnoughBalance ? "negative" : ""}`}>
          <span className="eyebrow">Projected Balance</span>
          <strong>{selectedAccount && projectedBalance !== null ? formatAmount(projectedBalance, selectedAccount.currency) : "-"}</strong>
          <p>{isWithdrawal ? "출금 승인 후 예상 잔액" : "입금 승인 후 예상 잔액"}</p>
        </article>
      </section>

      <section className="timeline-panel asset-panel">
        <div className="section-header compact">
          <div>
            <p className="eyebrow">Funding Desk</p>
            <h2>입출금 요청</h2>
          </div>
        </div>
        <form className="trade-ticket" onSubmit={handleSubmit}>
          <div className="trade-ticket-grid">
            <div className="trade-ticket-main">
              <article className="trade-leg-card">
                <div className="trade-leg-header">
                  <div>
                    <span className="trade-kicker">Account</span>
                    <strong>정산 계좌</strong>
                  </div>
                  {selectedAccount ? <b className={`status-pill ${selectedAccount.status.toLowerCase()}`}>{selectedAccount.status}</b> : null}
                </div>
                <label className="form-field">
                  <span>어느 계좌로 요청할까요?</span>
                  <select value={form.accountId} onChange={(event) => setForm((current) => ({ ...current, accountId: event.target.value }))} required>
                    <option value="">계좌 선택</option>
                    {activeAccounts.map((account) => (
                      <option key={account.id} value={account.id}>
                        {account.accountNumber} / {account.accountType} / {account.currency}
                      </option>
                    ))}
                  </select>
                </label>
                <div className="trade-leg-meta">
                  <div>
                    <span>계좌번호</span>
                    <strong>{selectedAccount?.accountNumber ?? "선택 필요"}</strong>
                  </div>
                  <div>
                    <span>현재 잔액</span>
                    <strong>{selectedAccount ? formatAmount(selectedAccount.balance, selectedAccount.currency) : "-"}</strong>
                  </div>
                </div>
              </article>

              <div className="trade-form-grid">
                <div className="form-field">
                  <span>요청 유형</span>
                  <div className="trade-toggle-row">
                    <button
                      type="button"
                      className={form.requestType === "DEPOSIT" ? "active" : ""}
                      onClick={() => setForm((current) => ({ ...current, requestType: "DEPOSIT" }))}
                    >
                      입금
                    </button>
                    <button
                      type="button"
                      className={form.requestType === "WITHDRAWAL" ? "active" : ""}
                      onClick={() => setForm((current) => ({ ...current, requestType: "WITHDRAWAL" }))}
                    >
                      출금
                    </button>
                  </div>
                </div>
                <label className="form-field">
                  <span>요청 금액</span>
                  <input
                    type="number"
                    min="0.0001"
                    step="0.0001"
                    value={form.amount}
                    onChange={(event) => setForm((current) => ({ ...current, amount: event.target.value }))}
                    placeholder="예: 1000000"
                    required
                  />
                  <small className="form-helper">
                    {selectedAccount ? `${selectedAccount.currency} 기준 금액입니다.` : "계좌 통화 기준으로 입력하세요."}
                  </small>
                </label>
              </div>

              <label className="form-field">
                <span>메모 / 요청 배경</span>
                <input
                  value={form.note}
                  onChange={(event) => setForm((current) => ({ ...current, note: event.target.value }))}
                  placeholder="예: 생활비 출금, 추가 투자 예탁금 입금"
                  maxLength={255}
                />
              </label>
            </div>

            <aside className="trade-summary-card">
              <span className="trade-kicker">Funding Ticket</span>
              <h3>요청 요약</h3>
              <div className="trade-summary-list">
                <div>
                  <span>요청 유형</span>
                  <strong>{form.requestType === "WITHDRAWAL" ? "출금" : "입금"}</strong>
                </div>
                <div>
                  <span>정산 계좌</span>
                  <strong>{selectedAccount ? `${selectedAccount.accountNumber} / ${selectedAccount.accountType}` : "선택 필요"}</strong>
                </div>
                <div>
                  <span>현재 잔액</span>
                  <strong>{selectedAccount ? formatAmount(selectedAccount.balance, selectedAccount.currency) : "-"}</strong>
                </div>
                <div>
                  <span>{isWithdrawal ? "출금 예정" : "입금 예정"}</span>
                  <strong>{selectedAccount ? formatAmount(parsedAmount || 0, selectedAccount.currency) : "-"}</strong>
                </div>
                <div>
                  <span>승인 후 예상 잔액</span>
                  <strong>{selectedAccount && projectedBalance !== null ? formatAmount(projectedBalance, selectedAccount.currency) : "-"}</strong>
                </div>
                <div>
                  <span>운영 처리</span>
                  <strong>승인 후 거래 원장 자동 반영</strong>
                </div>
              </div>
              {!hasEnoughBalance ? <p className="trade-summary-note warning">현재 잔액보다 큰 출금 요청입니다.</p> : null}
              <button type="submit" className="primary-button trade-submit-button" disabled={submitting || !canSubmit}>
                {submitting ? "요청 중..." : "입출금 요청"}
              </button>
              <p className="trade-summary-note">요청 후 운영 승인 절차를 거쳐 계좌와 거래내역에 반영됩니다.</p>
            </aside>
          </div>
        </form>
      </section>

      <section className="timeline-panel">
        <div className="section-header">
          <div>
            <p className="eyebrow">Funding Requests</p>
            <h2>내 입출금 요청</h2>
          </div>
        </div>
        <div className="table-shell">
          <table className="data-table">
            <thead>
              <tr>
                <th>요청번호</th>
                <th>계좌</th>
                <th>유형</th>
                <th>금액 / 스냅샷</th>
                <th>상태</th>
                <th>정산 거래</th>
                <th>메모</th>
              </tr>
            </thead>
            <tbody>
              {fundingRequests.length ? (
                fundingRequests.map((item) => (
                  <tr key={item.id}>
                    <td className="table-mono">{item.requestNumber}</td>
                    <td>
                      <div className="table-cell-stack">
                        <strong>{item.accountNumber}</strong>
                        <p>{`${item.accountType} / ${item.currency}`}</p>
                      </div>
                    </td>
                    <td>
                      <b className={`status-pill ${item.requestType === "WITHDRAWAL" ? "rejected" : "approved"}`}>
                        {item.requestType}
                      </b>
                    </td>
                    <td>
                      <div className="table-cell-stack">
                        <strong>{formatAmount(item.amount, item.currency)}</strong>
                        <p>{`snapshot ${formatAmount(item.balanceSnapshot, item.currency)}`}</p>
                      </div>
                    </td>
                    <td>
                      <b className={`status-pill ${item.status.toLowerCase()}`}>{item.status}</b>
                    </td>
                    <td>
                      <div className="table-cell-stack">
                        <strong className="table-mono">{item.settlementTransactionNumber ?? "정산 대기"}</strong>
                        <p>{item.settledAt ? new Date(item.settledAt).toLocaleString() : new Date(item.createdAt).toLocaleString()}</p>
                      </div>
                    </td>
                    <td>{item.note ?? "-"}</td>
                  </tr>
                ))
              ) : (
                <tr className="table-empty-row">
                  <td colSpan={7}>
                    <strong>입출금 요청 내역이 없습니다.</strong>
                    <p>첫 요청을 등록하면 운영 승인 상태를 여기서 추적할 수 있습니다.</p>
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
