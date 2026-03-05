import type { __DOMAIN_PASCAL__Item } from "./types";

type __DOMAIN_PASCAL__PageProps = {
  loading: boolean;
  items: __DOMAIN_PASCAL__Item[];
};

export function __DOMAIN_PASCAL__Page({ loading, items }: __DOMAIN_PASCAL__PageProps) {
  if (loading) {
    return (
      <section className="timeline-panel">
        <p>__DOMAIN_TITLE__ 데이터를 불러오는 중입니다...</p>
      </section>
    );
  }

  return (
    <section className="timeline-panel">
      <div className="section-header">
        <div>
          <p className="eyebrow">__DOMAIN_TITLE__</p>
          <h2>__DOMAIN_TITLE__</h2>
        </div>
      </div>
      <div className="table-shell">
        <table className="data-table">
          <thead>
            <tr>
              <th>코드</th>
              <th>이름</th>
              <th>상태</th>
              <th>생성일시</th>
            </tr>
          </thead>
          <tbody>
            {items.length ? (
              items.map((item) => (
                <tr key={item.id}>
                  <td className="table-mono">{item.code}</td>
                  <td>{item.name}</td>
                  <td>
                    <b className={`status-pill ${item.status.toLowerCase()}`}>{item.status}</b>
                  </td>
                  <td>{new Date(item.createdAt).toLocaleString()}</td>
                </tr>
              ))
            ) : (
              <tr className="table-empty-row">
                <td colSpan={4}>
                  <strong>등록된 데이터가 없습니다.</strong>
                  <p>도메인 API를 연결한 뒤 목록을 확인하세요.</p>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}
