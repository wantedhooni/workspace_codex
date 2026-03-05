import type { DashboardState } from "../../shared/types/dashboard";
import { formatAmount } from "../../shared/utils/format";
import { sortTransactionsByRecent } from "../../shared/utils/transactions";

type DashboardPageProps = {
  loading: boolean;
  dashboard: DashboardState | null;
};

export function DashboardPage({ loading, dashboard }: DashboardPageProps) {
  if (loading || !dashboard) {
    return (
      <section className="timeline-panel">
        <p>데이터를 불러오는 중입니다...</p>
      </section>
    );
  }

  const { insights, profile } = dashboard;
  const bankingAccounts = dashboard.accounts.filter((account) => account.accountType === "BANKING");
  const securitiesAccounts = dashboard.accounts.filter((account) => account.accountType === "SECURITIES");
  const pendingFundingCount = dashboard.fundingRequests.filter((item) => item.status === "PENDING_APPROVAL").length;
  const pendingExchangeCount = dashboard.exchangeRequests.filter((item) => item.status === "PENDING_APPROVAL").length;
  const pendingStockOrderCount = dashboard.stockOrders.filter((item) => item.status === "PENDING_APPROVAL").length;
  const latestTransaction = [...dashboard.transactions].sort(sortTransactionsByRecent)[0];

  return (
    <>
      <section className="hero-panel">
        <article className="hero-copy">
          <p className="eyebrow">Private Banking Desk</p>
          <h1>{profile.displayName}님의 자산 운영 현황</h1>
          <p className="description">
            총자산, 대기 지시, 통화 노출, 상위 보유종목을 한 화면에서 확인할 수 있도록 정리했습니다.
            현재 기준 총자산은 {formatAmount(insights.totalAssetsKrw, "KRW")} 입니다.
          </p>
        </article>
        <article className="hero-card">
          <p className="eyebrow">Action Board</p>
          <div className="attention-list">
            {insights.attentionItems.map((item) => (
              <div key={`${item.title}-${item.actionPath}`} className={`attention-item ${resolveAttentionTone(item.severity)}`}>
                <div>
                  <strong>{item.title}</strong>
                  <p>{item.message}</p>
                </div>
                <span>{item.actionPath === "/" ? "HOME" : item.actionPath}</span>
              </div>
            ))}
          </div>
        </article>
      </section>

      <section className="summary-grid">
        <article className="summary-card">
          <span className="eyebrow">Total Assets</span>
          <strong>{formatAmount(insights.totalAssetsKrw, "KRW")}</strong>
          <p>예수금과 평가자산을 합산한 원화 기준 총자산</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Cash Assets</span>
          <strong>{formatAmount(insights.cashAssetsKrw, "KRW")}</strong>
          <p>은행/증권 계좌 현금 잔액 합계</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Investment Assets</span>
          <strong>{formatAmount(insights.investmentAssetsKrw, "KRW")}</strong>
          <p>현재가 기준 보유 주식 평가금액</p>
        </article>
        <article className={`summary-card ${insights.unrealizedProfitLossKrw >= 0 ? "positive" : "negative"}`}>
          <span className="eyebrow">Unrealized P/L</span>
          <strong>{formatAmount(insights.unrealizedProfitLossKrw, "KRW")}</strong>
          <p>실시간 시세 기준 미실현 손익</p>
        </article>
      </section>

      <section className="summary-grid">
        <article className={`summary-card ${insights.realizedProfitLossKrw >= 0 ? "positive" : "negative"}`}>
          <span className="eyebrow">Realized P/L</span>
          <strong>{formatAmount(insights.realizedProfitLossKrw, "KRW")}</strong>
          <p>누적 실현 손익 기준</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Active Accounts</span>
          <strong>{insights.activeAccountCount}</strong>
          <p>거래 가능한 정상 상태 계좌 수</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Pending Instructions</span>
          <strong>{insights.pendingInstructionCount}</strong>
          <p>승인 대기 중인 환전 및 주문 지시 수</p>
        </article>
        <article className="summary-card">
          <span className="eyebrow">Latest Activity</span>
          <strong>{latestTransaction ? latestTransaction.transactionType : "NO DATA"}</strong>
          <p>{latestTransaction ? new Date(latestTransaction.occurredAt).toLocaleString() : "최근 거래 없음"}</p>
        </article>
      </section>

      <section className="asset-grid">
        <article className="timeline-panel asset-panel">
          <div className="section-header compact">
            <div>
              <p className="eyebrow">Currency Exposure</p>
              <h2>통화별 익스포저</h2>
            </div>
          </div>
          <div className="insight-table-shell">
            <table className="insight-table">
              <thead>
                <tr>
                  <th>통화</th>
                  <th>현금</th>
                  <th>포지션</th>
                  <th>총 노출</th>
                  <th>KRW 환산</th>
                </tr>
              </thead>
              <tbody>
                {insights.currencyExposures.map((item) => (
                  <tr key={item.currency}>
                    <td><strong>{item.currency}</strong></td>
                    <td>{formatAmount(item.cashBalance, item.currency)}</td>
                    <td>{formatAmount(item.positionValue, item.currency)}</td>
                    <td>{formatAmount(item.totalExposure, item.currency)}</td>
                    <td>{formatAmount(item.krwEquivalent, "KRW")}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </article>

        <article className="timeline-panel asset-panel">
          <div className="section-header compact">
            <div>
              <p className="eyebrow">Top Holdings</p>
              <h2>주요 보유 종목</h2>
            </div>
          </div>
          <div className="attention-list">
            {insights.topPositions.length ? (
              insights.topPositions.map((item) => (
                <div key={`${item.symbol}-${item.market}`} className={`attention-item ${item.unrealizedProfitLoss >= 0 ? "positive" : "negative"}`}>
                  <div>
                    <strong>{item.symbol} / {item.market}</strong>
                    <p>
                      평가금액 {formatAmount(item.marketValue, item.currency)} · 손익 {formatAmount(item.unrealizedProfitLoss, item.currency)}
                    </p>
                  </div>
                  <span>{`${Math.round(item.unrealizedProfitRate * 10000) / 100}%`}</span>
                </div>
              ))
            ) : (
              <div className="attention-item stable">
                <div>
                  <strong>보유 종목 없음</strong>
                  <p>아직 평가 대상 포지션이 없습니다.</p>
                </div>
                <span>0</span>
              </div>
            )}
          </div>
        </article>
      </section>

      <section className="asset-grid">
        <article className="timeline-panel asset-panel">
          <div className="section-header compact">
            <div>
              <p className="eyebrow">Portfolio Coverage</p>
              <h2>포트폴리오 구성</h2>
            </div>
          </div>
          <div className="allocation-list">
            <div className="allocation-row">
              <span>Banking Accounts</span>
              <strong>{bankingAccounts.length}</strong>
            </div>
            <div className="allocation-row">
              <span>Securities Accounts</span>
              <strong>{securitiesAccounts.length}</strong>
            </div>
            <div className="allocation-row">
              <span>FX Pairs</span>
              <strong>{dashboard.fxRates.length}</strong>
            </div>
            <div className="allocation-row">
              <span>Linked Bank Accounts</span>
              <strong>{dashboard.linkedBankAccounts.length}</strong>
            </div>
            <div className="allocation-row">
              <span>Tracked Positions</span>
              <strong>{dashboard.stockPositions.length}</strong>
            </div>
          </div>
        </article>

        <article className="timeline-panel asset-panel">
          <div className="section-header compact">
            <div>
              <p className="eyebrow">Service Queue</p>
              <h2>처리 대기 현황</h2>
            </div>
          </div>
          <div className="allocation-list">
            <div className="allocation-row">
              <span>Pending Funding</span>
              <strong>{pendingFundingCount}</strong>
            </div>
            <div className="allocation-row">
              <span>Pending Exchanges</span>
              <strong>{pendingExchangeCount}</strong>
            </div>
            <div className="allocation-row">
              <span>Pending Stock Orders</span>
              <strong>{pendingStockOrderCount}</strong>
            </div>
            <div className="allocation-row">
              <span>Partially Filled Orders</span>
              <strong>{insights.partiallyFilledOrderCount}</strong>
            </div>
            <div className="allocation-row">
              <span>Visible Transactions</span>
              <strong>{dashboard.transactions.length}</strong>
            </div>
          </div>
        </article>
      </section>

      <section className="asset-grid">
        <article className="timeline-panel asset-panel">
          <div className="section-header compact">
            <div>
              <p className="eyebrow">Notification Pulse</p>
              <h2>최근 알림</h2>
            </div>
          </div>
          <div className="attention-list">
            {dashboard.notifications.length ? (
              dashboard.notifications.slice(0, 4).map((item) => (
                <div key={item.id} className={`attention-item ${resolveNotificationTone(item.severity, item.read)}`}>
                  <div>
                    <strong>{item.title}</strong>
                    <p>{item.message}</p>
                  </div>
                  <span>{item.read ? "READ" : item.actionPath}</span>
                </div>
              ))
            ) : (
              <div className="attention-item stable">
                <div>
                  <strong>신규 알림 없음</strong>
                  <p>현재 확인할 신규 이벤트가 없습니다.</p>
                </div>
                <span>0</span>
              </div>
            )}
          </div>
        </article>

        <article className="timeline-panel asset-panel">
          <div className="section-header compact">
            <div>
              <p className="eyebrow">Notification Status</p>
              <h2>알림 처리 현황</h2>
            </div>
          </div>
          <div className="allocation-list">
            <div className="allocation-row">
              <span>Unread Notifications</span>
              <strong>{dashboard.unreadNotificationCount}</strong>
            </div>
            <div className="allocation-row">
              <span>Exchange Alerts</span>
              <strong>{dashboard.notifications.filter((item) => item.category === "EXCHANGE").length}</strong>
            </div>
            <div className="allocation-row">
              <span>Stock Order Alerts</span>
              <strong>{dashboard.notifications.filter((item) => item.category === "STOCK_ORDER").length}</strong>
            </div>
            <div className="allocation-row">
              <span>Portfolio Alerts</span>
              <strong>{dashboard.notifications.filter((item) => item.category === "PORTFOLIO").length}</strong>
            </div>
          </div>
        </article>
      </section>
    </>
  );
}

function resolveAttentionTone(severity: string) {
  if (severity === "HIGH") {
    return "negative";
  }
  if (severity === "MEDIUM" || severity === "WATCH") {
    return "watch";
  }
  if (severity === "STABLE") {
    return "stable";
  }
  return "info";
}

function resolveNotificationTone(severity: string, read: boolean) {
  if (read) {
    return "stable";
  }
  if (severity === "ACTION_REQUIRED" || severity === "WARNING") {
    return "negative";
  }
  if (severity === "SUCCESS") {
    return "positive";
  }
  return "info";
}
