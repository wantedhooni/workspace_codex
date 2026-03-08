import type { ColDef } from "ag-grid-community";
import { formatAmount } from "../../shared/utils/format";
import type { PageResponse } from "../../shared/types/page";
import type { StockPosition } from "./types";
import { AppGridTable } from "../../shared/components/AppGridTable";

type StockPositionsPageProps = {
  loading: boolean;
  stockPositions: StockPosition[];
  stockPositionRows: StockPosition[];
  stockPositionPage: PageResponse<StockPosition>;
  onStockPositionPageChange: (page: number, pageSize: number) => void;
};

type StockPositionCellParams = { data?: StockPosition };

export function StockPositionsPage({
  loading,
  stockPositions,
  stockPositionRows,
  stockPositionPage,
  onStockPositionPageChange,
}: StockPositionsPageProps) {
  const totalMarketValue = stockPositions.reduce((sum, item) => sum + Number(item.marketValue ?? item.costBasis), 0);
  const totalUnrealizedPnl = stockPositions.reduce((sum, item) => sum + Number(item.unrealizedProfitLoss ?? 0), 0);
  const totalRealizedPnl = stockPositions.reduce((sum, item) => sum + Number(item.realizedProfitLoss), 0);
  const columnDefs: ColDef<StockPosition>[] = [
    { headerName: "종목", field: "symbol", minWidth: 130, cellClass: "table-mono" },
    { headerName: "시장", field: "market", minWidth: 130 },
    {
      headerName: "보유 수량",
      minWidth: 130,
      cellRenderer: ({ data }: StockPositionCellParams) => (data ? `${Number(data.quantity).toLocaleString()}주` : "-"),
    },
    {
      headerName: "평균 단가",
      minWidth: 160,
      cellRenderer: ({ data }: StockPositionCellParams) => (data ? <span className="table-amount">{formatAmount(data.averagePrice, data.currency)}</span> : "-"),
    },
    {
      headerName: "현재가",
      minWidth: 160,
      cellRenderer: ({ data }: StockPositionCellParams) => (data ? <span className="table-amount">{data.currentPrice !== null ? formatAmount(data.currentPrice, data.currency) : "-"}</span> : "-"),
    },
    {
      headerName: "평가금액",
      minWidth: 170,
      cellRenderer: ({ data }: StockPositionCellParams) => (data ? <span className="table-amount">{data.marketValue !== null ? formatAmount(data.marketValue, data.currency) : formatAmount(data.costBasis, data.currency)}</span> : "-"),
    },
    {
      headerName: "평가 손익",
      minWidth: 180,
      cellRenderer: ({ data }: StockPositionCellParams) => data ? (
        <div className="table-cell-stack">
          <b className={`status-pill ${Number(data.unrealizedProfitLoss ?? 0) >= 0 ? "approved" : "rejected"}`}>
            {data.unrealizedProfitLoss !== null ? formatAmount(data.unrealizedProfitLoss, data.currency) : "-"}
          </b>
          <p>
            {data.unrealizedProfitRate !== null
              ? `${(Number(data.unrealizedProfitRate) * 100).toFixed(2)}%`
              : "시세 대기"}
          </p>
        </div>
      ) : "-",
    },
    {
      headerName: "실현 손익",
      minWidth: 180,
      cellRenderer: ({ data }: StockPositionCellParams) => data ? (
        <div className="table-cell-stack">
          <b className={`status-pill ${Number(data.realizedProfitLoss) >= 0 ? "approved" : "rejected"}`}>
            {formatAmount(data.realizedProfitLoss, data.currency)}
          </b>
          <p>{data.quoteSource ?? "quote n/a"}</p>
        </div>
      ) : "-",
    },
    {
      headerName: "업데이트 일시",
      minWidth: 200,
      cellRenderer: ({ data }: StockPositionCellParams) => (data ? new Date(data.quoteEffectiveAt ?? data.updatedAt).toLocaleString() : "-"),
    },
  ];

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
        <AppGridTable
          rowData={stockPositionRows}
          columnDefs={columnDefs}
          emptyMessage="보유 포지션이 없습니다."
          getRowId={(row) => row.id}
          pagination={{
            current: stockPositionPage.page + 1,
            pageSize: stockPositionPage.size,
            total: stockPositionPage.totalElements,
            onChange: onStockPositionPageChange,
          }}
        />
      </section>
    </>
  );
}
