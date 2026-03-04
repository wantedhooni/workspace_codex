import { useEffect, useState, type FormEvent } from "react";
import type { Account } from "../accounts/types";
import type { FxRate } from "../fx/types";
import type { CreateExchangeRequestPayload, ExchangeRequest } from "./types";
import { formatAmount } from "../../shared/utils/format";

const EXCHANGE_FEE_RATE = 0.0012;

type ExchangeRequestsPageProps = {
  loading: boolean;
  submitting: boolean;
  accounts: Account[];
  fxRates: FxRate[];
  exchangeRequests: ExchangeRequest[];
  onCreate: (payload: CreateExchangeRequestPayload) => Promise<void>;
};

export function ExchangeRequestsPage({
  loading,
  submitting,
  accounts,
  fxRates,
  exchangeRequests,
  onCreate,
}: ExchangeRequestsPageProps) {
  const bankingAccounts = [...accounts]
    .filter((account) => account.accountType === "BANKING")
    .sort((left, right) => left.currency.localeCompare(right.currency) || left.accountNumber.localeCompare(right.accountNumber));
  const [form, setForm] = useState({
    sourceAccountId: "",
    destinationAccountId: "",
    fromAmount: "1000",
  });
  const sourceAccount = bankingAccounts.find((account) => account.id === form.sourceAccountId);
  const destinationCandidates = bankingAccounts.filter(
    (account) => account.id !== form.sourceAccountId && (!sourceAccount || account.currency !== sourceAccount.currency),
  );
  const destinationAccount = destinationCandidates.find((account) => account.id === form.destinationAccountId);
  const accountById = new Map(accounts.map((account) => [account.id, account]));
  const parsedFromAmount = Number(form.fromAmount || 0);
  const rateSnapshot = resolveFxRate(sourceAccount?.currency, destinationAccount?.currency, fxRates);
  const expectedGrossReceiveAmount = rateSnapshot && Number.isFinite(parsedFromAmount)
    ? parsedFromAmount * rateSnapshot.rate
    : null;
  const expectedFeeAmount = expectedGrossReceiveAmount !== null
    ? expectedGrossReceiveAmount * EXCHANGE_FEE_RATE
    : null;
  const expectedReceiveAmount = expectedGrossReceiveAmount !== null && expectedFeeAmount !== null
    ? expectedGrossReceiveAmount - expectedFeeAmount
    : null;
  const canSubmit = Boolean(
    form.sourceAccountId
    && form.destinationAccountId
    && parsedFromAmount > 0
    && rateSnapshot,
  );

  useEffect(() => {
    if (!bankingAccounts.length) {
      return;
    }

    setForm((current) => {
      let nextSourceAccountId = current.sourceAccountId;
      if (!bankingAccounts.some((account) => account.id === nextSourceAccountId)) {
        nextSourceAccountId = bankingAccounts[0]?.id ?? "";
      }

      const nextSourceAccount = bankingAccounts.find((account) => account.id === nextSourceAccountId);
      const nextDestinationCandidates = bankingAccounts.filter(
        (account) => account.id !== nextSourceAccountId && (!nextSourceAccount || account.currency !== nextSourceAccount.currency),
      );

      let nextDestinationAccountId = current.destinationAccountId;
      if (!nextDestinationCandidates.some((account) => account.id === nextDestinationAccountId)) {
        nextDestinationAccountId = nextDestinationCandidates[0]?.id ?? "";
      }

      if (
        current.sourceAccountId === nextSourceAccountId
        && current.destinationAccountId === nextDestinationAccountId
      ) {
        return current;
      }

      return {
        ...current,
        sourceAccountId: nextSourceAccountId,
        destinationAccountId: nextDestinationAccountId,
      };
    });
  }, [accounts]);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await onCreate({
      sourceAccountId: form.sourceAccountId,
      destinationAccountId: form.destinationAccountId,
      fromAmount: Number(form.fromAmount),
    });
  }

  if (loading) {
    return (
      <section className="timeline-panel">
        <p>환전 정보를 불러오는 중입니다...</p>
      </section>
    );
  }

  return (
    <>
      <section className="timeline-panel asset-panel">
        <div className="section-header compact">
          <div>
            <p className="eyebrow">FX Exchange</p>
            <h2>환전 요청</h2>
          </div>
        </div>
        <form className="trade-ticket" onSubmit={onSubmit}>
          <div className="trade-ticket-grid">
            <div className="trade-ticket-main">
              <div className="trade-leg-grid">
                <article className="trade-leg-card">
                  <div className="trade-leg-header">
                    <div>
                      <span className="trade-kicker">Source</span>
                      <strong>출금 계좌</strong>
                    </div>
                    {sourceAccount ? <b className={`status-pill ${sourceAccount.status.toLowerCase()}`}>{sourceAccount.status}</b> : null}
                  </div>
                  <label className="form-field">
                    <span>어느 계좌에서 출금할까요?</span>
                    <select
                      value={form.sourceAccountId}
                      onChange={(event) => setForm((current) => ({ ...current, sourceAccountId: event.target.value }))}
                      required
                    >
                      <option value="">출금 계좌 선택</option>
                      {bankingAccounts.map((account) => (
                        <option key={account.id} value={account.id}>
                          {account.accountNumber} / {account.currency} / {account.status}
                        </option>
                      ))}
                    </select>
                  </label>
                  <div className="trade-leg-meta">
                    <div>
                      <span>계좌번호</span>
                      <strong>{sourceAccount?.accountNumber ?? "선택 필요"}</strong>
                    </div>
                    <div>
                      <span>출금 가능 잔액</span>
                      <strong>{sourceAccount ? formatAmount(sourceAccount.balance, sourceAccount.currency) : "-"}</strong>
                    </div>
                  </div>
                </article>

                <div className="trade-flow-connector" aria-hidden="true">
                  <span>{sourceAccount?.currency ?? "FROM"}</span>
                  <b>→</b>
                  <span>{destinationAccount?.currency ?? "TO"}</span>
                </div>

                <article className="trade-leg-card">
                  <div className="trade-leg-header">
                    <div>
                      <span className="trade-kicker">Destination</span>
                      <strong>입금 계좌</strong>
                    </div>
                    {destinationAccount ? <b className={`status-pill ${destinationAccount.status.toLowerCase()}`}>{destinationAccount.status}</b> : null}
                  </div>
                  <label className="form-field">
                    <span>어느 계좌로 입금할까요?</span>
                    <select
                      value={form.destinationAccountId}
                      onChange={(event) => setForm((current) => ({ ...current, destinationAccountId: event.target.value }))}
                      required
                    >
                      <option value="">입금 계좌 선택</option>
                      {destinationCandidates.map((account) => (
                        <option key={account.id} value={account.id}>
                          {account.accountNumber} / {account.currency} / {account.status}
                        </option>
                      ))}
                    </select>
                  </label>
                  <div className="trade-leg-meta">
                    <div>
                      <span>계좌번호</span>
                      <strong>{destinationAccount?.accountNumber ?? "선택 필요"}</strong>
                    </div>
                    <div>
                      <span>입금 통화</span>
                      <strong>{destinationAccount?.currency ?? "-"}</strong>
                    </div>
                  </div>
                </article>
              </div>

              <div className="trade-route-strip">
                <div>
                  <span className="trade-kicker">Exchange Route</span>
                  <strong>
                    {sourceAccount && destinationAccount
                      ? `${sourceAccount.currency} -> ${destinationAccount.currency}`
                      : "출금 계좌와 입금 계좌를 선택하세요"}
                  </strong>
                  <p>
                    {sourceAccount && destinationAccount
                      ? `${sourceAccount.accountNumber} -> ${destinationAccount.accountNumber}`
                      : "서로 다른 통화의 은행 계좌를 연결하면 예상 환산 금액이 바로 보입니다."}
                  </p>
                </div>
                {rateSnapshot ? (
                  <div className="trade-rate-chip">
                    <span>Live Rate</span>
                    <strong>{`1 ${sourceAccount?.currency} = ${rateSnapshot.rate.toFixed(4)} ${destinationAccount?.currency}`}</strong>
                  </div>
                ) : (
                  <div className="trade-rate-chip muted">
                    <span>Rate</span>
                    <strong>지원 환율 없음</strong>
                  </div>
                )}
              </div>

              <div className="trade-form-grid">
                <label className="form-field">
                  <span>환전 금액</span>
                  <input
                    type="number"
                    min="0.0001"
                    step="0.0001"
                    value={form.fromAmount}
                    onChange={(event) => setForm((current) => ({ ...current, fromAmount: event.target.value }))}
                    placeholder="예: 1000"
                    required
                  />
                  <small className="form-helper">
                    {sourceAccount
                      ? `${sourceAccount.currency} 기준으로 입력하세요.`
                      : "출금 계좌 통화 기준으로 요청 금액을 입력하세요."}
                  </small>
                </label>
              </div>
            </div>

            <aside className="trade-summary-card">
              <span className="trade-kicker">Request Preview</span>
              <h3>예상 정산</h3>
              <div className="trade-summary-list">
                <div>
                  <span>출금 예정</span>
                  <strong>{sourceAccount ? formatAmount(parsedFromAmount || 0, sourceAccount.currency) : "-"}</strong>
                </div>
                <div>
                  <span>적용 환율</span>
                  <strong>
                    {rateSnapshot && sourceAccount && destinationAccount
                      ? `1 ${sourceAccount.currency} = ${rateSnapshot.rate.toFixed(4)} ${destinationAccount.currency}`
                      : "선택 필요"}
                  </strong>
                </div>
                <div>
                  <span>예상 환산</span>
                  <strong>
                    {expectedGrossReceiveAmount !== null && destinationAccount
                      ? formatAmount(expectedGrossReceiveAmount, destinationAccount.currency)
                      : "-"}
                  </strong>
                </div>
                <div>
                  <span>예상 수수료</span>
                  <strong>
                    {expectedFeeAmount !== null && destinationAccount
                      ? formatAmount(expectedFeeAmount, destinationAccount.currency)
                      : "-"}
                  </strong>
                </div>
                <div>
                  <span>순수령 예상</span>
                  <strong>
                    {expectedReceiveAmount !== null && destinationAccount
                      ? formatAmount(expectedReceiveAmount, destinationAccount.currency)
                      : "-"}
                  </strong>
                </div>
                <div>
                  <span>시세 시각</span>
                  <strong>{rateSnapshot ? new Date(rateSnapshot.effectiveAt).toLocaleString() : "실시간 환율 대기"}</strong>
                </div>
              </div>
              <button
                type="submit"
                className="primary-button trade-submit-button"
                disabled={submitting || !canSubmit}
              >
                {submitting ? "요청 중..." : "환전 요청"}
              </button>
              <p className="trade-summary-note">
                {destinationCandidates.length === 0
                  ? "환전 요청을 위해서는 통화가 다른 은행 계좌가 최소 2개 필요합니다."
                  : "요청 후 운영 승인 절차를 거쳐 정산됩니다."}
              </p>
            </aside>
          </div>
        </form>
      </section>

      <section className="timeline-panel">
        <div className="section-header">
          <div>
            <p className="eyebrow">Exchange Requests</p>
            <h2>내 환전 요청</h2>
          </div>
        </div>
        <div className="table-shell">
          <table className="data-table">
            <thead>
              <tr>
                <th>요청번호</th>
                <th>흐름</th>
                <th>환전 금액</th>
                <th>상태</th>
                <th>정산 거래</th>
                <th>생성일시</th>
              </tr>
            </thead>
            <tbody>
              {exchangeRequests.length ? (
                exchangeRequests.map((item) => (
                  <tr key={item.id}>
                    <td className="table-mono">{item.requestNumber}</td>
                    <td>
                      <div className="table-cell-stack">
                        <strong>{resolveAccountLabel(accountById.get(item.sourceAccountId ?? ""), item.sourceAccountId, item.fromCurrency)}</strong>
                        <p>to {resolveAccountLabel(accountById.get(item.destinationAccountId), item.destinationAccountId, item.toCurrency)}</p>
                      </div>
                    </td>
                    <td>
                      <div className="table-cell-stack">
                        <div>
                          <strong>{formatAmount(item.fromAmount, item.fromCurrency)}</strong>
                          <p>{`gross ${formatAmount(item.toAmount, item.toCurrency)}`}</p>
                          <p>{`fee ${formatAmount(item.exchangeFeeAmount, item.toCurrency)} / net ${formatAmount(item.netToAmount, item.toCurrency)}`}</p>
                        </div>
                      </div>
                    </td>
                    <td>
                      <b className={`status-pill ${item.status.toLowerCase()}`}>{item.status}</b>
                    </td>
                    <td>
                      <div className="table-cell-stack">
                        <div>
                          <strong>출금</strong>
                          <p className="table-mono">{item.sourceTransactionNumber ?? "정산 대기"}</p>
                        </div>
                        <div>
                          <strong>입금</strong>
                          <p className="table-mono">{item.destinationTransactionNumber ?? "정산 대기"}</p>
                        </div>
                      </div>
                    </td>
                    <td>{new Date(item.createdAt).toLocaleString()}</td>
                  </tr>
                ))
              ) : (
                <tr className="table-empty-row">
                  <td colSpan={6}>
                    <strong>환전 요청 내역이 없습니다.</strong>
                    <p>첫 환전 요청을 등록해 보세요.</p>
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

function resolveFxRate(sourceCurrency: string | undefined, destinationCurrency: string | undefined, fxRates: FxRate[]) {
  if (!sourceCurrency || !destinationCurrency) {
    return null;
  }

  const direct = fxRates.find(
    (rate) => rate.baseCurrency === sourceCurrency && rate.quoteCurrency === destinationCurrency,
  );
  if (direct) {
    return direct;
  }

  const inverse = fxRates.find(
    (rate) => rate.baseCurrency === destinationCurrency && rate.quoteCurrency === sourceCurrency,
  );
  if (!inverse || Number(inverse.rate) === 0) {
    return null;
  }

  return {
    ...inverse,
    rate: 1 / Number(inverse.rate),
  };
}

function resolveAccountLabel(account: Account | undefined, accountId: string | null, currency: string) {
  if (account) {
    return `${account.accountNumber} / ${account.currency}`;
  }
  if (!accountId) {
    return `legacy / ${currency}`;
  }
  return `${accountId.slice(0, 8)} / ${currency}`;
}
