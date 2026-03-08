import { useEffect, useState, type FormEvent } from "react";
import type { Account } from "../accounts/types";
import type { FxRate } from "../fx/types";
import type { CreateExchangeRequestPayload, ExchangeRequest } from "./types";
import { formatAmount } from "../../shared/utils/format";

const EXCHANGE_FEE_RATE = 0.0012;
const EXCHANGE_CUTOFF_HOUR = 16;
const KRW_MANUAL_REVIEW_THRESHOLD = 5_000_000;
const FX_MANUAL_REVIEW_THRESHOLD = 5_000;
const RATE_STALENESS_MANUAL_REVIEW_MINUTES = 30;

type ExchangeRequestsPageProps = {
  loading: boolean;
  submitting: boolean;
  cancelingRequestId: string | null;
  accounts: Account[];
  fxRates: FxRate[];
  exchangeRequests: ExchangeRequest[];
  onCreate: (payload: CreateExchangeRequestPayload) => Promise<void>;
  onCancel: (requestId: string, reason?: string) => Promise<void>;
};

export function ExchangeRequestsPage({
  loading,
  submitting,
  cancelingRequestId,
  accounts,
  fxRates,
  exchangeRequests,
  onCreate,
  onCancel,
}: ExchangeRequestsPageProps) {
  const bankingAccounts = [...accounts]
    .filter((account) => account.accountType === "BANKING")
    .sort((left, right) => left.currency.localeCompare(right.currency) || left.accountNumber.localeCompare(right.accountNumber));
  const [form, setForm] = useState({
    sourceAccountId: "",
    destinationAccountId: "",
    fromAmount: "1000",
    requestMemo: "투자 포트폴리오 리밸런싱",
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
  const sameDaySettlementEligiblePreview = isSameDaySettlementEligibleNow();
  const expectedSettlementPreview = buildExpectedSettlementPreview(sameDaySettlementEligiblePreview);
  const rateAgeMinutes = rateSnapshot ? Math.max(0, Math.floor((Date.now() - Date.parse(rateSnapshot.effectiveAt)) / 60_000)) : null;
  const manualReviewThreshold = resolveManualReviewThreshold(sourceAccount?.currency);
  const previewManualReviewReasons: string[] = [];
  if (parsedFromAmount > 0 && parsedFromAmount >= manualReviewThreshold) {
    previewManualReviewReasons.push("고액 환전 심사");
  }
  if (rateAgeMinutes !== null && rateAgeMinutes >= RATE_STALENESS_MANUAL_REVIEW_MINUTES) {
    previewManualReviewReasons.push("시세 지연 확인");
  }
  const previewManualReviewRequired = previewManualReviewReasons.length > 0;
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
      requestMemo: form.requestMemo.trim() || undefined,
    });
  }

  async function requestCancel(exchangeRequest: ExchangeRequest) {
    if (!canCancelExchangeRequest(exchangeRequest.status)) {
      return;
    }
    const reasonInput = window.prompt("취소 사유를 입력하세요. (선택)", "환전 계획 변경");
    if (reasonInput === null) {
      return;
    }
    await onCancel(exchangeRequest.id, reasonInput.trim() || undefined);
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
      <section className="summary-grid">
        <article className="summary-card">
          <span className="eyebrow">Exchange Requests</span>
          <strong>{exchangeRequests.length}</strong>
          <p>등록된 환전 요청 전체 건수</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Same Day Window</span>
          <strong>{sameDaySettlementEligiblePreview ? "당일 가능" : "익영업일"}</strong>
          <p>{`컷오프 ${EXCHANGE_CUTOFF_HOUR}:00 이후 요청은 익영업일로 이월됩니다.`}</p>
        </article>
        <article className={`summary-card ${previewManualReviewRequired ? "negative" : ""}`}>
          <span className="eyebrow">Review Status</span>
          <strong>{previewManualReviewRequired ? "수동 심사 예상" : "자동 큐"}</strong>
          <p>{previewManualReviewRequired ? previewManualReviewReasons.join(" / ") : "현재 입력 기준 추가 심사 사유 없음"}</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Live Rate</span>
          <strong>{rateSnapshot && sourceAccount && destinationAccount ? `${sourceAccount.currency}/${destinationAccount.currency}` : "환율 선택 필요"}</strong>
          <p>{rateSnapshot ? `기준 시각 ${new Date(rateSnapshot.effectiveAt).toLocaleString()}` : "서로 다른 통화 계좌를 선택하면 즉시 계산됩니다."}</p>
        </article>
      </section>

      <div className="page-convenience-strip">
        <div>
          <span>환전 안내</span>
          <strong>출금 통화와 입금 통화를 먼저 고른 뒤 금액을 입력하면 순수령 예상액이 바로 계산됩니다.</strong>
        </div>
        <div>
          <span>예상 순수령</span>
          <strong>{expectedReceiveAmount !== null && destinationAccount ? formatAmount(expectedReceiveAmount, destinationAccount.currency) : "-"}</strong>
        </div>
      </div>

      <section className="timeline-panel asset-panel">
        <div className="section-header compact">
          <div>
            <p className="eyebrow">FX Exchange</p>
            <h2>환전 요청</h2>
            <p className="section-copy">실무 화면처럼 환율, 시세 신선도, 정산 예상 시각을 위쪽에서 먼저 보여주고 입력 폼은 그 아래에 배치했습니다.</p>
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

              <div className="funding-policy-strip">
                <article className={`policy-chip ${sameDaySettlementEligiblePreview ? "positive" : "warning"}`}>
                  <span>정산 윈도우</span>
                  <strong>{sameDaySettlementEligiblePreview ? "당일 정산 가능" : "익영업일 정산"}</strong>
                  <p>{expectedSettlementPreview}</p>
                </article>
                <article className={`policy-chip ${rateAgeMinutes !== null && rateAgeMinutes >= RATE_STALENESS_MANUAL_REVIEW_MINUTES ? "warning" : "neutral"}`}>
                  <span>시세 신선도</span>
                  <strong>{rateAgeMinutes !== null ? `${rateAgeMinutes}분 경과` : "환율 선택 필요"}</strong>
                  <p>{rateSnapshot ? `기준 시각 ${new Date(rateSnapshot.effectiveAt).toLocaleString()}` : "환율 페어를 선택하면 기준 시각이 표시됩니다."}</p>
                </article>
                <article className={`policy-chip ${previewManualReviewRequired ? "negative" : "positive"}`}>
                  <span>심사 플래그</span>
                  <strong>{previewManualReviewRequired ? "수동 심사 예상" : "기본 승인 큐"}</strong>
                  <p>{previewManualReviewRequired ? previewManualReviewReasons.join(" / ") : "현재 입력 조건에서는 추가 심사 사유가 없습니다."}</p>
                </article>
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
                <label className="form-field">
                  <span>요청 메모</span>
                  <textarea
                    value={form.requestMemo}
                    maxLength={200}
                    onChange={(event) => setForm((current) => ({ ...current, requestMemo: event.target.value }))}
                    placeholder="예: 해외주식 매수 자금 환전"
                  />
                  <small className="form-helper">{`${form.requestMemo.length}/200`}</small>
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
                <div>
                  <span>정산 예상 시각</span>
                  <strong>{expectedSettlementPreview}</strong>
                </div>
                <div>
                  <span>심사 플래그</span>
                  <strong>{previewManualReviewRequired ? previewManualReviewReasons.join(" / ") : "추가 심사 없음"}</strong>
                </div>
                <div className="trade-summary-memo">
                  <span>요청 메모</span>
                  <strong>{form.requestMemo.trim() ? form.requestMemo.trim() : "미입력"}</strong>
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
            <p className="section-copy">환전 흐름, 정산 결과, 취소 가능 여부를 표에서 바로 확인할 수 있습니다.</p>
          </div>
        </div>
        <div className="table-shell">
          <table className="data-table">
            <colgroup>
              <col style={{ width: 180 }} />
              <col style={{ width: 210 }} />
              <col style={{ width: 240 }} />
              <col style={{ width: 180 }} />
              <col style={{ width: 170 }} />
              <col style={{ width: 180 }} />
              <col style={{ width: 200 }} />
              <col style={{ width: 230 }} />
              <col style={{ width: 120 }} />
              <col style={{ width: 180 }} />
            </colgroup>
            <thead>
              <tr>
                <th>요청번호</th>
                <th>흐름</th>
                <th>환전 금액</th>
                <th>요청 메모</th>
                <th>상태</th>
                <th>정책 플래그</th>
                <th>예상 정산</th>
                <th>정산 거래</th>
                <th>액션</th>
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
                      <div className="table-cell-stack">
                        <p className="table-note">{item.requestMemo ?? "미입력"}</p>
                        {item.status === "CANCELED" ? (
                          <p className="table-note">{`취소 사유: ${item.cancellationReason ?? "사용자 요청 취소"}`}</p>
                        ) : null}
                      </div>
                    </td>
                    <td>
                      <b className={`status-pill ${item.status.toLowerCase()}`}>{item.status}</b>
                    </td>
                    <td>
                      <div className="table-cell-stack">
                        <div>
                          <strong>{item.sameDaySettlementEligible ? "당일 정산" : "익영업일 정산"}</strong>
                          <p>{item.manualReviewRequired ? item.manualReviewReason ?? "수동 심사" : "추가 심사 없음"}</p>
                        </div>
                      </div>
                    </td>
                    <td>
                      <div className="table-cell-stack">
                        <div>
                          <strong>{item.expectedSettlementAt ? new Date(item.expectedSettlementAt).toLocaleString() : "-"}</strong>
                          <p>{`rate ${new Date(item.appliedRateEffectiveAt).toLocaleString()}`}</p>
                        </div>
                      </div>
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
                    <td className="table-action-cell">
                      {canCancelExchangeRequest(item.status) ? (
                        <button
                          type="button"
                          className="secondary-button compact"
                          disabled={cancelingRequestId === item.id}
                          onClick={() => void requestCancel(item)}
                        >
                          {cancelingRequestId === item.id ? "취소 중..." : "요청 취소"}
                        </button>
                      ) : (
                        <span className="table-muted">-</span>
                      )}
                    </td>
                    <td>{new Date(item.createdAt).toLocaleString()}</td>
                  </tr>
                ))
              ) : (
                <tr className="table-empty-row">
                  <td colSpan={10}>
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

function resolveManualReviewThreshold(currency: string | undefined) {
  return currency?.toUpperCase() === "KRW" ? KRW_MANUAL_REVIEW_THRESHOLD : FX_MANUAL_REVIEW_THRESHOLD;
}

function canCancelExchangeRequest(status: string) {
  return status === "PENDING_APPROVAL";
}

function isSameDaySettlementEligibleNow() {
  const now = new Date();
  const day = now.getDay();
  const businessDay = day >= 1 && day <= 5;
  const beforeCutoff = now.getHours() < EXCHANGE_CUTOFF_HOUR
    || (now.getHours() === EXCHANGE_CUTOFF_HOUR && now.getMinutes() === 0 && now.getSeconds() === 0);
  return businessDay && beforeCutoff;
}

function buildExpectedSettlementPreview(sameDaySettlementEligible: boolean) {
  const now = new Date();
  if (sameDaySettlementEligible) {
    const sameDay = new Date(now);
    sameDay.setHours(17, 30, 0, 0);
    return sameDay.toLocaleString();
  }

  const nextBusinessDay = new Date(now);
  do {
    nextBusinessDay.setDate(nextBusinessDay.getDate() + 1);
  } while (nextBusinessDay.getDay() === 0 || nextBusinessDay.getDay() === 6);
  nextBusinessDay.setHours(10, 30, 0, 0);
  return nextBusinessDay.toLocaleString();
}
