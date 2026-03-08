import { useEffect, useState, type FormEvent } from "react";
import type { ColDef } from "ag-grid-community";
import type { Account } from "../accounts/types";
import type { CreateStockOrderPayload, StockOrder, StockPosition } from "./types";
import type { PageResponse } from "../../shared/types/page";
import { formatAmount } from "../../shared/utils/format";
import { AppGridTable } from "../../shared/components/AppGridTable";

const TRADING_FEE_RATE = 0.0015;
const SELL_TAX_RATE = 0.0023;
const LARGE_NOTIONAL_REVIEW_THRESHOLD = 50_000;
const PRICE_DEVIATION_REVIEW_THRESHOLD = 0.03;

type StockOrdersPageProps = {
  loading: boolean;
  submitting: boolean;
  cancelingOrderId: string | null;
  accounts: Account[];
  stockPositions: StockPosition[];
  stockOrders: StockOrder[];
  stockOrderRows: StockOrder[];
  stockOrderPage: PageResponse<StockOrder>;
  onStockOrderPageChange: (page: number, pageSize: number) => void;
  onCreate: (payload: CreateStockOrderPayload) => Promise<void>;
  onCancel: (orderId: string, reason?: string) => Promise<void>;
};

type StockOrderCellParams = { data?: StockOrder };

export function StockOrdersPage({
  loading,
  submitting,
  cancelingOrderId,
  accounts,
  stockPositions,
  stockOrders,
  stockOrderRows,
  stockOrderPage,
  onStockOrderPageChange,
  onCreate,
  onCancel,
}: StockOrdersPageProps) {
  const securitiesAccounts = accounts.filter((account) => account.accountType === "SECURITIES");
  const [form, setForm] = useState({
    accountId: "",
    symbol: "AAPL",
    market: "NASDAQ",
    side: "BUY",
    timeInForce: "DAY",
    quantity: "10",
    limitPrice: "180",
    currency: "USD",
    orderMemo: "장기 성장 포트폴리오 분할 매수",
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
      timeInForce: form.timeInForce,
      orderMemo: form.orderMemo.trim() || undefined,
    });
  }

  async function requestCancel(order: StockOrder) {
    if (!canCancelOrder(order.status)) {
      return;
    }
    const reasonInput = window.prompt("취소 사유를 입력하세요. (선택)", "전략 변경");
    if (reasonInput === null) {
      return;
    }
    await onCancel(order.id, reasonInput.trim() || undefined);
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
  const marketSessionPreview = resolveMarketSessionPreview(form.market);
  const expectedExecutionPreview = buildExpectedExecutionPreview(marketSessionPreview, form.market);
  const expiresAtPreview = buildExpiryPreview(form.timeInForce, marketSessionPreview, form.market);
  const referencePricePreview = Number(matchedPosition?.currentPrice ?? 0) > 0 ? Number(matchedPosition?.currentPrice) : null;
  const priceDeviationPreview = referencePricePreview && parsedLimitPrice > 0
    ? Math.abs(parsedLimitPrice - referencePricePreview) / referencePricePreview
    : null;
  const previewManualReviewReasons: string[] = [];
  if (estimatedNotional >= LARGE_NOTIONAL_REVIEW_THRESHOLD) {
    previewManualReviewReasons.push("고액 주문 심사");
  }
  if (marketSessionPreview !== "REGULAR") {
    previewManualReviewReasons.push("장외 시간 주문");
  }
  if (priceDeviationPreview !== null && priceDeviationPreview >= PRICE_DEVIATION_REVIEW_THRESHOLD) {
    previewManualReviewReasons.push("시세 대비 지정가 괴리");
  }
  if (referencePricePreview === null && normalizedSymbol) {
    previewManualReviewReasons.push("기준 시세 미확인");
  }
  const iocOutsideRegular = form.timeInForce === "IOC" && marketSessionPreview !== "REGULAR";
  if (iocOutsideRegular) {
    previewManualReviewReasons.push("IOC는 정규장에서만 허용");
  }
  const previewManualReviewRequired = previewManualReviewReasons.length > 0;
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
    && hasEnoughCash
    && !iocOutsideRegular
  );
  const columnDefs: ColDef<StockOrder>[] = [
    { headerName: "주문번호", field: "orderNumber", minWidth: 190, cellClass: "table-mono" },
    {
      headerName: "종목",
      minWidth: 150,
      sortable: false,
      cellRenderer: ({ data }: StockOrderCellParams) => data ? (
        <div className="table-cell-stack">
          <div>
            <strong>{data.symbol}</strong>
            <p>{data.market}</p>
          </div>
        </div>
      ) : "-",
    },
    {
      headerName: "주문 조건",
      minWidth: 220,
      sortable: false,
      cellRenderer: ({ data }: StockOrderCellParams) => data ? (
        <div className="table-cell-stack">
          <div>
            <strong>{data.side} · {renderTimeInForceLabel(data.timeInForce)}</strong>
            <p>{Number(data.quantity).toLocaleString()} @ {formatAmount(data.limitPrice, data.currency)}</p>
            <p>{data.orderMemo ?? "메모 없음"}</p>
          </div>
        </div>
      ) : "-",
    },
    {
      headerName: "상태",
      minWidth: 170,
      cellRenderer: ({ data }: StockOrderCellParams) => (data ? <b className={`status-pill ${data.status.toLowerCase()}`}>{data.status}</b> : "-"),
    },
    {
      headerName: "정책 플래그",
      minWidth: 200,
      sortable: false,
      cellRenderer: ({ data }: StockOrderCellParams) => data ? (
        <div className="table-cell-stack">
          <div>
            <strong>{data.manualReviewRequired ? "수동 심사" : "기본 승인"}</strong>
            <p>{data.status === "CANCELED" ? data.cancellationReason ?? "사용자 요청 취소" : data.manualReviewRequired ? data.manualReviewReason ?? "추가 심사" : "추가 심사 없음"}</p>
          </div>
        </div>
      ) : "-",
    },
    {
      headerName: "체결 윈도우",
      minWidth: 210,
      sortable: false,
      cellRenderer: ({ data }: StockOrderCellParams) => data ? (
        <div className="table-cell-stack">
          <div>
            <strong>{renderMarketSessionLabel(data.marketSession)}</strong>
            <p>{new Date(data.expectedExecutionAt).toLocaleString()}</p>
            <p>{data.referencePrice !== null ? `ref ${formatAmount(data.referencePrice, data.currency)} / dev ${data.priceDeviationRate !== null ? formatRatePercent(data.priceDeviationRate) : "-"}` : "기준 시세 없음"}</p>
          </div>
        </div>
      ) : "-",
    },
    {
      headerName: "유효 만료",
      minWidth: 190,
      sortable: false,
      cellRenderer: ({ data }: StockOrderCellParams) => data ? (
        <div className="table-cell-stack">
          <div>
            <strong>{new Date(data.expiresAt).toLocaleString()}</strong>
            <p>{data.timeInForce} 기준 만료</p>
          </div>
        </div>
      ) : "-",
    },
    {
      headerName: "체결 정보",
      minWidth: 230,
      sortable: false,
      cellRenderer: ({ data }: StockOrderCellParams) => data ? (
        data.executions.length ? (
          <div className="table-cell-stack">
            <div>
              <strong>{Number(data.executedQuantity ?? 0).toLocaleString()} / {Number(data.quantity).toLocaleString()} filled</strong>
              <p>@ {formatAmount(data.executedPrice ?? 0, data.currency)}</p>
            </div>
            {data.executions.map((execution: StockOrder["executions"][number]) => (
              <div key={execution.id}>
                <strong className="table-mono">{execution.executionNumber}</strong>
                <p>{Number(execution.executedQuantity).toLocaleString()} @ {formatAmount(execution.executedPrice, data.currency)}</p>
              </div>
            ))}
          </div>
        ) : (
          <span className="table-muted">체결 전</span>
        )
      ) : "-",
    },
    {
      headerName: "정산 결과",
      minWidth: 190,
      sortable: false,
      cellRenderer: ({ data }: StockOrderCellParams) => data ? (
        <div className="table-cell-stack">
          <div>
            <strong>
              {data.status === "PENDING_APPROVAL"
                ? "정산 대기"
                : data.status === "CANCELED"
                  ? "주문 취소"
                  : formatAmount(data.netSettlementAmount, data.currency)}
            </strong>
            <p>
              {data.status === "CANCELED" && data.canceledAt
                ? `취소 시각 ${new Date(data.canceledAt).toLocaleString()}`
                : `fee ${formatAmount(data.feeAmount, data.currency)} / tax ${formatAmount(data.taxAmount, data.currency)}`}
            </p>
          </div>
        </div>
      ) : "-",
    },
    {
      headerName: "액션",
      minWidth: 120,
      sortable: false,
      cellRenderer: ({ data }: StockOrderCellParams) => data ? (
        canCancelOrder(data.status) ? (
          <button
            type="button"
            className="secondary-button compact"
            disabled={cancelingOrderId === data.id}
            onClick={() => void requestCancel(data)}
          >
            {cancelingOrderId === data.id ? "취소 중..." : "주문 취소"}
          </button>
        ) : (
          <span className="table-muted">-</span>
        )
      ) : "-",
    },
    {
      headerName: "생성일시",
      minWidth: 180,
      cellRenderer: ({ data }: StockOrderCellParams) => (data ? new Date(data.createdAt).toLocaleString() : "-"),
    },
  ];

  if (loading) {
    return (
      <section className="timeline-panel">
        <p>주식 주문 정보를 불러오는 중입니다...</p>
      </section>
    );
  }

  return (
    <>
      <section className="summary-grid">
        <article className="summary-card">
          <span className="eyebrow">Open Orders</span>
          <strong>{stockOrders.filter((item) => item.status === "PENDING_APPROVAL" || item.status === "PARTIALLY_FILLED").length}</strong>
          <p>승인 대기 및 부분체결 주문 수</p>
        </article>
        <article className={`summary-card ${previewManualReviewRequired ? "negative" : ""}`}>
          <span className="eyebrow">Review Status</span>
          <strong>{previewManualReviewRequired ? "수동 심사 예상" : "자동 큐"}</strong>
          <p>{previewManualReviewRequired ? previewManualReviewReasons.join(" / ") : "현재 입력 기준 추가 심사 사유 없음"}</p>
        </article>
        <article className={`summary-card ${isSell && !hasEnoughShares ? "negative" : ""}`}>
          <span className="eyebrow">Exposure Check</span>
          <strong>{estimatedNotional > 0 ? formatAmount(estimatedNotional, form.currency) : "-"}</strong>
          <p>{isSell ? "보유 수량 기준 매도 가능 여부를 확인합니다." : "주문 예상 금액과 현금 영향도를 미리 계산합니다."}</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Execution Window</span>
          <strong>{renderMarketSessionLabel(marketSessionPreview)}</strong>
          <p>{`예상 체결 ${expectedExecutionPreview}`}</p>
        </article>
      </section>

      <div className="page-convenience-strip">
        <div>
          <span>주문 안내</span>
          <strong>종목, 방향, 수량, 지정가를 입력하면 세금·수수료와 현금 영향도가 함께 계산됩니다.</strong>
        </div>
        <div>
          <span>{isSell ? "예상 순수령" : "예상 출금"}</span>
          <strong>{estimatedNotional > 0 ? formatAmount(estimatedCashImpact, form.currency) : "-"}</strong>
        </div>
      </div>

      <section className="timeline-panel asset-panel">
        <div className="section-header compact">
          <div>
            <p className="eyebrow">Stock Trading</p>
            <h2>주식 주문</h2>
            <p className="section-copy">실제 주문 티켓처럼 계좌 정보, 정책 플래그, 체결 윈도우를 먼저 확인하고 주문 조건을 입력하도록 정리했습니다.</p>
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
                <div className="form-field">
                  <span>유효 조건 (TIF)</span>
                  <div className="trade-toggle-row triple">
                    <button
                      type="button"
                      className={form.timeInForce === "DAY" ? "active" : ""}
                      onClick={() => setForm((current) => ({ ...current, timeInForce: "DAY" }))}
                    >
                      DAY
                    </button>
                    <button
                      type="button"
                      className={form.timeInForce === "GTC" ? "active" : ""}
                      onClick={() => setForm((current) => ({ ...current, timeInForce: "GTC" }))}
                    >
                      GTC
                    </button>
                    <button
                      type="button"
                      className={form.timeInForce === "IOC" ? "active" : ""}
                      onClick={() => setForm((current) => ({ ...current, timeInForce: "IOC" }))}
                    >
                      IOC
                    </button>
                  </div>
                  <small className="form-helper">{renderTimeInForceHelper(form.timeInForce)}</small>
                </div>
                <div className="form-field">
                  <span>유효 만료 예상</span>
                  <input value={expiresAtPreview} readOnly />
                  <small className="form-helper">시장 세션과 TIF 기준의 예상 만료 시각입니다.</small>
                </div>
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

              <div className="funding-policy-strip">
                <article className={`policy-chip ${marketSessionPreview === "REGULAR" ? "positive" : "warning"}`}>
                  <span>시장 세션</span>
                  <strong>{renderMarketSessionLabel(marketSessionPreview)}</strong>
                  <p>{`예상 체결 시작 ${expectedExecutionPreview}`}</p>
                </article>
                <article className={`policy-chip ${iocOutsideRegular ? "negative" : "neutral"}`}>
                  <span>유효 조건</span>
                  <strong>{renderTimeInForceLabel(form.timeInForce)}</strong>
                  <p>{`예상 만료 ${expiresAtPreview}`}</p>
                </article>
                <article className={`policy-chip ${priceDeviationPreview !== null && priceDeviationPreview >= PRICE_DEVIATION_REVIEW_THRESHOLD ? "negative" : "neutral"}`}>
                  <span>가격 괴리</span>
                  <strong>{priceDeviationPreview !== null ? formatRatePercent(priceDeviationPreview) : "기준 시세 없음"}</strong>
                  <p>
                    {referencePricePreview !== null
                      ? `기준가 ${formatAmount(referencePricePreview, form.currency)}`
                      : "현재 보유/시세 데이터가 없어 괴리 계산을 보류합니다."}
                  </p>
                </article>
                <article className={`policy-chip ${previewManualReviewRequired ? "warning" : "positive"}`}>
                  <span>심사 플래그</span>
                  <strong>{previewManualReviewRequired ? "수동 심사 예상" : "기본 승인 큐"}</strong>
                  <p>{previewManualReviewRequired ? previewManualReviewReasons.join(" / ") : "현재 입력 기준 추가 심사 사유 없음"}</p>
                </article>
              </div>

              <div className="trade-form-grid single-column">
                <label className="form-field">
                  <span>요청 메모</span>
                  <textarea
                    value={form.orderMemo}
                    maxLength={200}
                    onChange={(event) => setForm((current) => ({ ...current, orderMemo: event.target.value }))}
                    placeholder="예: 실적 발표 전 분할 매수"
                  />
                  <small className="form-helper">{`${form.orderMemo.length}/200`}</small>
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
                  <span>유효 조건</span>
                  <strong>{renderTimeInForceLabel(form.timeInForce)}</strong>
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
                <div>
                  <span>시장 세션</span>
                  <strong>{renderMarketSessionLabel(marketSessionPreview)}</strong>
                </div>
                <div>
                  <span>예상 체결 시작</span>
                  <strong>{expectedExecutionPreview}</strong>
                </div>
                <div>
                  <span>예상 만료</span>
                  <strong>{expiresAtPreview}</strong>
                </div>
                <div>
                  <span>심사 플래그</span>
                  <strong>{previewManualReviewRequired ? previewManualReviewReasons.join(" / ") : "추가 심사 없음"}</strong>
                </div>
                <div className="trade-summary-memo">
                  <span>요청 메모</span>
                  <strong>{form.orderMemo.trim() ? form.orderMemo.trim() : "미입력"}</strong>
                </div>
              </div>
              {!hasEnoughShares ? <p className="trade-summary-note warning">현재 보유 수량보다 많은 매도 주문입니다.</p> : null}
              {!hasEnoughCash && sameCurrencyCashCheck ? <p className="trade-summary-note warning">계좌 현금보다 주문 금액이 큽니다.</p> : null}
              {iocOutsideRegular ? <p className="trade-summary-note warning">IOC 주문은 정규장에서만 접수할 수 있습니다.</p> : null}
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
            <p className="section-copy">체결 진척, 정산 결과, 취소 가능 상태를 표에서 한 번에 읽을 수 있도록 요약했습니다.</p>
          </div>
        </div>
        <AppGridTable
          rowData={stockOrderRows}
          columnDefs={columnDefs}
          emptyMessage="주식 주문 내역이 없습니다."
          getRowId={(row) => row.id}
          pagination={{
            current: stockOrderPage.page + 1,
            pageSize: stockOrderPage.size,
            total: stockOrderPage.totalElements,
            onChange: onStockOrderPageChange,
          }}
        />
      </section>
    </>
  );
}

function resolveMarketSessionPreview(market: string) {
  const normalizedMarket = market.trim().toUpperCase();
  if (normalizedMarket !== "NASDAQ" && normalizedMarket !== "NYSE") {
    return "REGULAR";
  }

  const marketClock = getNewYorkMarketClock(new Date());
  if (marketClock.weekday === "Sat" || marketClock.weekday === "Sun") {
    return "CLOSED";
  }
  const totalMinutes = marketClock.hour * 60 + marketClock.minute;
  const openMinutes = 9 * 60 + 30;
  const closeMinutes = 16 * 60;
  if (totalMinutes < openMinutes) {
    return "PRE_MARKET";
  }
  if (totalMinutes < closeMinutes) {
    return "REGULAR";
  }
  return "AFTER_HOURS";
}

function buildExpectedExecutionPreview(marketSession: string, market: string) {
  const normalizedMarket = market.trim().toUpperCase();
  if (normalizedMarket !== "NASDAQ" && normalizedMarket !== "NYSE") {
    return "즉시 체결 큐 진입";
  }
  if (marketSession === "REGULAR") {
    return "즉시 체결 큐 진입";
  }
  if (marketSession === "PRE_MARKET") {
    return "금일 09:30 ET";
  }
  return "다음 영업일 09:30 ET";
}

function buildExpiryPreview(timeInForce: string, marketSession: string, market: string) {
  if (timeInForce === "IOC") {
    return "접수 후 약 2분";
  }
  if (timeInForce === "GTC") {
    return "접수 시점 + 30일";
  }

  const normalizedMarket = market.trim().toUpperCase();
  if (normalizedMarket !== "NASDAQ" && normalizedMarket !== "NYSE") {
    return "접수 시점 + 1일";
  }
  if (marketSession === "AFTER_HOURS" || marketSession === "CLOSED") {
    return "다음 영업일 16:00 ET";
  }
  return "당일 16:00 ET";
}

function renderMarketSessionLabel(session: string) {
  switch (session) {
    case "PRE_MARKET":
      return "프리마켓";
    case "AFTER_HOURS":
      return "애프터마켓";
    case "CLOSED":
      return "휴장";
    default:
      return "정규장";
  }
}

function renderTimeInForceLabel(timeInForce: string) {
  switch (timeInForce) {
    case "IOC":
      return "IOC (즉시체결/잔량취소)";
    case "GTC":
      return "GTC (30일 유지)";
    default:
      return "DAY (당일 유효)";
  }
}

function renderTimeInForceHelper(timeInForce: string) {
  switch (timeInForce) {
    case "IOC":
      return "즉시 체결 가능한 수량만 체결되고 남은 수량은 자동 취소됩니다.";
    case "GTC":
      return "최대 30일 동안 주문을 유지합니다.";
    default:
      return "정규장 마감 시점(16:00 ET)까지 유효합니다.";
  }
}

function canCancelOrder(status: string) {
  return status === "PENDING_APPROVAL" || status === "PARTIALLY_FILLED";
}

function formatRatePercent(value: number) {
  return `${(value * 100).toFixed(2)}%`;
}

function getNewYorkMarketClock(now: Date) {
  const parts = new Intl.DateTimeFormat("en-US", {
    timeZone: "America/New_York",
    weekday: "short",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  }).formatToParts(now);
  const weekday = parts.find((part) => part.type === "weekday")?.value ?? "Mon";
  const hour = Number(parts.find((part) => part.type === "hour")?.value ?? "0");
  const minute = Number(parts.find((part) => part.type === "minute")?.value ?? "0");
  return { weekday, hour, minute };
}
