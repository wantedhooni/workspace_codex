import { useEffect, useMemo, useState } from "react";
import { formatAmount } from "../../shared/utils/format";
import type { FxRate } from "./types";

type FxRatesPageProps = {
  loading: boolean;
  fxRates: FxRate[];
};

export function FxRatesPage({ loading, fxRates }: FxRatesPageProps) {
  const [selectedRateId, setSelectedRateId] = useState("");
  const [amount, setAmount] = useState("1000");
  const [direction, setDirection] = useState<"baseToQuote" | "quoteToBase">("baseToQuote");

  useEffect(() => {
    if (fxRates.length && !selectedRateId) {
      setSelectedRateId(fxRates[0].id);
    }
  }, [fxRates, selectedRateId]);

  const selectedRate = useMemo(
    () => fxRates.find((rate) => rate.id === selectedRateId) ?? fxRates[0],
    [fxRates, selectedRateId],
  );

  const parsedAmount = Number(amount || 0);
  const isValidAmount = Number.isFinite(parsedAmount) && parsedAmount > 0;
  const sourceCurrency = !selectedRate
    ? ""
    : direction === "baseToQuote"
      ? selectedRate.baseCurrency
      : selectedRate.quoteCurrency;
  const destinationCurrency = !selectedRate
    ? ""
    : direction === "baseToQuote"
      ? selectedRate.quoteCurrency
      : selectedRate.baseCurrency;
  const directionalRate = !selectedRate
    ? 0
    : direction === "baseToQuote"
      ? Number(selectedRate.rate)
      : 1 / Number(selectedRate.rate);
  const convertedAmount = !selectedRate || !isValidAmount
    ? 0
    : direction === "baseToQuote"
      ? parsedAmount * Number(selectedRate.rate)
      : parsedAmount / Number(selectedRate.rate);

  if (loading) {
    return (
      <section className="timeline-panel">
        <p>환율 시세를 불러오는 중입니다...</p>
      </section>
    );
  }

  return (
    <>
      <section className="timeline-panel">
        <div className="section-header">
          <div>
            <p className="eyebrow">FX Calculator</p>
            <h2>환율 계산기</h2>
          </div>
        </div>

        {selectedRate ? (
          <div className="fx-calculator-panel">
            <article className="fx-calculator-card">
              <div className="fx-flow-strip">
                <div className="fx-flow-side">
                  <span className="fx-flow-label">Source</span>
                  <strong>{sourceCurrency}</strong>
                </div>
                <div className="fx-flow-center">
                  <span className="fx-flow-arrow">→</span>
                  <b>{sourceCurrency} to {destinationCurrency}</b>
                </div>
                <div className="fx-flow-side destination">
                  <span className="fx-flow-label">Destination</span>
                  <strong>{destinationCurrency}</strong>
                </div>
              </div>

              <div className="fx-calculator-grid">
                <label className="fx-field">
                  <span>환율 페어</span>
                  <select value={selectedRate.id} onChange={(event) => setSelectedRateId(event.target.value)}>
                    {fxRates.map((rate) => (
                      <option key={rate.id} value={rate.id}>
                        {rate.baseCurrency}/{rate.quoteCurrency} · {Number(rate.rate).toLocaleString()}
                      </option>
                    ))}
                  </select>
                </label>

                <label className="fx-field">
                  <span>환산 방향</span>
                  <div className="fx-direction-switch">
                    <button
                      type="button"
                      className={direction === "baseToQuote" ? "active" : ""}
                      onClick={() => setDirection("baseToQuote")}
                    >
                      {selectedRate.baseCurrency} → {selectedRate.quoteCurrency}
                    </button>
                    <button
                      type="button"
                      className={direction === "quoteToBase" ? "active" : ""}
                      onClick={() => setDirection("quoteToBase")}
                    >
                      {selectedRate.quoteCurrency} → {selectedRate.baseCurrency}
                    </button>
                  </div>
                </label>
              </div>

              <div className="fx-rate-banner">
                <span className="fx-rate-banner-label">Current rate</span>
                <strong>
                  1 {sourceCurrency} = {Number(directionalRate).toLocaleString(undefined, { maximumFractionDigits: 6 })} {destinationCurrency}
                </strong>
              </div>

              <div className="fx-result-grid">
                <label className="fx-amount-card">
                  <span>Source · {sourceCurrency}</span>
                  <input
                    type="number"
                    min="0"
                    step="0.0001"
                    value={amount}
                    onChange={(event) => setAmount(event.target.value)}
                    placeholder="금액 입력"
                  />
                </label>
                <div className="fx-equals">=</div>
                <article className="fx-output-card">
                  <span>Destination · {destinationCurrency}</span>
                  <strong>
                    {isValidAmount
                      ? formatAmount(convertedAmount, destinationCurrency)
                      : "금액 입력 필요"}
                  </strong>
                  <p>
                    {isValidAmount
                      ? `${Number(parsedAmount).toLocaleString()} ${sourceCurrency} = ${Number(convertedAmount).toLocaleString()} ${destinationCurrency}`
                      : `1 ${sourceCurrency} = ${Number(directionalRate).toLocaleString(undefined, { maximumFractionDigits: 6 })} ${destinationCurrency}`}
                  </p>
                </article>
              </div>

              <div className="fx-meta-row">
                <span>Source: {selectedRate.source}</span>
                <span>Effective: {new Date(selectedRate.effectiveAt).toLocaleString()}</span>
              </div>
            </article>
          </div>
        ) : (
          <article className="empty-state-card">
            <strong>표시할 환율 데이터가 없습니다.</strong>
            <p>환율 데이터가 준비되면 계산기를 사용할 수 있습니다.</p>
          </article>
        )}
      </section>

      <section className="timeline-panel">
        <div className="section-header">
          <div>
            <p className="eyebrow">FX Market</p>
            <h2>실시간 환율 보드</h2>
          </div>
        </div>
        <div className="table-shell">
          <table className="data-table">
            <colgroup>
              <col style={{ width: 180 }} />
              <col style={{ width: 160 }} />
              <col style={{ width: 180 }} />
              <col style={{ width: 220 }} />
            </colgroup>
            <thead>
              <tr>
                <th>페어</th>
                <th>환율</th>
                <th>Source</th>
                <th>Effective</th>
              </tr>
            </thead>
            <tbody>
              {fxRates.map((rate) => (
                <tr key={rate.id}>
                  <td className="table-mono">
                    {rate.baseCurrency}/{rate.quoteCurrency}
                  </td>
                  <td className="table-amount">{Number(rate.rate).toLocaleString()}</td>
                  <td>{rate.source}</td>
                  <td>{new Date(rate.effectiveAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
