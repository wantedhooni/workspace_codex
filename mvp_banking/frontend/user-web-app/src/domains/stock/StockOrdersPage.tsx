import { useEffect, useState, type FormEvent } from "react";
import type { Account } from "../accounts/types";
import type { CreateStockOrderPayload, StockOrder, StockPosition } from "./types";
import { formatAmount } from "../../shared/utils/format";

const TRADING_FEE_RATE = 0.0015;
const SELL_TAX_RATE = 0.0023;

type StockOrdersPageProps = {
  loading: boolean;
  submitting: boolean;
  accounts: Account[];
  stockPositions: StockPosition[];
  stockOrders: StockOrder[];
  onCreate: (payload: CreateStockOrderPayload) => Promise<void>;
};

export function StockOrdersPage({ loading, submitting, accounts, stockPositions, stockOrders, onCreate }: StockOrdersPageProps) {
  const securitiesAccounts = accounts.filter((account) => account.accountType === "SECURITIES");
  const [form, setForm] = useState({
    accountId: "",
    symbol: "AAPL",
    market: "NASDAQ",
    side: "BUY",
    quantity: "10",
    limitPrice: "180",
    currency: "USD",
  });

  useEffect(() => {
    if (securitiesAccounts.length && !form.accountId) {
      setForm((current) => ({ ...current, accountId: securitiesAccounts[0].id }));
    }
  }, [securitiesAccounts, form.accountId]);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await onCreate({
      accountId: form.accountId,
      symbol: form.symbol,
      market: form.market,
      side: form.side,
      quantity: Number(form.quantity),
      limitPrice: Number(form.limitPrice),
      currency: form.currency,
    });
  }

  const selectedAccount = securitiesAccounts.find((account) => account.id === form.accountId);
  const normalizedSymbol = form.symbol.trim().toUpperCase();
  const matchedPosition = stockPositions.find(
    (position) => position.accountId === form.accountId && position.symbol.toUpperCase() === normalizedSymbol,
  );
  const parsedQuantity = Number(form.quantity || 0);
  const parsedLimitPrice = Number(form.limitPrice || 0);
  const estimatedNotional = parsedQuantity > 0 && parsedLimitPrice > 0
    ? parsedQuantity * parsedLimitPrice
    : 0;
  const isSell = form.side === "SELL";
  const estimatedFeeAmount = estimatedNotional * TRADING_FEE_RATE;
  const estimatedTaxAmount = isSell ? estimatedNotional * SELL_TAX_RATE : 0;
  const estimatedCashImpact = isSell
    ? estimatedNotional - estimatedFeeAmount - estimatedTaxAmount
    : estimatedNotional + estimatedFeeAmount;
  const hasEnoughShares = !isSell || Number(matchedPosition?.quantity ?? 0) >= parsedQuantity;
  const sameCurrencyCashCheck = selectedAccount && selectedAccount.currency === form.currency;
  const hasEnoughCash = !sameCurrencyCashCheck || Number(selectedAccount?.balance ?? 0) >= estimatedCashImpact;
  const canSubmit = Boolean(
    form.accountId
    && normalizedSymbol
    && form.market
    && form.currency
    && parsedQuantity > 0
    && parsedLimitPrice > 0
    && hasEnoughShares
    && hasEnoughCash,
  );

  if (loading) {
    return (
      <section className="timeline-panel">
        <p>주식 주문 정보를 불러오는 중입니다...</p>
      </section>
    );
  }

  return (
    <>
      <section className="timeline-panel asset-panel">
        <div className="section-header compact">
          <div>
            <p className="eyebrow">Stock Trading</p>
            <h2>주식 주문</h2>
          </div>
        </div>
        <form className="trade-ticket" onSubmit={onSubmit}>
          <div className="trade-ticket-grid">
            <div className="trade-ticket-main">
              <article className="trade-leg-card">
                <div className="trade-leg-header">
                  <div>
                    <span className="trade-kicker">Account</span>
                    <strong>주문 계좌</strong>
                  </div>
                  {selectedAccount ? <b className={`status-pill ${selectedAccount.status.toLowerCase()}`}>{selectedAccount.status}</b> : null}
                </div>
                <label className="form-field">
                  <span>어느 계좌에서 주문할까요?</span>
                  <select value={form.accountId} onChange={(event) => setForm((current) => ({ ...current, accountId: event.target.value }))} required>
                    <option value="">증권 계좌 선택</option>
                    {securitiesAccounts.map((account) => (
                      <option key={account.id} value={account.id}>
                        {account.accountNumber} / {account.currency}
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
                    <span>주문 가능 현금</span>
                    <strong>{selectedAccount ? formatAmount(selectedAccount.balance, selectedAccount.currency) : "-"}</strong>
                  </div>
                </div>
              </article>

              <div className="trade-form-grid">
                <label className="form-field">
                  <span>종목 코드</span>
                  <input
                    value={form.symbol}
                    onChange={(event) => setForm((current) => ({ ...current, symbol: event.target.value.toUpperCase() }))}
                    placeholder="예: AAPL"
                    required
                  />
                  <small className="form-helper">미국 주식은 티커 기준으로 입력합니다.</small>
                </label>
                <label className="form-field">
                  <span>시장</span>
                  <input
                    value={form.market}
                    onChange={(event) => setForm((current) => ({ ...current, market: event.target.value.toUpperCase() }))}
                    placeholder="예: NASDAQ"
                    required
                  />
                </label>
              </div>

              <div className="trade-form-grid">
                <div className="form-field">
                  <span>주문 방향</span>
                  <div className="trade-toggle-row">
                    <button
                      type="button"
                      className={form.side === "BUY" ? "active" : ""}
                      onClick={() => setForm((current) => ({ ...current, side: "BUY" }))}
                    >
                      BUY
                    </button>
                    <button
                      type="button"
                      className={form.side === "SELL" ? "active" : ""}
                      onClick={() => setForm((current) => ({ ...current, side: "SELL" }))}
                    >
                      SELL
                    </button>
                  </div>
                </div>
                <label className="form-field">
                  <span>결제 통화</span>
                  <input
                    value={form.currency}
                    onChange={(event) => setForm((current) => ({ ...current, currency: event.target.value.toUpperCase() }))}
                    placeholder="예: USD"
                    required
                  />
                </label>
              </div>

              <div className="trade-form-grid">
                <label className="form-field">
                  <span>주문 수량</span>
                  <input
                    type="number"
                    min="0.0001"
                    step="0.0001"
                    value={form.quantity}
                    onChange={(event) => setForm((current) => ({ ...current, quantity: event.target.value }))}
                    placeholder="예: 10"
                    required
                  />
                  <small className="form-helper">
                    {isSell
                      ? `보유 수량: ${Number(matchedPosition?.quantity ?? 0).toLocaleString()}주`
                      : "주문하려는 수량을 입력하세요."}
                  </small>
                </label>
                <label className="form-field">
                  <span>지정가</span>
                  <input
                    type="number"
                    min="0.0001"
                    step="0.0001"
                    value={form.limitPrice}
                    onChange={(event) => setForm((current) => ({ ...current, limitPrice: event.target.value }))}
                    placeholder="예: 180"
                    required
                  />
                  <small className="form-helper">{`${form.currency} 기준 지정가입니다.`}</small>
                </label>
              </div>
            </div>

            <aside className="trade-summary-card">
              <span className="trade-kicker">Order Ticket</span>
              <h3>주문 요약</h3>
              <div className="trade-summary-list">
                <div>
                  <span>주문 방향</span>
                  <strong>{form.side}</strong>
                </div>
                <div>
                  <span>종목 / 시장</span>
                  <strong>{normalizedSymbol ? `${normalizedSymbol} / ${form.market}` : "입력 필요"}</strong>
                </div>
                <div>
                  <span>예상 주문 금액</span>
                  <strong>{estimatedNotional > 0 ? formatAmount(estimatedNotional, form.currency) : "-"}</strong>
                </div>
                <div>
                  <span>예상 수수료</span>
                  <strong>{estimatedNotional > 0 ? formatAmount(estimatedFeeAmount, form.currency) : "-"}</strong>
                </div>
                <div>
                  <span>예상 세금</span>
                  <strong>{isSell && estimatedNotional > 0 ? formatAmount(estimatedTaxAmount, form.currency) : formatAmount(0, form.currency)}</strong>
                </div>
                <div>
                  <span>계좌 현금</span>
                  <strong>{selectedAccount ? formatAmount(selectedAccount.balance, selectedAccount.currency) : "-"}</strong>
                </div>
                <div>
                  <span>보유 수량</span>
                  <strong>{matchedPosition ? `${Number(matchedPosition.quantity).toLocaleString()}주` : "0주"}</strong>
                </div>
                <div>
                  <span>{isSell ? "예상 순수령" : "예상 출금"}</span>
                  <strong>{estimatedNotional > 0 ? formatAmount(estimatedCashImpact, form.currency) : "-"}</strong>
                </div>
              </div>
              {!hasEnoughShares ? <p className="trade-summary-note warning">현재 보유 수량보다 많은 매도 주문입니다.</p> : null}
              {!hasEnoughCash && sameCurrencyCashCheck ? <p className="trade-summary-note warning">계좌 현금보다 주문 금액이 큽니다.</p> : null}
              {selectedAccount && selectedAccount.currency !== form.currency ? (
                <p className="trade-summary-note">
                  계좌 통화는 {selectedAccount.currency}, 주문 통화는 {form.currency}입니다.
                </p>
              ) : null}
              <button type="submit" className="primary-button trade-submit-button" disabled={submitting || !canSubmit}>
                {submitting ? "주문 중..." : "주문 요청"}
              </button>
            </aside>
          </div>
        </form>
      </section>

      <section className="timeline-panel">
        <div className="section-header">
          <div>
            <p className="eyebrow">Stock Orders</p>
            <h2>내 주식 주문</h2>
          </div>
        </div>
        <div className="table-shell">
          <table className="data-table">
            <thead>
              <tr>
                <th>주문번호</th>
                <th>종목</th>
                <th>주문 내용</th>
                <th>상태</th>
                <th>체결 정보</th>
                <th>정산 결과</th>
                <th>잔여 수량</th>
                <th>생성일시</th>
              </tr>
            </thead>
            <tbody>
              {stockOrders.length ? (
                stockOrders.map((item) => (
                  <tr key={item.id}>
                    <td className="table-mono">{item.orderNumber}</td>
                    <td>
                      <div className="table-cell-stack">
                        <div>
                          <strong>{item.symbol}</strong>
                          <p>{item.market}</p>
                        </div>
                      </div>
                    </td>
                    <td>
                      <div className="table-cell-stack">
                        <div>
                          <strong>{item.side}</strong>
                          <p>
                            {Number(item.quantity).toLocaleString()} @ {formatAmount(item.limitPrice, item.currency)}
                          </p>
                        </div>
                      </div>
                    </td>
                    <td>
                      <b className={`status-pill ${item.status.toLowerCase()}`}>{item.status}</b>
                    </td>
                    <td>
                      {item.executions.length ? (
                        <div className="table-cell-stack">
                          <div>
                            <strong>{Number(item.executedQuantity ?? 0).toLocaleString()} / {Number(item.quantity).toLocaleString()} filled</strong>
                            <p>@ {formatAmount(item.executedPrice ?? 0, item.currency)}</p>
                          </div>
                          {item.executions.map((execution) => (
                            <div key={execution.id}>
                              <strong className="table-mono">{execution.executionNumber}</strong>
                              <p>
                                {Number(execution.executedQuantity).toLocaleString()} @ {formatAmount(execution.executedPrice, item.currency)}
                              </p>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <span className="table-muted">체결 전</span>
                      )}
                    </td>
                    <td>
                      <div className="table-cell-stack">
                        <div>
                          <strong>{item.status === "PENDING_APPROVAL" ? "정산 대기" : formatAmount(item.netSettlementAmount, item.currency)}</strong>
                          <p>{`fee ${formatAmount(item.feeAmount, item.currency)} / tax ${formatAmount(item.taxAmount, item.currency)}`}</p>
                        </div>
                      </div>
                    </td>
                    <td>
                      <div className="table-cell-stack">
                        <div>
                          <strong>{Number(item.remainingQuantity ?? item.quantity).toLocaleString()}</strong>
                          <p>{Math.round(Number(item.fillRate ?? 0) * 100)}% filled</p>
                        </div>
                      </div>
                    </td>
                    <td>{new Date(item.createdAt).toLocaleString()}</td>
                  </tr>
                ))
              ) : (
                <tr className="table-empty-row">
                  <td colSpan={8}>
                    <strong>주식 주문 내역이 없습니다.</strong>
                    <p>첫 주문을 등록해 보세요.</p>
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
