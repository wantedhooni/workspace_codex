type CqrsItem = {
  orderId: string;
  customerId: string;
  productCode: string;
  quantity: number;
  totalAmount: number;
  status: string;
  createdAt: string;
};

type SagaOrderItem = {
  orderId: string;
  customerId: string;
  productCode: string;
  quantity: number;
  totalAmount: number;
  status: string;
  failureReason?: string | null;
  createdAt: string;
};

type SagaExecutionItem = {
  sagaId: string;
  orderId: string;
  status: string;
  paymentStatus: string;
  inventoryStatus: string;
  failureReason?: string | null;
  createdAt: string;
};

type OutboxOrderItem = {
  orderId: string;
  customerId: string;
  productId: string;
  quantity: number;
  totalAmount: number;
  status: string;
  createdAt: string;
};

type OutboxEventItem = {
  outboxEventId: string;
  aggregateId: string;
  eventType: string;
  topic: string;
  status: string;
  attemptCount: number;
  createdAt: string;
};

const cqrsBaseUrl = process.env.CQRS_API_BASE_URL ?? "http://localhost:8081";
const sagaBaseUrl = process.env.SAGA_API_BASE_URL ?? "http://localhost:8082";
const outboxBaseUrl = process.env.OUTBOX_API_BASE_URL ?? "http://localhost:8083";

async function fetchJson<T>(url: string): Promise<T[]> {
  try {
    const response = await fetch(url, { cache: "no-store" });
    if (!response.ok) {
      return [];
    }
    return (await response.json()) as T[];
  } catch {
    return [];
  }
}

function won(value: number) {
  return new Intl.NumberFormat("ko-KR", {
    style: "currency",
    currency: "KRW",
    maximumFractionDigits: 0,
  }).format(value);
}

function date(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value));
}

function statusTone(status: string) {
  if (["COMPLETED", "PUBLISHED", "CREATED"].includes(status)) {
    return "status-success";
  }
  if (["FAILED", "CANCELLED"].includes(status)) {
    return "status-danger";
  }
  if (["COMPENSATED", "PROCESSING", "PAYMENT_COMPLETED", "INVENTORY_RESERVED"].includes(status)) {
    return "status-warning";
  }
  return "status-neutral";
}

function countByStatus(items: Array<{ status: string }>) {
  return items.reduce<Record<string, number>>((acc, item) => {
    acc[item.status] = (acc[item.status] ?? 0) + 1;
    return acc;
  }, {});
}

export default async function Page() {
  const [cqrsOrders, sagaOrders, sagaExecutions, outboxOrders, outboxEvents] = await Promise.all([
    fetchJson<CqrsItem>(`${cqrsBaseUrl}/api/order-summaries`),
    fetchJson<SagaOrderItem>(`${sagaBaseUrl}/api/saga/orders`),
    fetchJson<SagaExecutionItem>(`${sagaBaseUrl}/api/saga/orders/executions`),
    fetchJson<OutboxOrderItem>(`${outboxBaseUrl}/api/orders`),
    fetchJson<OutboxEventItem>(`${outboxBaseUrl}/api/outbox-events`),
  ]);

  const cqrsSummary = countByStatus(cqrsOrders);
  const sagaSummary = countByStatus(sagaOrders);
  const outboxSummary = countByStatus(outboxEvents);

  return (
    <main className="page">
      <div className="shell">
        <section className="hero">
          <article className="heroCard">
            <span className="heroEyebrow">Spring Patterns + React/Next.js</span>
            <h1>패턴 샘플 통합 대시보드</h1>
            <p>
              `sample_cqrs`, `sample_saga`, `transactional_outbox` 서비스의 샘플 데이터를 한 화면에서
              확인하는 운영 데모용 웹 앱이다. 서버 컴포넌트가 각 서비스 API를 직접 호출해 목록과 상태를
              수집한다.
            </p>
          </article>
          <div className="metaGrid">
            <div className="metaCard">
              <div className="metaLabel">CQRS 읽기 모델</div>
              <div className="metaValue">{cqrsOrders.length}</div>
            </div>
            <div className="metaCard">
              <div className="metaLabel">Saga 주문</div>
              <div className="metaValue">{sagaOrders.length}</div>
            </div>
            <div className="metaCard">
              <div className="metaLabel">Saga 실행</div>
              <div className="metaValue">{sagaExecutions.length}</div>
            </div>
            <div className="metaCard">
              <div className="metaLabel">Outbox 이벤트</div>
              <div className="metaValue">{outboxEvents.length}</div>
            </div>
          </div>
        </section>

        <section className="grid">
          <section className="panel">
            <div className="panelHeader">
              <h2 className="panelTitle">CQRS</h2>
              <p className="panelDesc">명령 모델과 조회 프로젝션이 분리된 최근 주문 목록</p>
            </div>
            <div className="panelBody">
              <div className="badgeRow">
                {Object.entries(cqrsSummary).map(([key, value]) => (
                  <span key={key} className="badge">
                    {key} {value}
                  </span>
                ))}
              </div>
              <div className="tableWrap">
                <table>
                  <thead>
                    <tr>
                      <th>주문</th>
                      <th>상품</th>
                      <th>금액</th>
                      <th>상태</th>
                    </tr>
                  </thead>
                  <tbody>
                    {cqrsOrders.map((item) => (
                      <tr key={item.orderId}>
                        <td>
                          <strong>{item.customerId}</strong>
                          {date(item.createdAt)}
                        </td>
                        <td>{item.productCode}</td>
                        <td>{won(item.totalAmount)}</td>
                        <td>
                          <span className={`status ${statusTone(item.status)}`}>{item.status}</span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {cqrsOrders.length === 0 && <div className="empty">서비스 연결 전이거나 데이터가 없습니다.</div>}
              </div>
            </div>
          </section>

          <section className="panel">
            <div className="panelHeader">
              <h2 className="panelTitle">Saga</h2>
              <p className="panelDesc">성공, 실패, 보상 시나리오를 포함한 주문 및 실행 상태</p>
            </div>
            <div className="panelBody">
              <div className="badgeRow">
                {Object.entries(sagaSummary).map(([key, value]) => (
                  <span key={key} className="badge">
                    {key} {value}
                  </span>
                ))}
              </div>
              <div className="tableWrap">
                <table>
                  <thead>
                    <tr>
                      <th>주문</th>
                      <th>상품</th>
                      <th>상태</th>
                      <th>실패 사유</th>
                    </tr>
                  </thead>
                  <tbody>
                    {sagaOrders.map((item) => (
                      <tr key={item.orderId}>
                        <td>
                          <strong>{item.customerId}</strong>
                          {date(item.createdAt)}
                        </td>
                        <td>{item.productCode}</td>
                        <td>
                          <span className={`status ${statusTone(item.status)}`}>{item.status}</span>
                        </td>
                        <td>{item.failureReason ?? "-"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {sagaOrders.length === 0 && <div className="empty">서비스 연결 전이거나 데이터가 없습니다.</div>}
              </div>
              <div className="tableCard">
                <div className="tableWrap">
                  <table>
                    <thead>
                      <tr>
                        <th>Saga ID</th>
                        <th>주문 ID</th>
                        <th>결제</th>
                        <th>재고</th>
                      </tr>
                    </thead>
                    <tbody>
                      {sagaExecutions.map((item) => (
                        <tr key={item.sagaId}>
                          <td>{item.sagaId.slice(0, 8)}</td>
                          <td>{item.orderId.slice(0, 8)}</td>
                          <td>
                            <span className={`status ${statusTone(item.paymentStatus)}`}>{item.paymentStatus}</span>
                          </td>
                          <td>
                            <span className={`status ${statusTone(item.inventoryStatus)}`}>{item.inventoryStatus}</span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  {sagaExecutions.length === 0 && <div className="empty">Saga 실행 데이터가 없습니다.</div>}
                </div>
              </div>
            </div>
          </section>

          <section className="panel">
            <div className="panelHeader">
              <h2 className="panelTitle">Transactional Outbox</h2>
              <p className="panelDesc">주문 저장과 이벤트 릴레이 상태를 함께 확인하는 패널</p>
            </div>
            <div className="panelBody">
              <div className="badgeRow">
                {Object.entries(outboxSummary).map(([key, value]) => (
                  <span key={key} className="badge">
                    {key} {value}
                  </span>
                ))}
              </div>
              <div className="tableWrap">
                <table>
                  <thead>
                    <tr>
                      <th>고객</th>
                      <th>상품</th>
                      <th>총액</th>
                      <th>상태</th>
                    </tr>
                  </thead>
                  <tbody>
                    {outboxOrders.map((item) => (
                      <tr key={item.orderId}>
                        <td>
                          <strong>{item.customerId}</strong>
                          {date(item.createdAt)}
                        </td>
                        <td>{item.productId}</td>
                        <td>{won(item.totalAmount)}</td>
                        <td>
                          <span className={`status ${statusTone(item.status)}`}>{item.status}</span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {outboxOrders.length === 0 && <div className="empty">주문 데이터가 없습니다.</div>}
              </div>
              <div className="tableCard">
                <div className="tableWrap">
                  <table>
                    <thead>
                      <tr>
                        <th>이벤트</th>
                        <th>토픽</th>
                        <th>상태</th>
                        <th>시도</th>
                      </tr>
                    </thead>
                    <tbody>
                      {outboxEvents.map((item) => (
                        <tr key={item.outboxEventId}>
                          <td>{item.aggregateId.slice(0, 8)}</td>
                          <td>{item.topic}</td>
                          <td>
                            <span className={`status ${statusTone(item.status)}`}>{item.status}</span>
                          </td>
                          <td>{item.attemptCount}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  {outboxEvents.length === 0 && <div className="empty">Outbox 이벤트 데이터가 없습니다.</div>}
                </div>
              </div>
            </div>
          </section>
        </section>

        <p className="footer">
          기본 연결 주소: CQRS `http://localhost:8081`, Saga `http://localhost:8082`, Outbox `http://localhost:8083`,
          Web `http://localhost:3000`
        </p>
      </div>
    </main>
  );
}
