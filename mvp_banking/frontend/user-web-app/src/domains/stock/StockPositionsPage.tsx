import { formatAmount } from "../../shared/utils/format";
import type { StockPosition } from "./types";

type StockPositionsPageProps = {
  loading: boolean;
  stockPositions: StockPosition[];
};

export function StockPositionsPage({ loading, stockPositions }: StockPositionsPageProps) {
  const totalMarketValue = stockPositions.reduce((sum, item) => sum + Number(item.marketValue ?? item.costBasis), 0);
  const totalUnrealizedPnl = stockPositions.reduce((sum, item) => sum + Number(item.unrealizedProfitLoss ?? 0), 0);
  const totalRealizedPnl = stockPositions.reduce((sum, item) => sum + Number(item.realizedProfitLoss), 0);

  if (loading) {
    return (
      <section className="timeline-panel">
        <p>포지션 정보를 불러오는 중입니다...</p>
      </section>
    );
  }

  return (
    <>
      <section className="summary-grid">
        <article className="summary-card">
          <span className="eyebrow">Open Positions</span>
          <strong>{stockPositions.length}</strong>
          <p>현재 보유 중인 종목 수</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Market Value</span>
          <strong>{formatAmount(totalMarketValue, "USD")}</strong>
          <p>현재가 기준 평가금액</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Unrealized P/L</span>
          <strong>{formatAmount(totalUnrealizedPnl, "USD")}</strong>
          <p>미실현 평가손익 기준</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Realized P/L</span>
          <strong>{formatAmount(totalRealizedPnl, "USD")}</strong>
          <p>누적 실현 손익 기준</p>
        </article>
      </section>

      <section className="timeline-panel">
        <div className="section-header">
          <div>
            <p className="eyebrow">Positions</p>
            <h2>보유 포지션</h2>
          </div>
        </div>
        <div className="table-shell">
          <table className="data-table">
            <thead>
              <tr>
                <th>종목</th>
                <th>시장</th>
                <th>보유 수량</th>
                <th>평균 단가</th>
                <th>현재가</th>
                <th>평가금액</th>
                <th>평가 손익</th>
                <th>실현 손익</th>
                <th>업데이트 일시</th>
              </tr>
            </thead>
            <tbody>
              {stockPositions.length ? (
                stockPositions.map((item) => (
                  <tr key={item.id}>
                    <td className="table-mono">{item.symbol}</td>
                    <td>{item.market}</td>
                    <td>{Number(item.quantity).toLocaleString()}주</td>
                    <td className="table-amount">{formatAmount(item.averagePrice, item.currency)}</td>
                    <td className="table-amount">
                      {item.currentPrice !== null ? formatAmount(item.currentPrice, item.currency) : "-"}
                    </td>
                    <td className="table-amount">
                      {item.marketValue !== null ? formatAmount(item.marketValue, item.currency) : formatAmount(item.costBasis, item.currency)}
                    </td>
                    <td>
                      <div className="table-cell-stack">
                        <b className={`status-pill ${Number(item.unrealizedProfitLoss ?? 0) >= 0 ? "approved" : "rejected"}`}>
                          {item.unrealizedProfitLoss !== null ? formatAmount(item.unrealizedProfitLoss, item.currency) : "-"}
                        </b>
                        <p>
                          {item.unrealizedProfitRate !== null
                            ? `${(Number(item.unrealizedProfitRate) * 100).toFixed(2)}%`
                            : "시세 대기"}
                        </p>
                      </div>
                    </td>
                    <td>
                      <div className="table-cell-stack">
                        <b className={`status-pill ${Number(item.realizedProfitLoss) >= 0 ? "approved" : "rejected"}`}>
                          {formatAmount(item.realizedProfitLoss, item.currency)}
                        </b>
                        <p>{item.quoteSource ?? "quote n/a"}</p>
                      </div>
                    </td>
                    <td>{new Date(item.quoteEffectiveAt ?? item.updatedAt).toLocaleString()}</td>
                  </tr>
                ))
              ) : (
                <tr className="table-empty-row">
                  <td colSpan={9}>
                    <strong>보유 포지션이 없습니다.</strong>
                    <p>승인된 주식 주문이 생기면 포지션이 표시됩니다.</p>
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
