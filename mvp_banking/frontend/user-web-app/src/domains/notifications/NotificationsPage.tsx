import { Link } from "react-router-dom";
import type { Notification } from "./types";

type NotificationsPageProps = {
  loading: boolean;
  notifications: Notification[];
  unreadCount: number;
  readingNotificationId: string | null;
  onRead: (notificationId: string) => Promise<void>;
};

export function NotificationsPage({
  loading,
  notifications,
  unreadCount,
  readingNotificationId,
  onRead,
}: NotificationsPageProps) {
  const actionRequiredCount = notifications.filter((item) => item.severity === "ACTION_REQUIRED" && !item.read).length;
  const todayCount = notifications.filter((item) => isSameCalendarDay(item.createdAt)).length;

  if (loading) {
    return (
      <section className="timeline-panel">
        <p>알림 센터를 불러오는 중입니다...</p>
      </section>
    );
  }

  return (
    <>
      <section className="timeline-panel asset-panel">
        <div className="page-convenience-strip">
          <strong>{notifications.length}건 이벤트 표시 중</strong>
          <span>미확인 항목부터 읽음 처리하고 바로가기 링크로 해당 업무 화면에 이동할 수 있습니다.</span>
        </div>
        <div className="section-header compact">
          <div>
            <p className="eyebrow">Notification Center</p>
            <h2>알림 센터</h2>
            <p className="section-copy">
              환전, 주문, 포트폴리오 상태 변화를 사용자 관점에서 읽기 쉬운 운영 알림으로 정리했습니다.
            </p>
          </div>
        </div>
        <div className="notification-summary-grid">
          <article className="summary-card">
            <span className="eyebrow">Unread</span>
            <strong>{unreadCount}건</strong>
            <p>아직 확인하지 않은 신규 알림</p>
          </article>
          <article className="summary-card negative">
            <span className="eyebrow">Action Required</span>
            <strong>{actionRequiredCount}건</strong>
            <p>승인 대기, 반려, 후속 확인이 필요한 항목</p>
          </article>
          <article className="summary-card">
            <span className="eyebrow">Today</span>
            <strong>{todayCount}건</strong>
            <p>오늘 생성된 포트폴리오 및 거래 알림</p>
          </article>
        </div>
      </section>

      <section className="timeline-panel asset-panel">
        <div className="section-header compact">
          <div>
            <p className="eyebrow">Inbox Queue</p>
            <h2>이벤트 알림 목록</h2>
            <p className="section-copy">
              거래·환전·포트폴리오 이벤트를 우선순위와 처리 상태 기준으로 바로 확인할 수 있습니다.
            </p>
          </div>
        </div>
        <div className="table-shell">
          <table className="data-table">
            <colgroup>
              <col style={{ width: 130 }} />
              <col style={{ width: 130 }} />
              <col style={{ width: 360 }} />
              <col style={{ width: 190 }} />
              <col style={{ width: 130 }} />
              <col style={{ width: 220 }} />
            </colgroup>
            <thead>
              <tr>
                <th>우선순위</th>
                <th>분류</th>
                <th>알림 내용</th>
                <th>도착 시각</th>
                <th>상태</th>
                <th>액션</th>
              </tr>
            </thead>
            <tbody>
              {notifications.length ? (
                notifications.map((notification) => (
                  <tr key={notification.id}>
                    <td>
                      <span className={`notification-pill severity-${notification.severity.toLowerCase()}`}>
                        {formatSeverityLabel(notification.severity)}
                      </span>
                    </td>
                    <td>{formatCategoryLabel(notification.category)}</td>
                    <td>
                      <div className="notification-cell">
                        <strong>{notification.title}</strong>
                        <p>{notification.message}</p>
                        <code>{notification.actionPath}</code>
                      </div>
                    </td>
                    <td>{new Date(notification.createdAt).toLocaleString()}</td>
                    <td>
                      <span className={`notification-pill status-${notification.read ? "read" : "unread"}`}>
                        {notification.read ? "읽음" : "미확인"}
                      </span>
                    </td>
                    <td className="table-action-cell">
                      <div className="table-action-row">
                        <Link className="inline-link-button" to={notification.actionPath === "/" ? "/" : notification.actionPath}>
                          바로가기
                        </Link>
                        {!notification.read ? (
                          <button
                            type="button"
                            className="table-inline-button"
                            disabled={readingNotificationId === notification.id}
                            onClick={() => void onRead(notification.id)}
                          >
                            {readingNotificationId === notification.id ? "처리 중..." : "읽음 처리"}
                          </button>
                        ) : null}
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={6} className="empty-row">
                    아직 표시할 알림이 없습니다.
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

function formatSeverityLabel(severity: string) {
  if (severity === "ACTION_REQUIRED") {
    return "즉시 확인";
  }
  if (severity === "WARNING") {
    return "주의";
  }
  if (severity === "SUCCESS") {
    return "완료";
  }
  return "안내";
}

function formatCategoryLabel(category: string) {
  if (category === "EXCHANGE") {
    return "환전";
  }
  if (category === "STOCK_ORDER") {
    return "주식주문";
  }
  if (category === "PORTFOLIO") {
    return "포트폴리오";
  }
  if (category === "APPROVAL") {
    return "승인";
  }
  return "시스템";
}

function isSameCalendarDay(dateTime: string) {
  const today = new Date();
  const target = new Date(dateTime);
  return today.getFullYear() === target.getFullYear()
    && today.getMonth() === target.getMonth()
    && today.getDate() === target.getDate();
}
